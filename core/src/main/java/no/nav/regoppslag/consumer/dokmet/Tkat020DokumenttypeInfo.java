package no.nav.regoppslag.consumer.dokmet;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokmet.api.tkat020.DokumenttypeInfoTo;
import no.nav.dokmet.api.tkat020.SpraakInfoTo;
import no.nav.regoppslag.config.properties.RegoppslagProperties;
import no.nav.regoppslag.consumer.azure.TokenConsumer;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.metrics.Metrics;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import org.slf4j.MDC;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static no.nav.regoppslag.config.cache.CacheConfig.HENT_DOKMET_SPRAAKINFO;
import static no.nav.regoppslag.metrics.MetricLabels.DOK_CONSUMER;
import static no.nav.regoppslag.metrics.MetricLabels.PROCESS_CODE;
import static no.nav.regoppslag.util.MDCConstants.APP_NAME;
import static no.nav.regoppslag.util.MDCConstants.CALL_ID;
import static no.nav.regoppslag.util.NavHeaders.NAV_CALLID;
import static no.nav.regoppslag.util.NavHeaders.NAV_CONSUMER_ID;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Service
@Slf4j
public class Tkat020DokumenttypeInfo {

	private static final String TKAT020_TEKNISKFEIL = "TKAT020 - Teknisk feil";
	private static final String TKAT020_INGEN_TREFF = "TKAT020 - Ingen treff";

	private final RestTemplate restTemplate;
	private final MicrometerMetrics metrics;
	private final TokenConsumer tokenConsumer;
	private final RegoppslagProperties.Oauth2SecuredEndpoint dokmet;

	public Tkat020DokumenttypeInfo(RestTemplateBuilder restTemplateBuilder,
								   HttpComponentsClientHttpRequestFactory requestFactory,
								   RegoppslagProperties regoppslagProperties,
								   MicrometerMetrics metrics,
								   TokenConsumer tokenConsumer) {
		this.tokenConsumer = tokenConsumer;
		this.dokmet = regoppslagProperties.getEndpoints().getDokmet();
		this.restTemplate = restTemplateBuilder
				.requestFactory(requestFactory.getClass())
				.rootUri(this.dokmet.getUrl())
				.setConnectTimeout(Duration.ofSeconds(3))
				.setReadTimeout(Duration.ofSeconds(10))
				.build();
		this.metrics = metrics;
	}

	@Cacheable(value = HENT_DOKMET_SPRAAKINFO, key = "#dokumenttypeId")
	@Retryable(include = RegOppslagTechnicalException.class, exceptionExpression = "HttpStatus.NOT_FOUND != getHttpStatus()", backoff = @Backoff(delay = 200))
	@Metrics(value = DOK_CONSUMER, extraTags = {PROCESS_CODE, HENT_DOKMET_SPRAAKINFO}, percentiles = {0.5, 0.95}, histogram = true)
	public List<SpraakInfoTo> hentDokumenttypeInfoSpraak(final String dokumenttypeId) throws RegOppslagTechnicalException {
		HttpHeaders headers = createHeaders();

		metrics.cacheMiss(HENT_DOKMET_SPRAAKINFO);
		try {
			HttpEntity<String> request = new HttpEntity(headers);

			DokumenttypeInfoTo response = restTemplate.exchange(dokmet.getUrl() + "/" + dokumenttypeId, GET, request, DokumenttypeInfoTo.class).getBody();
			if (response.getDokumentProduksjonsInfo() == null || response.getDokumentProduksjonsInfo().getSpraakInfos() == null) {
				return Collections.emptyList();
			} else {
				return response.getDokumentProduksjonsInfo().getSpraakInfos();
			}
		} catch (HttpClientErrorException e) {
			//Kaster teknisk feil fordi manglende dokumenttypeId på prod databasen betyr at det er noe feil på vår side som må fikses.
			throw new RegOppslagTechnicalException(String.format("TKAT020 feilet med statusKode=%s. Fant ingen dokumenttypeInfo med dokumenttypeId=%s. ", e
					.getStatusCode(), dokumenttypeId), e, TKAT020_INGEN_TREFF, INTERNAL_SERVER_ERROR);
		} catch (HttpServerErrorException e) {
			throw new RegOppslagTechnicalException(String.format("TKAT020 feilet teknisk med statusKode=%s for dokumenttypeId=%s. Feilmelding=%s", e
					.getStatusCode(), dokumenttypeId, e.getMessage()), e, TKAT020_TEKNISKFEIL, e.getStatusCode());
		}
	}

	private HttpHeaders createHeaders() {
		String clientCredentialToken = tokenConsumer.getClientCredentialToken(dokmet.getScope());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(APPLICATION_JSON);
		headers.setBearerAuth(clientCredentialToken);
		headers.add(NAV_CONSUMER_ID, APP_NAME);
		headers.add(NAV_CALLID, MDC.get(CALL_ID));
		return headers;
	}
}