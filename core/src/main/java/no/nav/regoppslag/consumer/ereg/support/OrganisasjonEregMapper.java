package no.nav.regoppslag.consumer.ereg.support;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.dok.brevdata.felles.v1.navfelles.UtenlandskPostadresse;
import no.nav.regoppslag.consumer.map.Postadresse;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagIkkeFunnetException;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.regoppslag.service.LandkodeService;
import no.nav.regoppslag.service.PostnummerService;
import no.nav.regoppslag.to.MottakerTo;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.util.stream.Collectors.joining;
import static no.nav.regoppslag.consumer.map.PostadresseMapper.mapPostadresseToNorskpostadresse;
import static no.nav.regoppslag.consumer.map.PostadresseMapper.mapPostadresseToUtenlandskadresse;
import static no.nav.regoppslag.metrics.MetricLabels.EREG_MAPPER;
import static no.nav.regoppslag.metrics.MetricLabels.LAND;
import static no.nav.regoppslag.metrics.MetricLabels.UKJENT_POSTNUMMER;
import static no.nav.regoppslag.metrics.MetricLabels.UKJENT_POSTSTED;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Component
@Slf4j
public class OrganisasjonEregMapper {

	public static final String POSTSTED = "poststed";
	public static final String LANDKODE_NORGE = "NO";

	private final LandkodeService landkodeService;
	private final PostnummerService postnummerService;
	private final MicrometerMetrics metrics;

	private static final String LAND_NORGE = "Norge";

	@Inject
	public OrganisasjonEregMapper(PostnummerService postnummerService, LandkodeService landkodeService, MicrometerMetrics metrics) {
		this.landkodeService = landkodeService;
		this.postnummerService = postnummerService;
		this.metrics = metrics;
	}

	public String getSakspartNavn(Organisasjon wsOrganisasjon) {
		OrganisasjonDetaljer orgDet = wsOrganisasjon.getOrganisasjonDetaljer();
		Navn orgNavn = findValidOrgNavn(orgDet)
				.orElseThrow(() -> new RegOppslagIkkeFunnetException("Ingen gyldige organisasjonsnavn funnet for orgnummer=" + wsOrganisasjon.getOrganisasjonsnummer(), NOT_FOUND));

		return aggregerNavn(orgNavn);
	}


	public MottakerTo map(String orgNummer, Organisasjon wsOrganisasjon, String serviceCode) {
		Mottaker mottaker = new no.nav.dok.brevdata.felles.v1.navfelles.Organisasjon();

		OrganisasjonDetaljer orgDet = wsOrganisasjon.getOrganisasjonDetaljer();

		mottaker.setKortNavn(mapOrganisasjonKortnavn(wsOrganisasjon));
		mottaker.setNavn(mapOrganisasjonNavn(wsOrganisasjon));
		no.nav.regoppslag.consumer.map.Postadresse postadresse;
		try {
			postadresse = mapAdresse(orgNummer, orgDet);
		} catch (RegOppslagFunctionalException e) {
			log.info(String.format("Mapping av adresse feilet for orgnummer: %s", wsOrganisasjon.getOrganisasjonsnummer()));
			throw e;
		}

		incrementFunctionalMetrics(postadresse, serviceCode);

		if (LAND_NORGE.equals(postadresse.getLand()) || postadresse.getLand() == null) {
			NorskPostadresse norskPostadresse = mapPostadresseToNorskpostadresse(postadresse);
			mottaker.setMottakeradresse(norskPostadresse);
		} else {
			UtenlandskPostadresse utenlandskPostadresse = mapPostadresseToUtenlandskadresse(postadresse);
			mottaker.setMottakeradresse(utenlandskPostadresse);
		}

		return MottakerTo.builder().mottaker(mottaker).spraakKode(getSpraakKodeAsString(orgDet)).build();
	}

	private void incrementFunctionalMetrics(no.nav.regoppslag.consumer.map.Postadresse postadresse, String serviceCode) {
		if (isBlank(postadresse.getPoststed()) && (LAND_NORGE.equals(postadresse.getLand()) || isBlank(postadresse.getLand()))) {
			metrics.meter(serviceCode, EREG_MAPPER, UKJENT_POSTSTED, UKJENT_POSTSTED);
		}
		if (isBlank(postadresse.getPostnummer()) && (LAND_NORGE.equals(postadresse.getLand()) || isBlank(postadresse.getLand()))) {
			metrics.meter(serviceCode, EREG_MAPPER, UKJENT_POSTNUMMER, UKJENT_POSTNUMMER);
		}
		metrics.meter(serviceCode, EREG_MAPPER, LAND, postadresse.getLand() == null ? "Ukjent" : postadresse.getLand());
	}


	private no.nav.regoppslag.consumer.map.Postadresse mapAdresse(String orgNummer, OrganisasjonDetaljer orgDet) {
		if (orgDet.getOpphoersdato() != null && LocalDate.now().isAfter(orgDet.getOpphoersdato())) {
			String message = String.format("Organisasjon har opphørt, opphørsdato=%s, orgnr=%s", ISO_LOCAL_DATE.format(orgDet.getOpphoersdato()), orgNummer);
			throw new RegOppslagIkkeFunnetException(message, "Organisasjon har opphørt", NOT_FOUND);
		}

		no.nav.regoppslag.consumer.ereg.support.Postadresse activeAddress = selectActiveAddress(orgDet.getPostadresser(), orgDet.getForretningsadresser())
				.orElseThrow(() -> new RegOppslagIkkeFunnetException("Ingen gyldige adresser funnet for orgnummer=" + orgNummer, NOT_FOUND));

		return mapPostadresse(activeAddress);
	}

