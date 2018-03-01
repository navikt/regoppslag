package no.nav.regoppslag.consumer.norg2;

import lombok.extern.slf4j.Slf4j;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v2.HentKontaktinformasjonForEnhetBolkUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v2.OrganisasjonEnhetKontaktinformasjonV2;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v2.informasjon.WSFeiletEnhet;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v2.meldinger.WSHentKontaktinformasjonForEnhetBolkRequest;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v2.meldinger.WSHentKontaktinformasjonForEnhetBolkResponse;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Ketill Fenne, Visma Consulting
 */
@Slf4j
@Service
public class OrganisasjonEnhetKontaktinformasjonV2Consumer {

	private final OrganisasjonEnhetKontaktinformasjonV2 organisasjonEnhetKontaktinformasjonV2;

	public static final String HENT_ENHET_NAVN = "hentEnhetNavn";

	@Inject
	public OrganisasjonEnhetKontaktinformasjonV2Consumer(OrganisasjonEnhetKontaktinformasjonV2 organisasjonEnhetKontaktinformasjonV2) {
		this.organisasjonEnhetKontaktinformasjonV2 = organisasjonEnhetKontaktinformasjonV2;
	}

	public String hentEnhetNavn(String enhetNr) {
		try {
			WSHentKontaktinformasjonForEnhetBolkResponse response = organisasjonEnhetKontaktinformasjonV2.hentKontaktinformasjonForEnhetBolk(mapEnhetNr(enhetNr));
			return mapHentKontaktinformasjonForEnhetBolkResponse(response, enhetNr);
		} catch (HentKontaktinformasjonForEnhetBolkUgyldigInput hentKontaktinformasjonForEnhetBolkUgyldigInput) {
			hentKontaktinformasjonForEnhetBolkUgyldigInput.printStackTrace();
			return null;
		}
	}

	private WSHentKontaktinformasjonForEnhetBolkRequest mapEnhetNr(String enhetNummer) {
		WSHentKontaktinformasjonForEnhetBolkRequest request = new WSHentKontaktinformasjonForEnhetBolkRequest();
		request.getEnhetIdListe().add(enhetNummer);
		return request;
	}

	private String mapHentKontaktinformasjonForEnhetBolkResponse(WSHentKontaktinformasjonForEnhetBolkResponse response, String enhetNr) {
		if (response != null && response.getEnhetListe().size() == 1) {
			return response.getEnhetListe().get(0).getEnhetNavn();
		} else if (response != null && !response.getFeiletEnhetListe().isEmpty()) {
			logFeilmelding(response, enhetNr);
		}
		return null;
	}

	private void logFeilmelding(WSHentKontaktinformasjonForEnhetBolkResponse response, String enhetNummer) {
		for (WSFeiletEnhet feil : response.getFeiletEnhetListe()) {
			log.info("Enhet finnes ikke for enhetnummer={}, message={}",
					enhetNummer, feil.getFeilmelding());
		}
	}
}
