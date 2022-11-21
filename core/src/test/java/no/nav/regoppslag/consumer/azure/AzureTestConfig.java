package no.nav.regoppslag.consumer.azure;

import no.nav.regoppslag.consumer.azure.TokenConsumer;
import no.nav.regoppslag.consumer.azure.TokenResponse;
import no.nav.regoppslag.consumer.azure.AzureProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("itest")
@Configuration
public class AzureTestConfig {

	@Bean
	public TokenConsumer tokenConsumer() {
		return (String s) -> new TokenResponse();
	}

	@Bean
	public AzureProperties azureProperties() {
		AzureProperties azureproperties = new AzureProperties();
		azureproperties.setAppScopedigdirkrr("scope");
		azureproperties.setAppScopeDokmet("scope");
		azureproperties.setAppClientId("clientId");
		azureproperties.setAppClientSecret("secret");
		azureproperties.setOpenidConfigTokenEndpoint("url");
		return azureproperties;
	}

}
