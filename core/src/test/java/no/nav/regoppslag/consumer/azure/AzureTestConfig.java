package no.nav.regoppslag.consumer.azure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("itest")
@Configuration
public class AzureTestConfig {

	@Bean
	public TokenConsumer tokenConsumer() {
		return (String s) -> "";
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
