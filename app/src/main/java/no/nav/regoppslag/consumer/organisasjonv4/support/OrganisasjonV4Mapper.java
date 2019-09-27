package no.nav.regoppslag.consumer.organisasjonv4.support;

import static no.nav.regoppslag.consumer.map.PostadresseMapper.mapPostadresseToNorskpostadresse;
import static no.nav.regoppslag.consumer.map.PostadresseMapper.mapPostadresseToUtenlandskadresse;
import static no.nav.regoppslag.metrics.MetricLabels.LAND;
import static no.nav.regoppslag.metrics.MetricLabels.ORGANISASJONV4_MAPPER;
import static no.nav.regoppslag.metrics.MetricLabels.UKJENT_POSTNUMMER;
import static no.nav.regoppslag.metrics.MetricLabels.UKJENT_POSTSTED;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.dok.brevdata.felles.v1.navfelles.UtenlandskPostadresse;
import no.nav.regoppslag.consumer.map.Postadresse;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.regoppslag.service.LandkodeService;
import no.nav.regoppslag.service.PostnummerService;
import no.nav.regoppslag.treg001.to.MottakerTo;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Gateadresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.GeografiskAdresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.OrganisasjonsDetaljer;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjonsnavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.SemistrukturertAdresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Ketill Fenne, Visma Consulting AS
 */

@Component
@Slf4j
public class OrganisasjonV4Mapper {

	public static final String ADRESSELINJE_1 = "adresselinje1";
	public static final String ADRESSELINJE_2 = "adresselinje2";
	public static final String ADRESSE_3_SPLIT_1 = "Adresse 3 split 1";
	public static final String POSTNR = "postnr";
	public static final String POSTSTED = "poststed";

	private final LandkodeService landkodeService;
	private final PostnummerService postnummerService;
	private final MicrometerMetrics metrics;

	private static final String LAND_NORGE = "Norge";

	@Inject
	public OrganisasjonV4Mapper(PostnummerService postnummerService, LandkodeService landkodeService, MicrometerMetrics metrics) {
		this.landkodeService = landkodeService;
		this.postnummerService = postnummerService;
		this.metrics = metrics;
	}

	public String getSakspartNavn(Organisasjon wsOrganisasjon) throws RegOppslagFunctionalException {
		OrganisasjonsDetaljer orgDet = wsOrganisasjon.getOrganisasjonDetaljer();
		Organisasjonsnavn organisasjonsnavn = findValidOrgNavn(orgDet)
				.orElseThrow(() -> new RegOppslagFunctionalException("Ingen gyldige organisasjonsnavn funnet for orgnummer=" + orgDet.getOrgnummer()));

		return StringUtils.collectionToDelimitedString(((UstrukturertNavn) organisasjonsnavn.getNavn()).getNavnelinje(), " ")
				.trim();
	}


