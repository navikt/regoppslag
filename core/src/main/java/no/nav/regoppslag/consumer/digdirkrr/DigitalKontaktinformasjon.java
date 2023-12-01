package no.nav.regoppslag.consumer.digdirkrr;

import no.nav.regoppslag.config.properties.RegoppslagProperties;
import no.nav.regoppslag.config.properties.RegoppslagProperties.Oauth2SecuredEndpoint;
import no.nav.regoppslag.consumer.azure.AzureTokenConsumer;
import no.nav.regoppslag.exceptions.DigitalKontaktinformasjonFunctionalException;
import no.nav.regoppslag.exceptions.DigitalKontaktinformasjonTechnicalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;
import no.nav.regoppslag.metrics.Metrics;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static no.nav.regoppslag.metrics.MetricLabels.DOK_CONSUMER;
import static no.nav.regoppslag.metrics.MetricLabels.PROCESS_CODE;
import static no.nav.regoppslag.util.MDCConstants.CALL_ID;
import static no.nav.regoppslag.util.NavHeaders.NAV_CALL_ID;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
public class DigitalKontaktinformasjon {

	static final String HEADER_NAV_PERSONIDENTER = "Nav-Personidenter";
	private final RestTemplate restTemplate;
	private final AzureTokenConsumer azureTokenConsumer;
	private final Oauth2SecuredEndpoint digdirkrrproxy;

	public static final String HENT_SIKKER_DIGITAL_POSTADRESSE = "hentSikkerDigitalPostadresse";

	@Autowired
	public DigitalKontaktinformasjon(RestTemplateBuilder restTemplateBuilder,
									 RegoppslagProperties regoppslagProperties,
									 AzureTokenConsumer azureTokenConsumer) {
		this.digdirkrrproxy = regoppslagProperties.getEndpoints().getDigdirkrrproxy();
		this.azureTokenConsumer = azureTokenConsumer;
		this.restTemplate = restTemplateBuilder
				.setReadTimeout(Duration.ofSeconds(20))
				.setConnectTimeout(Duration.ofSeconds(5))
				.build();
	}

	@Retryable(retryFor = RegOppslagTechnicalException.class, maxAttempts = 5, backoff = @Backoff(delay = 200))
	@Metrics(value = DOK_CONSUMER, extraTags = {PROCESS_CODE, HENT_SIKKER_DIGITAL_POSTADRESSE}, percentiles = {0.5, 0.95}, histogram = true)
	public String hentSpraak(final String personidentifikator, final boolean inkluderSikkerDigitalPost) throws DigitalKontaktinformasjonFunctionalException {
		HttpHeaders headers = createHeaders();

		if (isBlank(personidentifikator)) {
			throw new RegoppslagIllegalArgumentException("Personidentifikator kan ikke være null", BAD_REQUEST);
		}

		final String fnrTrimmed = personidentifikator.trim();
		headers.add(HEADER_NAV_PERSONIDENTER, fnrTrimmed);

		try {
			PostPersonerRequest postPersonRequest = PostPersonerRequest.builder().personidenter(List.of(fnrTrimmed)).build();
			HttpEntity<String> request = new HttpEntity(postPersonRequest, headers);
			DkifResponse response = restTemplate.postForEntity(digdirkrrproxy.getUrl() + "/rest/v1/personer?inkluderSikkerDigitalPost=" + inkluderSikkerDigitalPost, request, DkifResponse.class).getBody();

			String spraak = isValidRespons(response, fnrTrimmed) ? mapSpraak(response.getKontaktinfo().get(fnrTrimmed)) : null;
			return isBlank(spraak) ? null : spraak.toUpperCase();

		} catch (HttpClientErrorException e) {
			throw new DigitalKontaktinformasjonFunctionalException(format("Funksjonell feil ved kall mot Digdir KRR. Feilmelding=%s", e
					.getMessage()), e.getCause(), "DKIF", e.getStatusCode());
		} catch (HttpServerErrorException e) {
			throw new DigitalKontaktinformasjonTechnicalException(format("Teknisk feil ved kall mot DigitalKontaktinformasjon.kontaktinformasjon. Feilmelding=%s", e.getMessage()), e);
		}
	}

	private boolean isValidRespons(DkifResponse response, String fnr) {
		return response != null && response.getKontaktinfo() != null && response.getKontaktinfo().get(fnr) != null;
	}

	private String mapSpraak(DkifResponse.DigitalKontaktinfo digitalKontaktinfo) {
		if (digitalKontaktinfo == null) {
			return null;
		}
		return digitalKontaktinfo.getSpraak();
	}

	private HttpHeaders createHeaders() {
		String clientCredentialToken = azureTokenConsumer.getClientCredentialToken(digdirkrrproxy.getScope());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(APPLICATION_JSON);
		headers.setBearerAuth(clientCredentialToken);
		headers.add(NAV_CALL_ID, getCallId());
		return headers;
	}

	private String getCallId() {
		return isBlank(MDC.get(CALL_ID)) ? UUID.randomUUID().toString() : MDC.get(CALL_ID);
	}
}
