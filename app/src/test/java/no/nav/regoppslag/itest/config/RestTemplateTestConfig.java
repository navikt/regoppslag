package no.nav.regoppslag.itest.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

import static no.nav.regoppslag.util.TestUtil.classpathToString;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Configuration
@Profile("itest")
public class RestTemplateTestConfig {
	
	public static final Duration TIMEOUT = Duration.ofMillis(30_000);
	
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
		return restTemplateBuilder
				.requestFactory(HttpComponentsClientHttpRequestFactory.class)
				.setReadTimeout(TIMEOUT)
				.setConnectTimeout(TIMEOUT)
				.build();
	}
	
}
