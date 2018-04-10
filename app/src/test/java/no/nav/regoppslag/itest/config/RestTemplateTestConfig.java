package no.nav.regoppslag.itest.config;

import static no.nav.regoppslag.util.TestUtil.classpathToString;

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
@Configuration
@Profile("itest")
public class RestTemplateTestConfig {
	
	public static final int TIMEOUT = 30_000;
	
	public static boolean ADD_SAML_TOKEN_TO_HEADER = true;
	
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder, final ServiceuserAlias serviceuserAlias) {
		return restTemplateBuilder
				.requestFactory(new HttpComponentsClientHttpRequestFactory())
				.setReadTimeout(TIMEOUT)
				.setConnectTimeout(TIMEOUT)
				.interceptors(new RestSamlTokenInterceptor(classpathToString("__files/felles/token/saml_token.xml"))).build();
	}
	
}
