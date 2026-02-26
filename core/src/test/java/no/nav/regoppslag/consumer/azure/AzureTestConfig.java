package no.nav.regoppslag.consumer.azure;

import no.nav.security.token.support.core.jwt.JwtToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.json.JsonMapper;

@Profile("itest")
@Configuration
public class AzureTestConfig {

	@Bean
	public AzureTokenConsumer azureTokenConsumer(AzureProperties azureProperties, JsonMapper jsonMapper, WebClient webClient) {
		return new AzureTokenConsumer(azureProperties, jsonMapper, webClient) {
			@Override
			public String getClientCredentialToken(String scope) {
				return "token";
			}

			@Override
			public String getOnBehalfOfToken(String scope, JwtToken token) {
				return "token";
			}
		};
	}
}
