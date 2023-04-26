package no.nav.regoppslag.consumer.azure;

import no.nav.security.token.support.core.jwt.JwtToken;

public interface TokenConsumer {
	String getClientCredentialToken(String scope);
	String getOnBehalfOfToken(String scope, JwtToken token);
}
