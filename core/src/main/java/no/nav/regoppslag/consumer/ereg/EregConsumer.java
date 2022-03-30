package no.nav.regoppslag.consumer.ereg;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.consumer.ereg.support.Organisasjon;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagIkkeFunnetException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.metrics.MetricLabels;
import no.nav.regoppslag.metrics.Metrics;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.time.Duration;

import static no.nav.regoppslag.metrics.MetricLabels.DOK_CONSUMER;
import static no.nav.regoppslag.metrics.MetricLabels.PROCESS_CODE;
import static no.nav.regoppslag.util.MDCConstants.*;

@Slf4j
@Service
public class EregConsumer {

	private final RestTemplate restTemplate;
	private final String eregUrl;
	private MicrometerMetrics metrics;

	@Inject
	public EregConsumer(@Value("${ereg-organisasjon-service.url}") String eregUrl,
						   RestTemplateBuilder restTemplateBuilder,
						MicrometerMetrics metrics) {
		this.eregUrl = eregUrl;
		this.restTemplate = restTemplateBuilder
				.setReadTimeout(Duration.ofSeconds(20))
				.setConnectTimeout(Duration.ofSeconds(5))
				.build();
		this.metrics = metrics;
	}

	@Cacheable(value = MetricLabels.HENT_ORGANISASJON, key = "#organisasjonsNummer")
	@Retryable(include = HttpServerErrorException.class, exclude = {HttpClientErrorException.class}, maxAttempts = 5, backoff = @Backoff(delay = 200))
	@Metrics(value = DOK_CONSUMER, extraTags = {PROCESS_CODE, MetricLabels.HENT_ORGANISASJON}, percentiles = {0.5, 0.95}, histogram = true)
	public Organisasjon hentOrganisasjon(String organisasjonsNummer) {

		HttpHeaders headers = new HttpHeaders();
		headers.set(NAV_CALL_ID, MDC.get(CALL_ID));
		headers.set(NAV_CONSUMER_ID, MDC.get(CONSUMER_ID));

		metrics.cacheMiss(MetricLabels.HENT_ORGANISASJON);

		try {
			HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
			ResponseEntity<Organisasjon> organisasjonResponseEntity = this.restTemplate.exchange(this.eregUrl + organisasjonsNummer, HttpMethod.GET, httpEntity, Organisasjon.class);
			return organisasjonResponseEntity.getBody();
		} catch (HttpClientErrorException.NotFound e) {
			throw new RegOppslagIkkeFunnetException("Fant ikke Organisasjon med organisasjonsnummer=" + organisasjonsNummer, e.getStatusCode());
		} catch (HttpClientErrorException e) {
			throw new RegOppslagFunctionalException("Funksjonell feil mot hentOrganisasjon for organisasjon med organisasjonsnummer=" + organisasjonsNummer, e.getStatusCode());
		} catch (HttpServerErrorException e) {
			throw new RegOppslagTechnicalException("Teknisk feil mot hentOrganisasjon for organisasjon med organisasjonsnummer=" + organisasjonsNummer, e);
		}
	}

}
