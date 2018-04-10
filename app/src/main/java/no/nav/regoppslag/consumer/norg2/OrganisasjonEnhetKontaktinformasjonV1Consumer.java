package no.nav.regoppslag.consumer.norg2;

import static no.nav.regoppslag.metrics.PrometheusLabels.CACHE_HIT;
import static no.nav.regoppslag.metrics.PrometheusLabels.CACHE_MISS;
import static no.nav.regoppslag.metrics.PrometheusLabels.SERVICE_CODE_TREG001;
import static no.nav.regoppslag.metrics.PrometheusMetrics.cacheCounter;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestLatency;

import io.prometheus.client.Histogram;
import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.binding.HentKontaktinformasjonForEnhetBolkUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.binding.OrganisasjonEnhetKontaktinformasjonV1;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.Organisasjonsenhet;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.meldinger.HentKontaktinformasjonForEnhetBolkRequest;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.meldinger.HentKontaktinformasjonForEnhetBolkResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
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

	@Cacheable(HENT_ENHET_NAVN)
	@Retryable(include = RegOppslagTechnicalException.class, exclude = {RegOppslagFunctionalException.class }, maxAttempts = 5, backoff = @Backoff(delay = 200))
	public Organisasjonsenhet hentKontaktinformasjonForEnhet(String enhetNr) throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		
		cacheCounter.labels(HENT_ENHET_NAVN, "OrganisasjonEnhetKontaktinformasjonV1", CACHE_HIT).dec();
		cacheCounter.labels(HENT_ENHET_NAVN, "OrganisasjonEnhetKontaktinformasjonV1", CACHE_MISS).inc();
		
		try {
			requestTimer = requestLatency.labels(SERVICE_CODE_TREG001, "NORG2", "hentKontaktinformasjonForEnhetBolk").startTimer();
			
			HentKontaktinformasjonForEnhetBolkResponse response = organisasjonEnhetKontaktinformasjonV1.hentKontaktinformasjonForEnhetBolk(mapEnhetNr(enhetNr));
			return mapHentKontaktinformasjonForEnhetBolkResponse(response, enhetNr);
		} catch (HentKontaktinformasjonForEnhetBolkUgyldigInput hentKontaktinformasjonForEnhetBolkUgyldigInput) {
			throw new RegOppslagFunctionalException("Nav enhet finnes ikke for enhetNr=" + enhetNr + ", message=" + hentKontaktinformasjonForEnhetBolkUgyldigInput.getMessage(), hentKontaktinformasjonForEnhetBolkUgyldigInput);
		} catch (Exception e) {
			throw new RegOppslagTechnicalException("Noe gikk galt i kall til Norg for enhetNr=" + enhetNr + ", message=" + e.getMessage());
		} finally {
			requestTimer.observeDuration();
		}
	}

	private HentKontaktinformasjonForEnhetBolkRequest mapEnhetNr(String enhetNummer) {
		HentKontaktinformasjonForEnhetBolkRequest request = new HentKontaktinformasjonForEnhetBolkRequest();
		request.getEnhetIdListe().add(enhetNummer);
		return request;
	}

	private Organisasjonsenhet mapHentKontaktinformasjonForEnhetBolkResponse(HentKontaktinformasjonForEnhetBolkResponse response, String enhetNr) throws HentKontaktinformasjonForEnhetBolkUgyldigInput {
		if (response != null && response.getEnhetListe().size() == 1) {
			return response.getEnhetListe().get(0);
		} else if (response != null && !response.getFeiletEnhetListe().isEmpty()) {
			throw new HentKontaktinformasjonForEnhetBolkUgyldigInput("Nav enhet finnes ikke for enhetNr=" + enhetNr, null);
		}
		return null;
	}
}
