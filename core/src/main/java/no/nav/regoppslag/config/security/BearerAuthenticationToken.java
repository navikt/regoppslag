package no.nav.regoppslag.config.security;

import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static no.nav.regoppslag.config.security.SecurityContextHandlerInterceptor.AUTH_ERRORMESSAGE;
import static no.nav.regoppslag.config.security.TokenClaimExtractor.getUserId;

/**
 * Holder autentisert Bearer token
 */
public class BearerAuthenticationToken extends AbstractAuthenticationToken {

	private final JwtToken authenticatedJwtToken;
	private final String principal;

	public BearerAuthenticationToken(TokenValidationContext tokenValidationContext) {
		super(List.of(new SimpleGrantedAuthority("ROLE_AUTHENTICATED_REQUEST")));
		this.authenticatedJwtToken = tokenValidationContext.getFirstValidToken()
				.orElseThrow(() -> new RegOppslagSecurityException(AUTH_ERRORMESSAGE));
		this.principal = getUserId(tokenValidationContext, authenticatedJwtToken);
		setAuthenticated(true);
	}

	@Override
	public Object getCredentials() {
		return authenticatedJwtToken;
	}

	@Override
	public Object getPrincipal() {
		return principal;
	}
}
