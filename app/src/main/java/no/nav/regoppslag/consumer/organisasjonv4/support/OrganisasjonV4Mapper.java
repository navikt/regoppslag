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

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.dok.brevdata.felles.v1.navfelles.Sakspart;
import no.nav.dok.brevdata.felles.v1.navfelles.UtenlandskPostadresse;
import no.nav.dok.brevdata.felles.v1.simpletypes.Spraakkode;
import no.nav.regoppslag.consumer.map.Postadresse;
import no.nav.regoppslag.service.LandkodeService;
import no.nav.regoppslag.service.PostnummerService;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Gateadresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.GeografiskAdresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.NoekkelVerdiAdresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.OrganisasjonsDetaljer;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.SemistrukturertAdresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.StedsadresseNorge;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.StrukturertAdresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.inject.Inject;
import java.util.Optional;

/**
 * @author Ketill Fenne, Visma Consulting AS
 */

@Component
@Slf4j
public class OrganisasjonV4Mapper {

	@Inject
	private final LandkodeService landkodeService;

	@Inject
	private final PostnummerService postnummerService;

	private static final String LAND_NORGE = "Norge";

	public OrganisasjonV4Mapper(PostnummerService postnummerService, LandkodeService landkodeService) {
		this.landkodeService = landkodeService;
		this.postnummerService = postnummerService;
	}

	public String getSakspartNavn(Organisasjon wsOrganisasjon) {
		OrganisasjonsDetaljer orgDet = wsOrganisasjon.getOrganisasjonDetaljer();
		return StringUtils.collectionToDelimitedString(((UstrukturertNavn) orgDet.getNavn()
				.get(0)
				.getNavn()).getNavnelinje(), " ").trim();
	}