	public MottakerTo map(String orgNummer, Organisasjon wsOrganisasjon, String serviceCode) throws RegOppslagFunctionalException {
		Mottaker mottaker = new no.nav.dok.brevdata.felles.v1.navfelles.Organisasjon();

		OrganisasjonsDetaljer orgDet = wsOrganisasjon.getOrganisasjonDetaljer();

		mottaker.setKortNavn(mapOrganisasjonKortnavn(wsOrganisasjon));
		mottaker.setNavn(mapOrganisasjonNavn(orgDet));
		Postadresse postadresse;
		try {
			postadresse = mapAdresse(orgNummer, orgDet);
		} catch (RegOppslagFunctionalException e) {
			log.info(String.format("Mapping av adresse feilet for orgnummer: %s", wsOrganisasjon.getOrgnummer()));
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

	private void incrementFunctionalMetrics(Postadresse postadresse, String serviceCode) {
		if (isBlank(postadresse.getPoststed()) && (LAND_NORGE.equals(postadresse.getLand()) || isBlank(postadresse.getLand()))) {
			metrics.meter(serviceCode, ORGANISASJONV4_MAPPER, UKJENT_POSTSTED, UKJENT_POSTSTED);
		}
		if (isBlank(postadresse.getPostnummer()) && (LAND_NORGE.equals(postadresse.getLand()) || isBlank(postadresse.getLand()))) {
			metrics.meter(serviceCode, ORGANISASJONV4_MAPPER, UKJENT_POSTNUMMER, UKJENT_POSTNUMMER);
		}
		metrics.meter(serviceCode, ORGANISASJONV4_MAPPER, LAND, postadresse.getLand() == null ? "Ukjent" : postadresse.getLand());
	}


	private Postadresse mapAdresse(String orgNummer, OrganisasjonsDetaljer orgDet) throws RegOppslagFunctionalException {
		if (orgDet.getOpphoersdato() != null && LocalDateTime.now().isAfter(orgDet.getOpphoersdato().toGregorianCalendar().toZonedDateTime().toLocalDateTime())) {
			String message = String.format("Organisasjon har opphørt, opphørsdato=%s orgnr=%s", new SimpleDateFormat("dd/MM/yyyy").format(orgDet.getOpphoersdato().toGregorianCalendar().getTime()), orgNummer);
			throw new RegOppslagFunctionalException(message, "Organisasjon har opphørt");
		}

		GeografiskAdresse activeAddress = selectActiveAddress(orgDet.getPostadresse(), orgDet.getForretningsadresse())
				.orElseThrow(() -> new RegOppslagFunctionalException("Ingen gyldige adresser funnet for orgnummer=" + orgDet.getOrgnummer()));

		Postadresse postadresse = Postadresse.builder().build();
		if (activeAddress instanceof SemistrukturertAdresse) {
			SemistrukturertAdresse semistrukturertAdresse = (SemistrukturertAdresse) activeAddress;
			postadresse = settAdresseledd(semistrukturertAdresse);
			if (postadresse.getPostnummer() != null) {
				postadresse.setPoststed(postnummerService.finnPoststed(postadresse.getPostnummer()));
			}
		} else {
			Gateadresse gateadresse = (Gateadresse) activeAddress;
			postadresse.setAdresselinje1(String.format("%s %s%s", gateadresse.getGatenavn(), gateadresse.getHusnummer(), gateadresse.getHusbokstav()));
			postadresse.setPostnummer(gateadresse.getPoststed().getKodeRef());
			postadresse.setPoststed(postnummerService.finnPoststed(gateadresse.getPoststed().getKodeRef()));
		}

		if (activeAddress.getLandkode() != null) {
			postadresse.setLand(landkodeService.finnLandnavn(activeAddress.getLandkode().getKodeRef()));
		}

		return postadresse;
	}

	private String getSpraakKodeAsString(OrganisasjonsDetaljer orgDet) {
		if (orgDet.getGjeldendeMaalform() != null) {
			return orgDet.getGjeldendeMaalform().getKodeRef();
		}
		return null;
	}

	private String mapOrganisasjonKortnavn(Organisasjon wsOrganisasjon) {
		return StringUtils.collectionToDelimitedString(((UstrukturertNavn) wsOrganisasjon.getNavn()).getNavnelinje(), " ")
				.trim();
	}

	private String mapOrganisasjonNavn(OrganisasjonsDetaljer orgDet) throws RegOppslagFunctionalException {
		Organisasjonsnavn organisasjonsnavn = findValidOrgNavn(orgDet)
				.orElseThrow(() -> new RegOppslagFunctionalException("Ingen gyldige organisasjonsnavn funnet for orgnummer=" + orgDet.getOrgnummer()));
		return StringUtils.collectionToDelimitedString(((UstrukturertNavn) organisasjonsnavn.getNavn()).getNavnelinje(), " ")
				.trim();
	}

	private Optional<Organisasjonsnavn> findValidOrgNavn(OrganisasjonsDetaljer orgDet) {
		return orgDet.getNavn().stream()
				.filter(this::isValidGyldighetsPeriodeForOrganisasjonsnavn)
				.findFirst();
	}

	private boolean isValidGyldighetsPeriodeForOrganisasjonsnavn(Organisasjonsnavn organisasjonsnavn) {
		final LocalDateTime now = LocalDateTime.now();

		LocalDateTime fomGyldig = organisasjonsnavn.getFomGyldighetsperiode().toGregorianCalendar().toZonedDateTime().toLocalDateTime();
		LocalDateTime tomGyldig = organisasjonsnavn.getTomGyldighetsperiode() == null ? null : organisasjonsnavn.getTomGyldighetsperiode().toGregorianCalendar().toZonedDateTime().toLocalDateTime();
		LocalDateTime tomBruk = organisasjonsnavn.getTomBruksperiode() == null ? null : organisasjonsnavn.getTomBruksperiode().toGregorianCalendar().toZonedDateTime().toLocalDateTime();

		return fomGyldig.isBefore(now)
				&& (tomGyldig == null || tomGyldig.isAfter(now))
				&& (tomBruk == null || tomBruk.isAfter(now));
	}

	// Postadresse skal overstyre forretningsadresse dersom den finnes
	private Optional<GeografiskAdresse> selectActiveAddress(List<GeografiskAdresse> postadresse, List<GeografiskAdresse> forretningsadresse) {
		// Stream.of er basert på array så rekkefølgen er ordered, gyldige postadresse vil bli funnet før forretningsadresse
		return Stream.of(
				selectGyldigGeografiskAdresse(postadresse), selectGyldigGeografiskAdresse(forretningsadresse))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst();
	}

	private Optional<GeografiskAdresse> selectGyldigGeografiskAdresse(List<GeografiskAdresse> adresser) {
		return adresser.stream().filter(this::isValidGeografiskAdresse).findAny();
	}

	private boolean isValidGeografiskAdresse(GeografiskAdresse adresse) {
		return isValidGyldighetsPeriodeForAdresse(adresse) && containsPostnummer(adresse);
	}

	private boolean isValidGyldighetsPeriodeForAdresse(GeografiskAdresse adresse) {
		final LocalDateTime now = LocalDateTime.now();

		LocalDateTime fomGyldig = adresse.getFomGyldighetsperiode().toGregorianCalendar().toZonedDateTime().toLocalDateTime();
		LocalDateTime tomGyldig = adresse.getTomGyldighetsperiode() == null ? null : adresse.getTomGyldighetsperiode().toGregorianCalendar().toZonedDateTime().toLocalDateTime();
		LocalDateTime tomBruk = adresse.getTomBruksperiode() == null ? null : adresse.getTomBruksperiode().toGregorianCalendar().toZonedDateTime().toLocalDateTime();

		return fomGyldig.isBefore(now)
				&& (tomGyldig == null || tomGyldig.isAfter(now))
				&& (tomBruk == null || tomBruk.isAfter(now));
	}

	private boolean containsPostnummer(GeografiskAdresse adresse) {
		if (adresse instanceof SemistrukturertAdresse) {
			return ((SemistrukturertAdresse) adresse).getAdresseledd()
					.stream()
					.anyMatch(nva -> POSTNR.equals(nva.getNoekkel().getKodeRef()) && isNotEmpty(nva.getVerdi()));
		} else if (adresse instanceof Gateadresse) {
			return ((Gateadresse) adresse).getPoststed() != null;
		} else {
			return false;
		}
	}

	private Postadresse settAdresseledd(SemistrukturertAdresse semistrukturertAdresse) {
		Postadresse postadresse = Postadresse.builder().build();
		semistrukturertAdresse.getAdresseledd().forEach(nokkel -> {
			if (ADRESSELINJE_1.equals(nokkel.getNoekkel().getKodeRef())) {
				postadresse.setAdresselinje1(nokkel.getVerdi());
			} else if (ADRESSELINJE_2.equals(nokkel.getNoekkel().getKodeRef())) {
				postadresse.setAdresselinje2(nokkel.getVerdi());
			} else if (ADRESSE_3_SPLIT_1.equals(nokkel.getNoekkel().getKodeRef())) {
				postadresse.setAdresselinje3(nokkel.getVerdi());
			} else if (POSTNR.equals(nokkel.getNoekkel().getKodeRef())) {
				postadresse.setPostnummer(nokkel.getVerdi());
			} else if (POSTSTED.equals(nokkel.getNoekkel().getKodeRef())) {
				postadresse.setPoststed(nokkel.getVerdi());
			}
		});
		return postadresse;
	}


}