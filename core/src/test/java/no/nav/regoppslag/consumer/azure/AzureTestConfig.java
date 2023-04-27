package no.nav.regoppslag.consumer.azure;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

@Profile("itest")
@Configuration
public class AzureTestConfig {

	@Bean
	public AzureTokenConsumer azureTokenConsumer(AzureProperties azureProperties, ObjectMapper objectMapper, WebClient webClient) {
		return new AzureTokenConsumer(azureProperties, objectMapper, webClient) {
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

	@Bean
	public AzureProperties azureProperties() {
		AzureProperties azureproperties = new AzureProperties();
		azureproperties.setAppClientId("clientId");
		azureproperties.setAppClientSecret("secret");
		azureproperties.setOpenidConfigTokenEndpoint("url");
		return azureproperties;
	}

}
