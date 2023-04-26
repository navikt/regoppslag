package no.nav.regoppslag.consumer.azure;

import no.nav.security.token.support.core.jwt.JwtToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("itest")
@Configuration
public class AzureTestConfig {

	@Bean
	public TokenConsumer tokenConsumer() {
		return new TokenConsumer() {
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
