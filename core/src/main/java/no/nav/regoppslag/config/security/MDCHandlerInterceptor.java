package no.nav.regoppslag.config.security;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;

import static no.nav.regoppslag.config.security.SecurityContextHandlerInterceptor.AUTH_ERRORMESSAGE;
import static no.nav.regoppslag.config.security.TokenClaimExtractor.UKJENT_CONSUMER_ID;
import static no.nav.regoppslag.config.security.TokenClaimExtractor.UKJENT_USER_ID;
import static no.nav.regoppslag.config.security.TokenClaimExtractor.getConsumerId;
import static no.nav.regoppslag.config.security.TokenClaimExtractor.getUserId;
import static no.nav.regoppslag.util.MDCConstants.CALL_ID;
import static no.nav.regoppslag.util.MDCConstants.CONSUMER_ID;
import static no.nav.regoppslag.util.MDCConstants.USER_ID;
import static no.nav.regoppslag.util.NavHeaders.NAV_CALLID;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
public class MDCHandlerInterceptor implements HandlerInterceptor {

	private final TokenValidationContextHolder tokenValidationContextHolder;

	public MDCHandlerInterceptor(TokenValidationContextHolder tokenValidationContextHolder) {
		this.tokenValidationContextHolder = tokenValidationContextHolder;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		TokenValidationContext tokenValidationContext = tokenValidationContextHolder.getTokenValidationContext();

		JwtToken jwtToken = tokenValidationContext.getFirstValidToken();

		if (jwtToken == null) {
			throw new RegOppslagSecurityException(AUTH_ERRORMESSAGE);
		}

		populateCallId(request);
		populateConsumerId(tokenValidationContext, jwtToken);
		populateUserId(tokenValidationContext, jwtToken);

		// token-support will handle no-token cases and unauthenticated cases
		return true;
	}

	private void populateCallId(HttpServletRequest request) {
		final String navCallId = request.getHeader(NAV_CALLID);

		if (isNotBlank(navCallId)) {
			MDC.put(CALL_ID, navCallId);
			return;
		}

		MDC.put(CALL_ID, UUID.randomUUID().toString());
	}

	private void populateConsumerId(TokenValidationContext tokenValidationContext, JwtToken jwtToken) {
		final String consumerId = getConsumerId(tokenValidationContext, jwtToken);

		if (isNotBlank(consumerId)) {
			MDC.put(CONSUMER_ID, consumerId);
			return;
		}

		MDC.put(CONSUMER_ID, UKJENT_CONSUMER_ID);
	}

	private void populateUserId(TokenValidationContext tokenValidationContext, JwtToken jwtToken) {
		final String consumerId = getUserId(tokenValidationContext, jwtToken);

		if (isNotBlank(consumerId)) {
			MDC.put(USER_ID, consumerId);
			return;
		}

		MDC.put(USER_ID, UKJENT_USER_ID);
	}

}
