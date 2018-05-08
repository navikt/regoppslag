package no.nav.regoppslag.consumer.organisasjonv4;

import static no.nav.regoppslag.metrics.PrometheusLabels.CACHE_COUNTER;
import static no.nav.regoppslag.metrics.PrometheusLabels.CACHE_MISS;
import static no.nav.regoppslag.metrics.PrometheusLabels.ORGANISASJONV4;
import static no.nav.regoppslag.metrics.PrometheusMetrics.getConsumerId;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestCounter;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestLatency;

import io.prometheus.client.Histogram;
import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
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
	public static final String ORGV4_UGYLDIG_INPUT = "OrganisasjonV4 - Ugyldig input";
	public static final String ORGV4_ORG_IKKE_FUNNET = "OrganisasjonV4 - Organisasjon ikke funnet";
	
	
	@Inject
	public OrganisasjonV4Consumer(OrganisasjonV4 organisasjonV4) {
		this.organisasjonV4 = organisasjonV4;
	}
	
	@Cacheable(HENT_ORGANISASJON)
	@Retryable(include = RegOppslagTechnicalException.class, exclude = {RegOppslagFunctionalException.class}, maxAttempts = 5, backoff = @Backoff(delay = 200))
	public Organisasjon hentOrganisasjon(final String organisasjonsnummer, final String serviceCode) throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		
		requestCounter.labels(serviceCode, HENT_ORGANISASJON, CACHE_COUNTER, getConsumerId(), CACHE_MISS).inc();
		
		try {
			HentOrganisasjonRequest request = mapHentNoekkelinfoOrganisasjonRequest(organisasjonsnummer);
			requestTimer = requestLatency.labels(serviceCode, ORGANISASJONV4, HENT_ORGANISASJON).startTimer();
			HentOrganisasjonResponse response = organisasjonV4.hentOrganisasjon(request);
			return mapHentOrganisasjonResponse(response);
		} catch (HentOrganisasjonOrganisasjonIkkeFunnet | HentOrganisasjonUgyldigInput e) {
			throw new RegOppslagFunctionalException(String.format("Nav enhet finnes ikke for enhetNr=%s, message=%s", organisasjonsnummer, e
					.getMessage()), e, e.getClass()
					.equals(HentOrganisasjonOrganisasjonIkkeFunnet.class) ? ORGV4_ORG_IKKE_FUNNET : ORGV4_UGYLDIG_INPUT);
		} catch (Exception e) {
			throw new RegOppslagTechnicalException(String.format("Noe gikk galt i kall til OrganisasjonV4.hentOrganisasjon for enhetNr=%s, message=%s", organisasjonsnummer, e
					.getMessage()), e, "OrganisasjonV4 - Teknisk feil");
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
