package no.nav.regoppslag.itest.config;

import no.nav.regoppslag.config.fasit.ServiceuserAlias;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Profile("itest")
@Configuration
public class RestTemplateTestConfig {
	
	public static final int TIMEOUT = 30_000;
	
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder, final ServiceuserAlias serviceuserAlias) {
		return restTemplateBuilder
				.requestFactory(new HttpComponentsClientHttpRequestFactory())
				.setReadTimeout(TIMEOUT)
				.setConnectTimeout(TIMEOUT)
				.basicAuthorization(serviceuserAlias.getUsername(), serviceuserAlias.getPassword()).build();
	}
	
}
