package no.nav.regoppslag.consumer.organisasjonv4.support;

import static no.nav.regoppslag.consumer.map.PostadresseMapper.mapPostadresseToNorskpostadresse;
import static no.nav.regoppslag.consumer.map.PostadresseMapper.mapPostadresseToUtenlandskadresse;
import static no.nav.regoppslag.metrics.PrometheusLabels.LAND;
import static no.nav.regoppslag.metrics.PrometheusLabels.ORGANISASJONV4_MAPPER;
import static no.nav.regoppslag.metrics.PrometheusLabels.UKJENT_POSTNUMMER;
import static no.nav.regoppslag.metrics.PrometheusLabels.UKJENT_POSTSTED;
import static no.nav.regoppslag.metrics.PrometheusMetrics.getConsumerId;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestCounter;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.dok.brevdata.felles.v1.navfelles.UtenlandskPostadresse;
import no.nav.dok.brevdata.felles.v1.simpletypes.Spraakkode;
import no.nav.regoppslag.consumer.map.Postadresse;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.service.LandkodeService;
import no.nav.regoppslag.service.PostnummerService;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Gateadresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.GeografiskAdresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.NoekkelVerdiAdresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.OrganisasjonsDetaljer;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjonsnavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.SemistrukturertAdresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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

	@Inject
	private final LandkodeService landkodeService;

	@Inject
	private final PostnummerService postnummerService;

	private static final String LAND_NORGE = "Norge";

	public OrganisasjonV4Mapper(PostnummerService postnummerService, LandkodeService landkodeService) {
		this.landkodeService = landkodeService;
		this.postnummerService = postnummerService;
	}

	public String getSakspartNavn(Organisasjon wsOrganisasjon) throws RegOppslagFunctionalException {
		OrganisasjonsDetaljer orgDet = wsOrganisasjon.getOrganisasjonDetaljer();

		Organisasjonsnavn organisasjonsnavn = findValidOrgNavn(orgDet);

		if (organisasjonsnavn == null) {
			throw new RegOppslagFunctionalException("Ingen gyldige sakspartnavn funnet");
		}

		return StringUtils.collectionToDelimitedString(((UstrukturertNavn) organisasjonsnavn.getNavn()).getNavnelinje(), " ")
				.trim();
	}


	public Mottaker map(Organisasjon wsOrganisasjon, String serviceCode) throws RegOppslagFunctionalException {
		Mottaker mottaker = new no.nav.dok.brevdata.felles.v1.navfelles.Organisasjon();

		OrganisasjonsDetaljer orgDet = wsOrganisasjon.getOrganisasjonDetaljer();

		mottaker.setKortNavn(mapOrganisasjonKortnavn(wsOrganisasjon));
		mottaker.setNavn(mapOrganisasjonNavn(orgDet));
		mottaker.setSpraakkode(mapSpraakkode(orgDet));
		Postadresse postadresse;
		try {
			postadresse = mapAdresse(orgDet);
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

		return mottaker;
	}

	private void incrementFunctionalMetrics(Postadresse postadresse, String serviceCode) {
		if (isBlank(postadresse.getPoststed()) && (LAND_NORGE.equals(postadresse.getLand()) || isBlank(postadresse.getLand()))) {
			requestCounter.labels(serviceCode, ORGANISASJONV4_MAPPER, UKJENT_POSTSTED, getConsumerId(), UKJENT_POSTSTED).inc();
		}
		if (isBlank(postadresse.getPostnummer()) && (LAND_NORGE.equals(postadresse.getLand()) || isBlank(postadresse.getLand()))) {
			requestCounter.labels(serviceCode, ORGANISASJONV4_MAPPER, UKJENT_POSTNUMMER, getConsumerId(), UKJENT_POSTNUMMER).inc();
		}
		requestCounter.labels(serviceCode, ORGANISASJONV4_MAPPER, LAND, getConsumerId(), postadresse.getLand() == null ? "Ukjent" : postadresse
				.getLand()).inc();
	}


	private Postadresse mapAdresse(OrganisasjonsDetaljer orgDet) throws RegOppslagFunctionalException {

		List<GeografiskAdresse> geografiskAdresseList = orgDet.getPostadresse();
		geografiskAdresseList.addAll(orgDet.getForretningsadresse());

		if (orgDet.getOpphoersdato() != null && LocalDateTime.now().isAfter(orgDet.getOpphoersdato().toGregorianCalendar().toZonedDateTime().toLocalDateTime())) {
			log.info("Organisasjon har opphørt, orgnr: ", orgDet.getOrgnummer());
			throw new RegOppslagFunctionalException("Organisasjon har opphørt, orgnr: ", orgDet.getOrgnummer());
		}

		GeografiskAdresse activeAddress = findValidAddress(geografiskAdresseList);

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

	private Spraakkode mapSpraakkode(OrganisasjonsDetaljer orgDet) {
		if (orgDet.getGjeldendeMaalform() != null) {
			if ("NO".equals(orgDet.getGjeldendeMaalform().getKodeRef())) {
				return Spraakkode.NB;
			} else {
				return Spraakkode.valueOf(orgDet.getGjeldendeMaalform().getKodeRef());
			}
		}
		return null;
	}

	private String mapOrganisasjonKortnavn(Organisasjon wsOrganisasjon) {
		return StringUtils.collectionToDelimitedString(((UstrukturertNavn) wsOrganisasjon.getNavn()).getNavnelinje(), " ")
				.trim();
	}

	private String mapOrganisasjonNavn(OrganisasjonsDetaljer orgDet) throws RegOppslagFunctionalException {
		Organisasjonsnavn organisasjonsnavn = findValidOrgNavn(orgDet);

		if (organisasjonsnavn == null) {
			throw new RegOppslagFunctionalException("Ingen gyldige organisasjonsnavn funnet");
		}

		return StringUtils.collectionToDelimitedString(((UstrukturertNavn) organisasjonsnavn.getNavn()).getNavnelinje(), " ")
				.trim();
	}

	private Organisasjonsnavn findValidOrgNavn(OrganisasjonsDetaljer orgDet) {
		final LocalDateTime now = LocalDateTime.now();

		return orgDet.getNavn().stream()
				.findFirst()
				.filter(n -> {
					LocalDateTime fomGyldig = n.getFomGyldighetsperiode().toGregorianCalendar().toZonedDateTime().toLocalDateTime();
					LocalDateTime tomGyldig = n.getTomGyldighetsperiode() == null ? null : n.getTomGyldighetsperiode().toGregorianCalendar().toZonedDateTime().toLocalDateTime();
					LocalDateTime tomBruk = n.getTomBruksperiode() == null ? null : n.getTomBruksperiode().toGregorianCalendar().toZonedDateTime().toLocalDateTime();

					return fomGyldig.isBefore(now)
							&& (tomGyldig == null || tomGyldig.isAfter(now))
							&& (tomBruk == null || tomBruk.isAfter(now));
				})
				.orElse(null);
	}

	private GeografiskAdresse findValidAddress(List<GeografiskAdresse> adresseList) throws RegOppslagFunctionalException {
		final LocalDateTime now = LocalDateTime.now();

		GeografiskAdresse collect = adresseList.stream()
				.findFirst()
				.filter(a -> {
					LocalDateTime fomGyldig = a.getFomGyldighetsperiode().toGregorianCalendar().toZonedDateTime().toLocalDateTime();
					LocalDateTime tomGyldig = a.getTomGyldighetsperiode() == null ? null : a.getTomGyldighetsperiode().toGregorianCalendar().toZonedDateTime().toLocalDateTime();
					LocalDateTime tomBruk = a.getTomBruksperiode() == null ? null : a.getTomBruksperiode().toGregorianCalendar().toZonedDateTime().toLocalDateTime();

					boolean isValid = validateAddress(a);

					return fomGyldig.isBefore(now)
							&& (tomGyldig == null || tomGyldig.isAfter(now))
							&& (tomBruk == null || tomBruk.isAfter(now))
							&& isValid;
				})
				.orElse(null);

		if (collect == null) {
			throw new RegOppslagFunctionalException("Ingen gyldige adresser funnet");
		}

		return collect;
	}

	private boolean validateAddress(GeografiskAdresse adresse) {
		AtomicBoolean valid = new AtomicBoolean(true);

		if (!(adresse instanceof SemistrukturertAdresse)) {
			Gateadresse gateadresse = (Gateadresse) adresse;
			if (gateadresse.getPoststed() == null) {
				log.info("Mangelfull adresse, mangler poststed");
				valid.set(false);
			}
		} else {
			List<NoekkelVerdiAdresse> adresseledd = ((SemistrukturertAdresse) adresse).getAdresseledd();
			adresseledd.forEach(al -> {
				if (POSTNR.equals(al.getNoekkel().getKodeRef()) && isEmpty(al.getVerdi())) {
					log.info("Mangelfull adresse, mangler postnummer");
					valid.set(false);
				}
				if (POSTSTED.equals(al.getNoekkel().getKodeRef()) && isEmpty(al.getVerdi())) {
					log.info("Mangelfull adresse, mangler poststed");
					valid.set(false);
				}
			});
		}

		return valid.get();
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