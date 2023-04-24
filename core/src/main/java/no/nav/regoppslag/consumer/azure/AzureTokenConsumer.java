package no.nav.regoppslag.consumer.azure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static no.nav.regoppslag.config.cache.CacheConfig.AZURE_CLIENT_CREDENTIAL_TOKEN;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@Component
public class AzureTokenConsumer implements TokenConsumer {

	private static final String AZURE_TOKEN_INSTANCE = "azuretoken";

	private final AzureProperties azureProperties;
	private final ObjectMapper objectMapper;
	private final WebClient webClient;

	@Autowired
	public AzureTokenConsumer(AzureProperties azureProperties,
							  ObjectMapper objectMapper,
							  WebClient webClient) {
		this.azureProperties = azureProperties;
		this.objectMapper = objectMapper;
		this.webClient = webClient.mutate()
				.defaultHeader(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
				.baseUrl(azureProperties.getOpenidConfigTokenEndpoint())
				.build();
	}

	@Override
	@Retry(name = AZURE_TOKEN_INSTANCE)
	@CircuitBreaker(name = AZURE_TOKEN_INSTANCE)
	@Cacheable(value = AZURE_CLIENT_CREDENTIAL_TOKEN)
	public String getClientCredentialToken(String scope) {

		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("client_id", azureProperties.getAppClientId());
		formData.add("client_secret", azureProperties.getAppClientSecret());
		formData.add("grant_type", "client_credentials");
		formData.add("scope", scope);

		String responseJson = webClient.post()
				.body(BodyInserters.fromFormData(formData))
				.retrieve()
				.bodyToMono(String.class)
				.doOnError(this::handleError)
				.block();

		try {
			return objectMapper.readValue(responseJson, TokenResponse.class).accessToken();
		} catch (JsonProcessingException | ClassCastException e) {
			throw new AzureTokenException(String.format("Klarte ikke parse token fra Azure. Feilmelding=%s", e.getMessage()), e);
		}
	}

	private void handleError(Throwable error) {
		if (error instanceof WebClientResponseException response && ((WebClientResponseException) error).getStatusCode().is4xxClientError()) {
			throw new AzureTokenException(
					String.format("Klarte ikke hente token fra Azure. Feilet med statuskode=%s Feilmelding=%s",
							response.getRawStatusCode(),
							response.getMessage()),
					error);
		} else {
			throw new AzureTokenException(
					String.format("Kall mot Azure feilet med feilmelding=%s", error.getMessage()),
					error);
		}
	}
}