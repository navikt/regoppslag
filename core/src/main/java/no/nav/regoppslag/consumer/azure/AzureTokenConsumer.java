package no.nav.regoppslag.consumer.azure;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import static no.nav.regoppslag.config.cache.CacheConfig.AZURE_CLIENT_CREDENTIAL_TOKEN;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@Component
public class AzureTokenConsumer {

	private static final String AZURE_TOKEN_INSTANCE = "azuretoken";
	static final String ON_BEHALF_OF_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer";
	static final String ON_BEHALF_OF = "on_behalf_of";
	static final String CLIENT_CREDENTIALS = "client_credentials";

	private final AzureProperties azureProperties;
	private final JsonMapper jsonMapper;
	private final WebClient webClient;

	@Autowired
	public AzureTokenConsumer(AzureProperties azureProperties,
							  JsonMapper jsonMapper,
							  WebClient webClient) {
		this.azureProperties = azureProperties;
		this.jsonMapper = jsonMapper;
		this.webClient = webClient.mutate()
				.defaultHeader(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
				.baseUrl(azureProperties.openidConfigTokenEndpoint())
				.build();
	}

	@Retry(name = AZURE_TOKEN_INSTANCE)
	@CircuitBreaker(name = AZURE_TOKEN_INSTANCE)
	@Cacheable(value = AZURE_CLIENT_CREDENTIAL_TOKEN)
	public String getClientCredentialToken(String scope) {
		return getAzureToken(scope, null);
	}

	@Retry(name = AZURE_TOKEN_INSTANCE)
	@CircuitBreaker(name = AZURE_TOKEN_INSTANCE)
	//@Cacheable(value = AZURE_ON_BEHALF_OF_TOKEN, key = "#token.subject")
	public String getOnBehalfOfToken(String scope, JwtToken token) {
		// Caches på #token.subject som er "sub" claim i JWT. Skal være trygt å cache på denne key på tvers av app/scopes
		// https://learn.microsoft.com/en-us/azure/active-directory/develop/access-tokens#payload-claims
		// This value can be used to perform authorization checks, such as when the token is used to access a resource, and can be used as a key in database tables.
		return getAzureToken(scope, token.getEncodedToken());
	}

	private String getAzureToken(String scope, String token) {
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("client_id", azureProperties.appClientId());
		formData.add("client_secret", azureProperties.appClientSecret());
		formData.add("scope", scope);

		if (token != null) {
			formData.add("requested_token_use", ON_BEHALF_OF);
			formData.add("grant_type", ON_BEHALF_OF_GRANT_TYPE);
			formData.add("assertion", token);
		} else {
			formData.add("grant_type", CLIENT_CREDENTIALS);
		}

		String responseJson = webClient.post()
				.body(BodyInserters.fromFormData(formData))
				.retrieve()
				.bodyToMono(String.class)
				.onErrorMap(this::mapError)
				.block();

		try {
			return jsonMapper.readValue(responseJson, TokenResponse.class).accessToken();
		} catch (JacksonException | ClassCastException e) {
			throw new AzureTokenException(String.format("Klarte ikke parse token fra Azure. Feilmelding=%s", e.getMessage()), e);
		}
	}

	private Throwable mapError(Throwable error) {
		if (error instanceof WebClientResponseException response && response.getStatusCode().is4xxClientError()) {
			throw new AzureTokenException(
					String.format("Klarte ikke hente token fra Azure. Feilet med status=%s, feilmelding=%s, body=%s",
							response.getStatusCode(),
							response.getMessage(),
							response.getResponseBodyAsString()),
					error);
		} else {
			throw new AzureTokenException(
					String.format("Kall mot Azure feilet med feilmelding=%s", error.getMessage()), error);
		}
	}
}
