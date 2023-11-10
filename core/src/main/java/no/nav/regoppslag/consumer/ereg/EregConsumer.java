package no.nav.regoppslag.consumer.ereg;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.config.properties.RegoppslagProperties;
import no.nav.regoppslag.consumer.ereg.support.Organisasjon;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagIkkeFunnetException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.metrics.Metrics;
import org.slf4j.MDC;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.UUID;
import java.util.regex.Pattern;

import static no.nav.regoppslag.config.cache.CacheConfig.HENT_ORGANISASJON;
import static no.nav.regoppslag.metrics.MetricLabels.DOK_CONSUMER;
import static no.nav.regoppslag.metrics.MetricLabels.PROCESS_CODE;
import static no.nav.regoppslag.util.MDCConstants.CALL_ID;
import static no.nav.regoppslag.util.NavHeaders.NAV_CALL_ID;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
@Service
public class EregConsumer {
	static final Pattern ORGNUMMER_PATTERN = Pattern.compile("^\\d{9}$");
	private final RestTemplate restTemplate;
	private final String eregUrl;

	public EregConsumer(RestTemplateBuilder restTemplateBuilder,
						RegoppslagProperties regoppslagProperties) {
		this.eregUrl = regoppslagProperties.getEndpoints().getEreg().getUrl();
		this.restTemplate = restTemplateBuilder
				.setReadTimeout(Duration.ofSeconds(20))
				.setConnectTimeout(Duration.ofSeconds(5))
				.build();
	}

	@Retryable(retryFor = HttpServerErrorException.class, noRetryFor = {HttpClientErrorException.class}, maxAttempts = 5, backoff = @Backoff(delay = 200))
	@Metrics(value = DOK_CONSUMER, extraTags = {PROCESS_CODE, HENT_ORGANISASJON}, percentiles = {0.5, 0.95}, histogram = true)
	public Organisasjon hentOrganisasjon(String organisasjonsnummer) {
		if (organisasjonsnummer == null || !ORGNUMMER_PATTERN.matcher(organisasjonsnummer).matches()) {
			throw new RegOppslagFunctionalException("Kan ikke slå opp i ereg. organisasjonsnummer='" + organisasjonsnummer + "' er ikke 9 siffer", BAD_REQUEST);
		}

		HttpHeaders headers = new HttpHeaders();
		headers.set(NAV_CALL_ID, getCallId());

		var uri = UriComponentsBuilder.fromHttpUrl(eregUrl)
				.path(organisasjonsnummer)
				.build()
				.toUri();

		try {
			HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
			ResponseEntity<Organisasjon> organisasjonResponseEntity = this.restTemplate.exchange(uri, GET, httpEntity, Organisasjon.class);
			return organisasjonResponseEntity.getBody();
		} catch (HttpClientErrorException.NotFound e) {
			throw new RegOppslagIkkeFunnetException("Fant ikke Organisasjon med organisasjonsnummer=" + organisasjonsnummer, e.getStatusCode());
		} catch (HttpClientErrorException e) {
			throw new RegOppslagFunctionalException("Funksjonell feil mot hentOrganisasjon for organisasjon med organisasjonsnummer=" + organisasjonsnummer, e.getStatusCode());
		} catch (HttpServerErrorException e) {
			throw new RegOppslagTechnicalException("Teknisk feil mot hentOrganisasjon for organisasjon med organisasjonsnummer=" + organisasjonsnummer, e);
		}
	}

	private String getCallId() {
		String callId = MDC.get(CALL_ID);
		if (isBlank(callId)) {
			return UUID.randomUUID().toString();
		} else {
			return callId;
		}
	}
}
