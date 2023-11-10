package no.nav.regoppslag.config.security;

import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Populerer spring security sin {@link org.springframework.security.core.context.SecurityContext}
 * Brukes for å propagere authenticated brukere på en stateless måte uten å kjøre full spring-security web oppsett
 */
public class SecurityContextHandlerInterceptor implements HandlerInterceptor {

	public static final String AUTH_ERRORMESSAGE = "Tilgang er avvist. " +
			"Ingen gyldig token på Authorization header. Token må være utsted av NAV onprem security-token-service eller azure.";
	private final TokenValidationContextHolder tokenValidationContextHolder;

	public SecurityContextHandlerInterceptor(TokenValidationContextHolder tokenValidationContextHolder) {
		this.tokenValidationContextHolder = tokenValidationContextHolder;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		TokenValidationContext tokenValidationContext = tokenValidationContextHolder.getTokenValidationContext();
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(new BearerAuthenticationToken(tokenValidationContext));
		SecurityContextHolder.setContext(context);
		return true;
	}
}
