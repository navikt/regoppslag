package no.nav.regoppslag.norg2;

import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v2.HentKontaktinformasjonForEnhetBolkUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v2.OrganisasjonEnhetKontaktinformasjonV2;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v2.meldinger.WSHentKontaktinformasjonForEnhetBolkRequest;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v2.meldinger.WSHentKontaktinformasjonForEnhetBolkResponse;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Ketill Fenne, Visma Consulting
 */
@Service
public class OrganisasjonEnhetKontaktinformasjonV2Consumer {


	private OrganisasjonEnhetKontaktinformasjonV2 organisasjonEnhetKontaktinformasjonV2;

	@Inject
	public OrganisasjonEnhetKontaktinformasjonV2Consumer(OrganisasjonEnhetKontaktinformasjonV2 organisasjonEnhetKontaktinformasjonV2) {
		this.organisasjonEnhetKontaktinformasjonV2 = organisasjonEnhetKontaktinformasjonV2;
	}

	public String hentEnhetNavn(String enhetNr) {
		try {
			WSHentKontaktinformasjonForEnhetBolkResponse response = organisasjonEnhetKontaktinformasjonV2.hentKontaktinformasjonForEnhetBolk(mapEnhetNr(enhetNr));
			return mapHentKontaktinformasjonForEnhetBolkResponse(response);
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

	private String mapHentKontaktinformasjonForEnhetBolkResponse(WSHentKontaktinformasjonForEnhetBolkResponse response) {
		if (response == null || response.getEnhetListe().isEmpty()) {
			return null;
		} else {
			return response.getEnhetListe().get(0).getEnhetNavn();
		}
	}
}
