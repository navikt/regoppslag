package no.nav.regoppslag.itest.config;

import static no.nav.regoppslag.itest.config.RestTemplateTestConfig.ADD_SAML_TOKEN_TO_HEADER;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Base64Utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class RestSamlTokenInterceptor implements ClientHttpRequestInterceptor {
	
	final String token;
	
	public RestSamlTokenInterceptor(String token) {
		this.token = token;
	}
	
	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
		if (ADD_SAML_TOKEN_TO_HEADER) { //This is here to be able to test with no headers
			String token = Base64Utils.encodeToString((this.token).getBytes(StandardCharsets.UTF_8));
			request.getHeaders().add("Authorization", "SAML " + token);
			return execution.execute(request, body);
		}
		return execution.execute(request, body);
	}
}
