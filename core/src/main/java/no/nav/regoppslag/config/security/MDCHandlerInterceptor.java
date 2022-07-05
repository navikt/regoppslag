package no.nav.regoppslag.config.security;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.util.MDCConstants;
import no.nav.regoppslag.util.NavHeaders;
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
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
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
		final String navCallId = request.getHeader(NavHeaders.NAV_CALLID);
		if (isNotBlank(navCallId)) {
			MDC.put(MDCConstants.CALL_ID, navCallId);
			return;
		}
		// Fallback
		MDC.put(MDCConstants.CALL_ID, UUID.randomUUID().toString());
	}

	private void populateConsumerId(TokenValidationContext tokenValidationContext, JwtToken jwtToken) {
		final String consumerId = tokenClaimExtractor.getConsumerId(tokenValidationContext, jwtToken);
		if (isNotBlank(consumerId)) {
			MDC.put(MDCConstants.CONSUMER_ID, consumerId);
			return;
		}
		// Fallback
		MDC.put(MDCConstants.CONSUMER_ID, UKJENT_CONSUMER_ID);
	}

	private void populateUserId(TokenValidationContext tokenValidationContext, JwtToken jwtToken) {
		final String consumerId = tokenClaimExtractor.getUserId(tokenValidationContext, jwtToken);
		if (isNotBlank(consumerId)) {
			MDC.put(MDCConstants.USER_ID, consumerId);
			return;
		}
		// Fallback
		MDC.put(MDCConstants.USER_ID, UKJENT_USER_ID);
	}


}
