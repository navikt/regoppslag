package no.nav.regoppslag.consumer.ereg.support;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.dok.brevdata.felles.v1.navfelles.UtenlandskPostadresse;
import no.nav.regoppslag.consumer.ereg.MottakerTo;
import no.nav.regoppslag.consumer.map.Postadresse;
import no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagIkkeFunnetException;
import no.nav.regoppslag.service.PostnummerService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static no.nav.regoppslag.consumer.map.OrganisasjonPostadresseMapper.mapPostadresseToNorskPostadresse;
import static no.nav.regoppslag.consumer.map.OrganisasjonPostadresseMapper.mapPostadresseToUtenlandskPostadresse;
import static no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode.ENHETFORRETNINGSADRESSE;
import static no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode.ENHETPOSTADRESSE;
import static no.nav.regoppslag.service.LandkodeService.finnLandnavn;
import static no.nav.regoppslag.util.SafeLoggingUtil.removeUnsafeChars;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Component
@Slf4j
public class OrganisasjonEregMapper {

	public static final String LANDKODE_NORGE = "NO";
	private static final String LAND_NORGE = "Norge";

	private final PostnummerService postnummerService;

	public OrganisasjonEregMapper(PostnummerService postnummerService) {
		this.postnummerService = postnummerService;
	}

	public String getSakspartNavn(Organisasjon wsOrganisasjon) {
		return mapOrganisasjonNavn(wsOrganisasjon);
	}

	public MottakerTo map(String orgNummer, Organisasjon wsOrganisasjon) {
		Mottaker mottaker = new no.nav.dok.brevdata.felles.v1.navfelles.Organisasjon();

		OrganisasjonDetaljer orgDet = wsOrganisasjon.getOrganisasjonDetaljer();

		mottaker.setKortNavn(mapOrganisasjonNavn(wsOrganisasjon));
		mottaker.setNavn(mapOrganisasjonNavn(wsOrganisasjon));
		no.nav.regoppslag.consumer.map.Postadresse postadresse;
		try {
			postadresse = mapAdresse(orgNummer, orgDet);
		} catch (RegOppslagFunctionalException e) {
			log.info("Mapping av adresse feilet for orgnummer: {}", removeUnsafeChars(wsOrganisasjon.getOrganisasjonsnummer()));
			throw e;
		}

		if (LAND_NORGE.equals(postadresse.getLand()) || postadresse.getLand() == null) {
			NorskPostadresse norskPostadresse = mapPostadresseToNorskPostadresse(postadresse);
			mottaker.setMottakeradresse(norskPostadresse);
		} else {
			UtenlandskPostadresse utenlandskPostadresse = mapPostadresseToUtenlandskPostadresse(postadresse);
			mottaker.setMottakeradresse(utenlandskPostadresse);
		}

		return MottakerTo.builder()
				.adresseKilde(mapAdresseKilde(orgDet))
				.mottaker(mottaker)
				.spraakKode(getSpraakKodeAsString(orgDet))
				.build();
	}

	private no.nav.regoppslag.consumer.map.Postadresse mapAdresse(String orgNummer, OrganisasjonDetaljer orgDet) {
		if (orgDet.getOpphoersdato() != null && LocalDate.now().isAfter(orgDet.getOpphoersdato())) {
			String message = format("Organisasjon har opphørt, opphørsdato=%s, orgnr=%s", ISO_LOCAL_DATE.format(orgDet.getOpphoersdato()), orgNummer);
			throw new RegOppslagIkkeFunnetException(message, NOT_FOUND);
		}

		no.nav.regoppslag.consumer.ereg.support.Postadresse activeAddress = selectActiveAddress(orgDet.getPostadresser(), orgDet.getForretningsadresser())
				.orElseThrow(() -> new RegOppslagIkkeFunnetException("Ingen gyldige adresser funnet for orgnummer=" + orgNummer, NOT_FOUND));

		return mapPostadresse(activeAddress);
	}

	private String getSpraakKodeAsString(OrganisasjonDetaljer orgDet) {
		return orgDet.getMaalform();
	}

	private String mapOrganisasjonNavn(Organisasjon wsOrganisasjon) {
		return wsOrganisasjon.getNavn().getSammensattnavn();
	}

	private AdresseKildeCode mapAdresseKilde(OrganisasjonDetaljer orgDet) {
		return orgDet.getPostadresser() == null || orgDet.getPostadresser().stream().noneMatch(this::isValidPostAdresse) ? ENHETFORRETNINGSADRESSE : ENHETPOSTADRESSE;
	}

	private boolean isValidGyldighetsAndBruksPeriode(Gyldighetsperiode gyldighetsperiode, Bruksperiode bruksperiode) {
		final LocalDateTime nowTime = LocalDateTime.now();
		final LocalDate nowDate = LocalDate.now();

		return gyldighetsperiode.getFom().isBefore(nowDate)
			   && (gyldighetsperiode.getTom() == null || gyldighetsperiode.getTom().isAfter(nowDate))
			   && (bruksperiode.getTom() == null || bruksperiode.getTom().isAfter(nowTime));
	}

	// Adresse skal overstyre forretningsadresse dersom den finnes
	private Optional<no.nav.regoppslag.consumer.ereg.support.Postadresse> selectActiveAddress(List<no.nav.regoppslag.consumer.ereg.support.Postadresse> postadresse, List<no.nav.regoppslag.consumer.ereg.support.Postadresse> forretningsadresse) {
		// Stream.of er basert på array så rekkefølgen er ordered, gyldige postadresse vil bli funnet før forretningsadresse
		return Stream.of(selectGyldigPostAdresse(postadresse), selectGyldigPostAdresse(forretningsadresse))
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
			postadresse.setLand(finnLandnavn(eregAdresse.getLandkode()));
		}

		postadresse.setAdresselinje1(eregAdresse.getAdresselinje1());
		postadresse.setAdresselinje2(eregAdresse.getAdresselinje2());
		postadresse.setAdresselinje3(eregAdresse.getAdresselinje3());

		return postadresse;
	}
}
