package no.nav.regoppslag.config;

import no.nav.regoppslag.consumer.azure.TokenConsumer;
import no.nav.regoppslag.consumer.azure.TokenResponse;
import no.nav.regoppslag.consumer.azure.digdir.AzureProperties;
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
		azureproperties.setScopeDigdirKrr("scope");
		azureproperties.setClientId("clientId");
		azureproperties.setClientSecret("secret");
		azureproperties.setTenantId("tenantId");
		azureproperties.setTokenUrl("url");
		azureproperties.setWellKnownUrl("wellKnown");
		return azureproperties;
	}

}
