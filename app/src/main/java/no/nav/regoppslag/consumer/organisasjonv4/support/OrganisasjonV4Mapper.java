package no.nav.regoppslag.consumer.organisasjonv4.support;

import static no.nav.regoppslag.metrics.PrometheusLabels.ADRESSETYPE;
import static no.nav.regoppslag.metrics.PrometheusLabels.LAND;
import static no.nav.regoppslag.metrics.PrometheusLabels.ORGANISASJONV4_MAPPER;
import static no.nav.regoppslag.metrics.PrometheusLabels.PERSONV3_MAPPER;
import static no.nav.regoppslag.metrics.PrometheusLabels.UKJENT_POSTNUMMER;
import static no.nav.regoppslag.metrics.PrometheusMetrics.getConsumerId;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestCounter;

import com.neovisionaries.i18n.CountryCode;
import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.dok.brevdata.felles.v1.navfelles.Sakspart;
import no.nav.dok.brevdata.felles.v1.simpletypes.Spraakkode;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
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

	public void map(Organisasjon wsOrganisasjon, Sakspart sakspart)  {
		OrganisasjonsDetaljer orgDet = wsOrganisasjon.getOrganisasjonDetaljer();
		sakspart.setNavn(StringUtils.collectionToDelimitedString(((UstrukturertNavn) orgDet.getNavn()
				.get(0)
				.getNavn()).getNavnelinje(), " ").trim());
	}


	public void map(Organisasjon wsOrganisasjon, Mottaker mottaker, String serviceCode) throws RegOppslagFunctionalException {
		OrganisasjonsDetaljer orgDet = wsOrganisasjon.getOrganisasjonDetaljer();
		mottaker.setKortNavn(StringUtils.collectionToDelimitedString(((UstrukturertNavn) wsOrganisasjon.getNavn()).getNavnelinje(), " ")
				.trim());
		mottaker.setNavn(StringUtils.collectionToDelimitedString(((UstrukturertNavn) orgDet.getNavn()
				.get(0)
				.getNavn()).getNavnelinje(), " ").trim());
		
		if (orgDet.getGjeldendeMaalform() != null) {
			if ("NO".equals(orgDet.getGjeldendeMaalform().getKodeRef())) {
				mottaker.setSpraakkode(Spraakkode.NB);
			} else {
				mottaker.setSpraakkode(Spraakkode.valueOf(orgDet.getGjeldendeMaalform().getKodeRef()));
			}
		}
		
		NorskPostadresse norskPostadresse = new NorskPostadresse();
		if (!CollectionUtils.isEmpty(orgDet.getPostadresse())) {
			mapPostadresse(orgDet, norskPostadresse);
		} else if (!CollectionUtils.isEmpty(orgDet.getForretningsadresse())) {
			mapForretningsAdresse(orgDet, norskPostadresse);
		}

		if (StringUtils.isEmpty(norskPostadresse.getPostnummer())) {
			log.info(String.format("%s Mottaker med type=ORGANISASJON mangler postnummer. Setter postnummer til \"0000\" og poststed til \"UKJENT/UNKNOWN\". land=%s, poststed=%s", serviceCode, norskPostadresse
					.getLand(), norskPostadresse.getPoststed()));
			norskPostadresse.setPostnummer("0000");
			norskPostadresse.setPoststed("UKJENT/UNKNOWN");

			if (norskPostadresse.getLand()==null){
				requestCounter.labels(serviceCode, ORGANISASJONV4_MAPPER, UKJENT_POSTNUMMER, getConsumerId(), "UKJENT.UKJENT_LAND").inc();
			} else if (LAND_NORGE.equals(norskPostadresse.getLand())) {
				requestCounter.labels(serviceCode, ORGANISASJONV4_MAPPER, UKJENT_POSTNUMMER, getConsumerId(), "UKJENT.NORGE").inc();
			} else {
				requestCounter.labels(serviceCode, ORGANISASJONV4_MAPPER, UKJENT_POSTNUMMER, getConsumerId(), "UKJENT.UTLAND").inc();
			}
		}


		requestCounter.labels(serviceCode, ORGANISASJONV4_MAPPER, LAND, getConsumerId(), norskPostadresse.getLand() == null ? "Ukjent" : norskPostadresse
				.getLand()).inc();

		mottaker.setMottakeradresse(norskPostadresse);
	}
	
	private void mapPostadresse(OrganisasjonsDetaljer orgDet, NorskPostadresse norskPostadresse) {
		if (orgDet.getPostadresse().get(0) instanceof SemistrukturertAdresse) {
			SemistrukturertAdresse adresse = (SemistrukturertAdresse) orgDet.getPostadresse().get(0);
			settAdresseledd(adresse, norskPostadresse);
			if (norskPostadresse.getPostnummer() != null) {
				norskPostadresse.setPoststed(postnummerService.finnPoststed(norskPostadresse.getPostnummer()));
			}
		} else {
			Gateadresse gateadresse = (Gateadresse) orgDet.getPostadresse().get(0);
			norskPostadresse.setAdresselinje1(Optional.ofNullable(gateadresse.getGatenavn())
					.orElse("") + " " + Optional.ofNullable(gateadresse.getHusnummer() == null ? null : gateadresse.getHusnummer()
					.toString())
					.orElse("") + Optional.ofNullable(gateadresse.getHusbokstav()).orElse(""));
			if (orgDet.getPostadresse().get(0) instanceof StrukturertAdresse) {
				StedsadresseNorge stedsadresseNorge = (StedsadresseNorge) orgDet.getPostadresse().get(0);
				if (stedsadresseNorge.getPoststed() != null) {
					norskPostadresse.setPostnummer(stedsadresseNorge.getPoststed().getKodeRef());
					norskPostadresse.setPoststed(postnummerService.finnPoststed(stedsadresseNorge.getPoststed().getKodeRef()));
				}
			}
		}
		GeografiskAdresse geografiskAdresse = orgDet.getPostadresse().get(0);
		if (geografiskAdresse.getLandkode() != null) {
			norskPostadresse.setLand(landkodeService.finnLandnavn(geografiskAdresse.getLandkode().getKodeRef()));
		}
	}
	
	private void mapForretningsAdresse(OrganisasjonsDetaljer orgDet, NorskPostadresse norskPostadresse) {
		if (orgDet.getForretningsadresse().get(0) instanceof SemistrukturertAdresse) {
			SemistrukturertAdresse adresse = (SemistrukturertAdresse) orgDet.getForretningsadresse().get(0);
			settAdresseledd(adresse, norskPostadresse);
			if (norskPostadresse.getPostnummer() != null) {
				norskPostadresse.setPoststed(postnummerService.finnPoststed(norskPostadresse.getPostnummer()));
			}
		} else {
			Gateadresse gateadresse = (Gateadresse) orgDet.getForretningsadresse().get(0);
			norskPostadresse.setAdresselinje1(Optional.ofNullable(gateadresse.getGatenavn())
					.orElse("") + " " + Optional.ofNullable(gateadresse.getHusnummer() == null ? null : gateadresse.getHusnummer()
					.toString())
					.orElse("") + Optional.ofNullable(gateadresse.getHusbokstav()).orElse(""));
			if (orgDet.getForretningsadresse().get(0) instanceof StrukturertAdresse) {
				StedsadresseNorge stedsadresseNorge = (StedsadresseNorge) orgDet.getForretningsadresse().get(0);
				if (stedsadresseNorge.getPoststed() != null) {
					norskPostadresse.setPostnummer(stedsadresseNorge.getPoststed().getKodeRef());
					norskPostadresse.setPoststed(postnummerService.finnPoststed(stedsadresseNorge.getPoststed().getKodeRef()));
				}
			}
		}

		GeografiskAdresse geografiskAdresse = orgDet.getForretningsadresse().get(0);
		if (geografiskAdresse.getLandkode() != null) {
			norskPostadresse.setLand(landkodeService.finnLandnavn(geografiskAdresse.getLandkode().getKodeRef()));
		}
		
	}
	
	private void settAdresseledd(SemistrukturertAdresse adresse, NorskPostadresse norskPostadresse) {
		for (NoekkelVerdiAdresse nokler : adresse.getAdresseledd()) {
			if ("adresselinje1".equals(nokler.getNoekkel().getKodeRef())) {
				norskPostadresse.setAdresselinje1(nokler.getVerdi());
			} else if ("adresselinje2".equals(nokler.getNoekkel().getKodeRef())) {
				norskPostadresse.setAdresselinje2(nokler.getVerdi());
			} else if ("Adresse 3 split 1".equals(nokler.getNoekkel().getKodeRef())) {
				norskPostadresse.setAdresselinje3(nokler.getVerdi());
			} else if ("postnr".equals(nokler.getNoekkel().getKodeRef())) {
				norskPostadresse.setPostnummer(nokler.getVerdi());
			} else if ("poststed".equals(nokler.getNoekkel().getKodeRef())) {
				norskPostadresse.setPoststed(nokler.getVerdi());
			}
		}
	}

	
}