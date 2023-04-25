package no.nav.regoppslag.consumer;

import no.nav.regoppslag.consumer.azure.TokenConsumer;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.core.jwt.JwtTokenClaims;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class AzureFlowInterceptor implements ClientHttpRequestInterceptor {
	static final String DEFAULT_CLAIM_OID = "oid";
	static final String DEFAULT_CLAIM_SUB = "sub";
	static final String AZURE_ISSUER_V2 = "azurev2";

	private final TokenConsumer tokenConsumer;
	private final TokenValidationContextHolder tokenValidationContextHolder;
	private final String scope;

	public AzureFlowInterceptor(TokenConsumer tokenConsumer, TokenValidationContextHolder tokenValidationContextHolder, String scope) {
		this.tokenConsumer = tokenConsumer;
		this.tokenValidationContextHolder = tokenValidationContextHolder;
		this.scope = scope;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
		TokenValidationContext tokenValidationContext = tokenValidationContextHolder.getTokenValidationContext();
		if(tokenValidationContext.hasValidToken() && tokenValidationContext.hasTokenFor(AZURE_ISSUER_V2)) {
			JwtToken jwtToken = tokenValidationContext.getJwtToken(AZURE_ISSUER_V2);
			if(isOnBehalfOfAzureToken(jwtToken)) {
				// on_behalf_of
				request.getHeaders().setBearerAuth(tokenConsumer.getOnBehalfOfToken(scope, jwtToken.getTokenAsString()));
			} else {
				// client_credential
				request.getHeaders().setBearerAuth(tokenConsumer.getClientCredentialToken(scope));
			}
		} else {
			// rest-sts
			request.getHeaders().setBearerAuth(tokenConsumer.getClientCredentialToken(scope));
		}
		return execution.execute(request, body);
	}

	private boolean isOnBehalfOfAzureToken(JwtToken jwtToken) {
		JwtTokenClaims jwtTokenClaims = jwtToken.getJwtTokenClaims();
		return jwtTokenClaims.getStringClaim(DEFAULT_CLAIM_SUB) != null && jwtTokenClaims.getStringClaim(DEFAULT_CLAIM_OID) != null
			   && !jwtTokenClaims.getStringClaim(DEFAULT_CLAIM_SUB).equals(jwtTokenClaims.getStringClaim(DEFAULT_CLAIM_OID));
	}
}
