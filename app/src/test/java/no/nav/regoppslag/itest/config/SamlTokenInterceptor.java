package no.nav.regoppslag.itest.config;

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
public class SamlTokenInterceptor implements ClientHttpRequestInterceptor {
	
	final String token;
	
	public SamlTokenInterceptor(String token) {
		this.token = token;
	}
	
	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
		
		String token = Base64Utils.encodeToString((this.token).getBytes(StandardCharsets.UTF_8));
		request.getHeaders().add("Authorization", "SAML " + token);
		return execution.execute(request, body);
	}
}