	private String getSpraakKodeAsString(OrganisasjonDetaljer orgDet) {
		return orgDet.getMaalform();
	}

	private String mapOrganisasjonKortnavn(Organisasjon wsOrganisasjon) {
		return wsOrganisasjon.getNavn().getRedigertnavn();
	}

	private String mapOrganisasjonNavn(Organisasjon orgDet) {
		Navn organisasjonsnavn = findValidOrgNavn(orgDet.getOrganisasjonDetaljer())
				.orElseThrow(() -> new RegOppslagIkkeFunnetException("Ingen gyldige organisasjonsnavn funnet for orgnummer=" + orgDet.getOrganisasjonsnummer(), NOT_FOUND));

		return aggregerNavn(organisasjonsnavn);
	}

	private Optional<Navn> findValidOrgNavn(OrganisasjonDetaljer orgDet) {
		return orgDet.getNavn().stream()
				.filter((org) -> isValidGyldighetsAndBruksPeriode(org.getGyldighetsperiode(), org.getBruksperiode()))
				.findFirst();
	}

	private boolean isValidGyldighetsAndBruksPeriode(Gyldighetsperiode gyldighetsperiode, Bruksperiode bruksperiode) {
		final LocalDateTime nowTime = LocalDateTime.now();
		final LocalDate nowDate = LocalDate.now();

		return gyldighetsperiode.getFom().isBefore(nowDate)
				&& (gyldighetsperiode.getTom() == null || gyldighetsperiode.getTom().isAfter(nowDate))
				&& (bruksperiode.getTom() == null || bruksperiode.getTom().isAfter(nowTime));
	}

	// Postadresse skal overstyre forretningsadresse dersom den finnes
	private Optional<no.nav.regoppslag.consumer.ereg.support.Postadresse> selectActiveAddress(List<no.nav.regoppslag.consumer.ereg.support.Postadresse> postadresse, List<no.nav.regoppslag.consumer.ereg.support.Postadresse> forretningsadresse) {
		// Stream.of er basert på array så rekkefølgen er ordered, gyldige postadresse vil bli funnet før forretningsadresse
		return Stream.of(
						selectGyldigPostAdresse(postadresse), selectGyldigPostAdresse(forretningsadresse))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst();
	}

	private Optional<no.nav.regoppslag.consumer.ereg.support.Postadresse> selectGyldigPostAdresse(List<no.nav.regoppslag.consumer.ereg.support.Postadresse> adresser) {
		if (adresser == null) {
			return Optional.empty();
		}
		return adresser.stream().filter(this::isValidPostAdresse).findAny();
	}

	private boolean isValidPostAdresse(no.nav.regoppslag.consumer.ereg.support.Postadresse adresse) {
		boolean isValidGeografiskAdresse = isValidGyldighetsAndBruksPeriode(adresse.getGyldighetsperiode(), adresse.getBruksperiode()) && landkodeIsNotNull(adresse);
		if (isValidGeografiskAdresse && landkodeIsNorge(adresse)) {
			isValidGeografiskAdresse = containsPostnummer(adresse);
		}
		return isValidGeografiskAdresse;
	}

	private boolean landkodeIsNorge(no.nav.regoppslag.consumer.ereg.support.Postadresse adresse) {
		return adresse.getLandkode().equals(LANDKODE_NORGE);
	}

	private boolean landkodeIsNotNull(no.nav.regoppslag.consumer.ereg.support.Postadresse adresse) {
		return adresse.getLandkode() != null;
	}

	private boolean containsPostnummer(no.nav.regoppslag.consumer.ereg.support.Postadresse adresse) {
		return (adresse.getPostnummer() != null || adresse.getPoststed() != null);
	}

	private no.nav.regoppslag.consumer.map.Postadresse mapPostadresse(no.nav.regoppslag.consumer.ereg.support.Postadresse eregAdresse) {
		no.nav.regoppslag.consumer.map.Postadresse postadresse = Postadresse.builder().build();

		if (eregAdresse.getLandkode().equals(LANDKODE_NORGE)) {
			postadresse.setPostnummer(eregAdresse.getPostnummer());
			if (eregAdresse.getPostnummer() != null) {
				postadresse.setPoststed(postnummerService.finnPoststed(eregAdresse.getPostnummer()));
			}
		} else {
			postadresse.setPostnummer(eregAdresse.getPostnummer());
			postadresse.setPoststed(eregAdresse.getPoststed());
		}

		if (eregAdresse.getLandkode() != null) {
			postadresse.setLand(landkodeService.finnLandnavn(eregAdresse.getLandkode()));
		}

		postadresse.setAdresselinje1(eregAdresse.getAdresselinje1());
		postadresse.setAdresselinje2(eregAdresse.getAdresselinje2());
		postadresse.setAdresselinje3(eregAdresse.getAdresselinje3());

		return postadresse;
	}

	private String aggregerNavn(Navn navn) {

		List<String> navnelinjer = Arrays.asList(
				navn.getNavnelinje1(),
				navn.getNavnelinje2(),
				navn.getNavnelinje3(),
				navn.getNavnelinje4(),
				navn.getNavnelinje5());
		return navnelinjer.stream().filter(StringUtils::isNotBlank)
				.map(String::trim)
				.collect(joining(" "));
	}
}
