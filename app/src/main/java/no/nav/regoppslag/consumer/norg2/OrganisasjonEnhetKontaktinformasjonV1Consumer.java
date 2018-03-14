package no.nav.regoppslag.consumer.norg2;

import static no.nav.regoppslag.metrics.PrometheusLabels.SERVICE_CODE_TREG001;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestLatency;

import io.prometheus.client.Histogram;
import lombok.extern.slf4j.Slf4j;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.binding.HentKontaktinformasjonForEnhetBolkUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.binding.OrganisasjonEnhetKontaktinformasjonV1;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.FeiletEnhet;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.Organisasjonsenhet;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.meldinger.HentKontaktinformasjonForEnhetBolkRequest;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.meldinger.HentKontaktinformasjonForEnhetBolkResponse;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Ketill Fenne, Visma Consulting
 */
@Slf4j
@Service
public class OrganisasjonEnhetKontaktinformasjonV1Consumer {

	private final OrganisasjonEnhetKontaktinformasjonV1 organisasjonEnhetKontaktinformasjonV1;
	private Histogram.Timer requestTimer;

	public static final String HENT_ENHET_NAVN = "hentEnhetNavn";

	@Inject
	public OrganisasjonEnhetKontaktinformasjonV1Consumer(OrganisasjonEnhetKontaktinformasjonV1 organisasjonEnhetKontaktinformasjonV1) {
		this.organisasjonEnhetKontaktinformasjonV1 = organisasjonEnhetKontaktinformasjonV1;
	}

	public Organisasjonsenhet hentKontaktinformasjonForEnhet(String enhetNr) {
		try {
			requestTimer = requestLatency.labels(SERVICE_CODE_TREG001, "NORG2", "hentKontaktinformasjonForEnhetBolk").startTimer();
			
			HentKontaktinformasjonForEnhetBolkResponse response = organisasjonEnhetKontaktinformasjonV1.hentKontaktinformasjonForEnhetBolk(mapEnhetNr(enhetNr));
			return mapHentKontaktinformasjonForEnhetBolkResponse(response, enhetNr);
		} catch (HentKontaktinformasjonForEnhetBolkUgyldigInput hentKontaktinformasjonForEnhetBolkUgyldigInput) {
			log.info("Nav enhet finnes ikke for enhetNr={}, message={}", enhetNr, hentKontaktinformasjonForEnhetBolkUgyldigInput.getMessage());
			return null;
		} finally {
			requestTimer.observeDuration();
		}
	}

	private HentKontaktinformasjonForEnhetBolkRequest mapEnhetNr(String enhetNummer) {
		HentKontaktinformasjonForEnhetBolkRequest request = new HentKontaktinformasjonForEnhetBolkRequest();
		request.getEnhetIdListe().add(enhetNummer);
		return request;
	}

	private Organisasjonsenhet mapHentKontaktinformasjonForEnhetBolkResponse(HentKontaktinformasjonForEnhetBolkResponse response, String enhetNr) {
		if (response != null && response.getEnhetListe().size() == 1) {
			return response.getEnhetListe().get(0);
		} else if (response != null && !response.getFeiletEnhetListe().isEmpty()) {
			logFeilmelding(response, enhetNr);
		}
		return null;
	}

	private void logFeilmelding(HentKontaktinformasjonForEnhetBolkResponse response, String enhetNummer) {
		for (FeiletEnhet feil : response.getFeiletEnhetListe()) {
			log.info("Enhet finnes ikke for enhetnummer={}, message={}",
					enhetNummer, feil.getFeilmelding());
		}
	}
}