	public Mottaker map(Organisasjon wsOrganisasjon, String serviceCode) {
		Mottaker mottaker = new no.nav.dok.brevdata.felles.v1.navfelles.Organisasjon();

		OrganisasjonsDetaljer orgDet = wsOrganisasjon.getOrganisasjonDetaljer();

		mottaker.setKortNavn(mapOrganisasjonKortnavn(wsOrganisasjon));
		mottaker.setNavn(mapOrganisasjonNavn(orgDet));
		mottaker.setSpraakkode(mapSpraakkode(orgDet));
		Postadresse postadresse = mapAdresse(orgDet);

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


	private Postadresse mapAdresse(OrganisasjonsDetaljer orgDet) {
		if (!CollectionUtils.isEmpty(orgDet.getPostadresse())) {
			return mapPostadresse(orgDet);
		} else if (!CollectionUtils.isEmpty(orgDet.getForretningsadresse())) {
			return mapForretningsAdresse(orgDet);
		}
		return Postadresse.builder().build();
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

	private String mapOrganisasjonNavn(OrganisasjonsDetaljer orgDet) {
		return StringUtils.collectionToDelimitedString(((UstrukturertNavn) orgDet.getNavn()
				.get(0)
				.getNavn()).getNavnelinje(), " ").trim();
	}

	private Postadresse mapPostadresse(OrganisasjonsDetaljer orgDet) {
		Postadresse postadresse = Postadresse.builder().build();
		if (orgDet.getPostadresse().get(0) instanceof SemistrukturertAdresse) {
			SemistrukturertAdresse semistrukturertAdresse = (SemistrukturertAdresse) orgDet.getPostadresse().get(0);
			postadresse = settAdresseledd(semistrukturertAdresse);
			if (postadresse.getPostnummer() != null) {
				postadresse.setPoststed(postnummerService.finnPoststed(postadresse.getPostnummer()));
			}
		} else {
			Gateadresse gateadresse = (Gateadresse) orgDet.getPostadresse().get(0);
			postadresse.setAdresselinje1(Optional.ofNullable(gateadresse.getGatenavn())
					.orElse("") + " " + Optional.ofNullable(gateadresse.getHusnummer() == null ? null : gateadresse.getHusnummer()
					.toString())
					.orElse("") + Optional.ofNullable(gateadresse.getHusbokstav()).orElse(""));
			if (orgDet.getPostadresse().get(0) instanceof StrukturertAdresse) {
				StedsadresseNorge stedsadresseNorge = (StedsadresseNorge) orgDet.getPostadresse().get(0);
				if (stedsadresseNorge.getPoststed() != null) {
					postadresse.setPostnummer(stedsadresseNorge.getPoststed().getKodeRef());
					postadresse.setPoststed(postnummerService.finnPoststed(stedsadresseNorge.getPoststed().getKodeRef()));
				}
			}
		}
		GeografiskAdresse geografiskAdresse = orgDet.getPostadresse().get(0);
		if (geografiskAdresse.getLandkode() != null) {
			postadresse.setLand(landkodeService.finnLandnavn(geografiskAdresse.getLandkode().getKodeRef()));
		}
		return postadresse;
	}

	private Postadresse mapForretningsAdresse(OrganisasjonsDetaljer orgDet) {
		Postadresse postadresse = Postadresse.builder().build();
		if (orgDet.getForretningsadresse().get(0) instanceof SemistrukturertAdresse) {
			SemistrukturertAdresse semistrukturertAdresse = (SemistrukturertAdresse) orgDet.getForretningsadresse().get(0);
			postadresse = settAdresseledd(semistrukturertAdresse);
			if (postadresse.getPostnummer() != null) {
				postadresse.setPoststed(postnummerService.finnPoststed(postadresse.getPostnummer()));
			}
		} else {
			Gateadresse gateadresse = (Gateadresse) orgDet.getForretningsadresse().get(0);
			postadresse.setAdresselinje1(Optional.ofNullable(gateadresse.getGatenavn())
					.orElse("") + " " + Optional.ofNullable(gateadresse.getHusnummer() == null ? null : gateadresse.getHusnummer()
					.toString())
					.orElse("") + Optional.ofNullable(gateadresse.getHusbokstav()).orElse(""));
			if (orgDet.getForretningsadresse().get(0) instanceof StrukturertAdresse) {
				StedsadresseNorge stedsadresseNorge = (StedsadresseNorge) orgDet.getForretningsadresse().get(0);
				if (stedsadresseNorge.getPoststed() != null) {
					postadresse.setPostnummer(stedsadresseNorge.getPoststed().getKodeRef());
					postadresse.setPoststed(postnummerService.finnPoststed(stedsadresseNorge.getPoststed().getKodeRef()));
				}
			}
		}

		GeografiskAdresse geografiskAdresse = orgDet.getForretningsadresse().get(0);
		if (geografiskAdresse.getLandkode() != null) {
			postadresse.setLand(landkodeService.finnLandnavn(geografiskAdresse.getLandkode().getKodeRef()));
		}
		return postadresse;
	}

	private Postadresse settAdresseledd(SemistrukturertAdresse semistrukturertAdresse) {
		Postadresse postadresse = Postadresse.builder().build();
		for (NoekkelVerdiAdresse nokler : semistrukturertAdresse.getAdresseledd()) {
			if ("adresselinje1".equals(nokler.getNoekkel().getKodeRef())) {
				postadresse.setAdresselinje1(nokler.getVerdi());
			} else if ("adresselinje2".equals(nokler.getNoekkel().getKodeRef())) {
				postadresse.setAdresselinje2(nokler.getVerdi());
			} else if ("Adresse 3 split 1".equals(nokler.getNoekkel().getKodeRef())) {
				postadresse.setAdresselinje3(nokler.getVerdi());
			} else if ("postnr".equals(nokler.getNoekkel().getKodeRef())) {
				postadresse.setPostnummer(nokler.getVerdi());
			} else if ("poststed".equals(nokler.getNoekkel().getKodeRef())) {
				postadresse.setPoststed(nokler.getVerdi());
			}
		}
		return postadresse;
	}


}