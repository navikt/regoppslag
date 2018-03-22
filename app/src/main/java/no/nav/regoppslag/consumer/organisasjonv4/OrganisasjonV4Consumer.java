package no.nav.regoppslag.consumer.organisasjonv4;

import static no.nav.regoppslag.metrics.PrometheusLabels.CACHE_HIT;
import static no.nav.regoppslag.metrics.PrometheusLabels.CACHE_MISS;
import static no.nav.regoppslag.metrics.PrometheusLabels.SERVICE_CODE_TREG001;
import static no.nav.regoppslag.metrics.PrometheusMetrics.cacheCounter;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestLatency;

import io.prometheus.client.Histogram;
import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonOrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.OrganisasjonV4;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonRequest;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Ketill Fenne, Visma Consulting AS
 */
@Slf4j
@Service
public class OrganisasjonV4Consumer {

	private final OrganisasjonV4 organisasjonV4;
	private Histogram.Timer requestTimer;
	
	public static final String HENT_ORGANISASJON = "hentOrganisasjon";
	
	@Inject
	public OrganisasjonV4Consumer(OrganisasjonV4 organisasjonV4) {
		this.organisasjonV4 = organisasjonV4;
	}

	@Cacheable(HENT_ORGANISASJON)
	@Retryable(maxAttempts = 5, backoff = @Backoff(delay = 200), include = Exception.class, exclude = {RegOppslagFunctionalException.class })
	public Organisasjon hentOrganisasjon(final String organisasjonsnummer) throws RegOppslagFunctionalException {
		
		cacheCounter.labels(HENT_ORGANISASJON, "OrganisasjonV4", CACHE_HIT).dec();
		cacheCounter.labels(HENT_ORGANISASJON, "OrganisasjonV4", CACHE_MISS).inc();
		
		try {
			HentOrganisasjonRequest request = mapHentNoekkelinfoOrganisasjonRequest(organisasjonsnummer);
			requestTimer = requestLatency.labels(SERVICE_CODE_TREG001, "OrganisasjonV4", "hentOrganisasjon").startTimer();
			HentOrganisasjonResponse response = organisasjonV4.hentOrganisasjon(request);
			return mapHentOrganisasjonResponse(response);
		} catch (HentOrganisasjonOrganisasjonIkkeFunnet | HentOrganisasjonUgyldigInput e) {
			throw new RegOppslagFunctionalException("Nav enhet finnes ikke for enhetNr=" + organisasjonsnummer + ", message=" + e.getMessage(), e);
		} finally {
			requestTimer.observeDuration();
		}
	}

	private HentOrganisasjonRequest mapHentNoekkelinfoOrganisasjonRequest(String avsenderId) {
		HentOrganisasjonRequest request = new HentOrganisasjonRequest();
		request.setOrgnummer(avsenderId);
		return request;
	}

	private Organisasjon mapHentOrganisasjonResponse(HentOrganisasjonResponse response) {
		if (response == null || response.getOrganisasjon() == null) {
			return null;
		}
		return response.getOrganisasjon();
	}
}
