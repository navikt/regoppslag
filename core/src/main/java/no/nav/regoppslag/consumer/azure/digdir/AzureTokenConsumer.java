package no.nav.regoppslag.consumer.azure.digdir;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import no.nav.regoppslag.config.RegoppslagProperties;
import no.nav.regoppslag.consumer.azure.AzureTokenException;
import no.nav.regoppslag.consumer.azure.TokenConsumer;
import no.nav.regoppslag.consumer.azure.TokenResponse;
import org.apache.http.HttpHost;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Collections;

import static no.nav.regoppslag.metrics.MetricLabels.AZURE_CLIENT_CREDENTIAL_DIGDIR_TOKEN_CACHE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;


@Component
@Profile({"nais", "local"})
public class AzureTokenConsumer implements TokenConsumer {
	private static final String AZURE_TOKEN_INSTANCE = "azuretoken";
	private final RestTemplate restTemplate;
	private final AzureProperties azureProperties;

	public AzureTokenConsumer(AzureProperties azureProperties,
							  RestTemplateBuilder restTemplateBuilder,
							  HttpClientConnectionManager httpClientConnectionManager,
							  RegoppslagProperties regoppslagProperties) {
		final CloseableHttpClient httpClient = createHttpClient(regoppslagProperties.getProxy(), httpClientConnectionManager);
		this.restTemplate = restTemplateBuilder
				.setConnectTimeout(Duration.ofSeconds(3))
				.setReadTimeout(Duration.ofSeconds(20))
				.requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient))
				.build();
		this.azureProperties = azureProperties;
	}

	private CloseableHttpClient createHttpClient(RegoppslagProperties.Proxy proxy,
												 HttpClientConnectionManager httpClientConnectionManager) {
		if (proxy.isSet()) {
			final HttpHost proxyHost = new HttpHost(proxy.getHost(), proxy.getPort());
			return HttpClients.custom()
					.setRoutePlanner(new DefaultProxyRoutePlanner(proxyHost))
					.setConnectionManager(httpClientConnectionManager)
					.build();
		} else {
			return HttpClients.custom()
					.setConnectionManager(httpClientConnectionManager)
					.build();
		}
	}


	@Retry(name = AZURE_TOKEN_INSTANCE)
	@CircuitBreaker(name = AZURE_TOKEN_INSTANCE)
	@Cacheable(AZURE_CLIENT_CREDENTIAL_DIGDIR_TOKEN_CACHE)
	public TokenResponse getClientCredentialToken(String scope) {
		try {
			HttpHeaders headers = createHeaders();
			String form = "grant_type=client_credentials&scope=" + scope + "&client_id=" +
					azureProperties.getClientId() + "&client_secret=" + azureProperties.getClientSecret();
			HttpEntity<String> requestEntity = new HttpEntity<>(form, headers);

			return restTemplate.exchange(azureProperties.getTokenUrl(), POST, requestEntity, TokenResponse.class)
					.getBody();
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			throw new AzureTokenException(String.format("Klarte ikke hente token fra Azure. Feilet med httpstatus=%s. Feilmelding=%s", e.getStatusCode(), e.getMessage()), e);
		}
	}

	private HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(APPLICATION_FORM_URLENCODED);
		headers.setAccept(Collections.singletonList(APPLICATION_JSON));
		return headers;
	}
}