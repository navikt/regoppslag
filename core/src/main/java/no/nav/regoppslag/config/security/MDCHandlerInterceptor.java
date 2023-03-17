package no.nav.regoppslag.config.security;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

import static no.nav.regoppslag.config.security.TokenClaimExtractor.UKJENT_CONSUMER_ID;
import static no.nav.regoppslag.config.security.TokenClaimExtractor.UKJENT_USER_ID;
import static no.nav.regoppslag.util.MDCConstants.CALL_ID;
import static no.nav.regoppslag.util.MDCConstants.CONSUMER_ID;
import static no.nav.regoppslag.util.MDCConstants.USER_ID;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
public class MDCHandlerInterceptor implements HandlerInterceptor {

	private static final String AUTH_ERRORMESSAGE = "Tilgang er avvist. " +
			"Ingen gyldig token på Authorization header. Token må være utsted av NAV onprem security-token-service eller azure.";

	private final TokenValidationContextHolder tokenValidationContextHolder;
	private final TokenClaimExtractor tokenClaimExtractor = new TokenClaimExtractor();

	public MDCHandlerInterceptor(TokenValidationContextHolder tokenValidationContextHolder) {
		this.tokenValidationContextHolder = tokenValidationContextHolder;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		TokenValidationContext tokenValidationContext = tokenValidationContextHolder.getTokenValidationContext();

		JwtToken jwtToken = tokenValidationContext.getFirstValidToken()
				.orElseThrow(() -> new RegOppslagSecurityException(AUTH_ERRORMESSAGE));

		populateCallId(request);
		populateConsumerId(tokenValidationContext, jwtToken);
		populateUserId(tokenValidationContext, jwtToken);

		// token-support will handle no-token cases and unauthenticated cases
		return true;
	}

	private void populateCallId(HttpServletRequest request) {
		final String navCallId = request.getHeader(CALL_ID);

		if (isNotBlank(navCallId)) {
			MDC.put(CALL_ID, navCallId);
			return;
		}

		MDC.put(CALL_ID, UUID.randomUUID().toString());
	}

	private void populateConsumerId(TokenValidationContext tokenValidationContext, JwtToken jwtToken) {
		final String consumerId = tokenClaimExtractor.getConsumerId(tokenValidationContext, jwtToken);

		if (isNotBlank(consumerId)) {
			MDC.put(CONSUMER_ID, consumerId);
			return;
		}

		MDC.put(CONSUMER_ID, UKJENT_CONSUMER_ID);
	}

	private void populateUserId(TokenValidationContext tokenValidationContext, JwtToken jwtToken) {
		final String consumerId = tokenClaimExtractor.getUserId(tokenValidationContext, jwtToken);

		if (isNotBlank(consumerId)) {
			MDC.put(USER_ID, consumerId);
			return;
		}

		MDC.put(USER_ID, UKJENT_USER_ID);
	}

}
