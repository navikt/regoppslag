package no.nav.regoppslag.consumer;

import no.nav.regoppslag.config.security.BearerAuthenticationToken;
import no.nav.regoppslag.consumer.azure.TokenConsumer;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static no.nav.regoppslag.config.security.SecurityContextHandlerInterceptor.AUTH_ERRORMESSAGE;
import static no.nav.regoppslag.config.security.TokenClaimExtractor.isOnBehalfOfFlowToken;

public class AzureFlowInterceptor implements ClientHttpRequestInterceptor {

	private final TokenConsumer tokenConsumer;
	private final String scope;

	public AzureFlowInterceptor(TokenConsumer tokenConsumer, String scope) {
		this.tokenConsumer = tokenConsumer;
		this.scope = scope;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof BearerAuthenticationToken) {
			JwtToken authenticatedJwtToken = (JwtToken) authentication.getCredentials();
			if (isOnBehalfOfFlowToken(authenticatedJwtToken)) {
				// on_behalf_of
				request.getHeaders().setBearerAuth(tokenConsumer.getOnBehalfOfToken(scope, authenticatedJwtToken));
			} else {
				// client_credential
				request.getHeaders().setBearerAuth(tokenConsumer.getClientCredentialToken(scope));
			}
			return execution.execute(request, body);
		} else {
			throw new RegOppslagSecurityException(AUTH_ERRORMESSAGE);
		}
	}
}
