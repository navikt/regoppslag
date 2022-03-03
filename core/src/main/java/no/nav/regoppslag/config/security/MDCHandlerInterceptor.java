package no.nav.regoppslag.config.security;

import no.nav.regoppslag.util.MDCConstants;
import no.nav.regoppslag.util.NavHeaders;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class MDCHandlerInterceptor implements HandlerInterceptor {

	private static final String UNKNOWN_VALUE = "unknown";
	public static final String ISSUER_AZUREV2 = "azurev2";
	public static final String ISSUER_RESTSTS = "reststs";
	private static final String UKJENT = "UKJENT";
	private final TokenValidationContextHolder tokenValidationContextHolder;

	public MDCHandlerInterceptor(TokenValidationContextHolder tokenValidationContextHolder) {
		this.tokenValidationContextHolder = tokenValidationContextHolder;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		populateCallId(request);

		populateConsumerId(request, tokenValidationContextHolder);

		//token-support will handle no-token cases and unauthenticated cases
		return true;
	}

	private void populateCallId(HttpServletRequest request) {
		final String navCallId = request.getHeader(NavHeaders.NAV_CALLID);
		if (isNotBlank(navCallId)) {
			MDC.put(MDCConstants.NAV_CALL_ID, navCallId);
			return;
		}

		// Fallback
		MDC.put(MDCConstants.NAV_CALL_ID, UUID.randomUUID().toString());
	}

	private void populateConsumerId(HttpServletRequest request, TokenValidationContextHolder tokenValidationContextHolder) {
		String consumerId = null;

		final TokenValidationContext tokenValidationContext = tokenValidationContextHolder.getTokenValidationContext();
		if (tokenValidationContext.getJwtTokenAsOptional(ISSUER_AZUREV2).isPresent()) {
			// Azure AD token (header: Authorization). Oauth 2.0 client credential grant flow og on-behalf-of flow
			//azureAdFlowSporingHandler.handle(tokenValidationContext.getJwtToken(ISSUER_AZUREV2), navUserIdHeader);
			consumerId = tokenValidationContext.getJwtToken(ISSUER_AZUREV2).getSubject();
		} else if (tokenValidationContext.getFirstValidToken().isPresent()) {
			// REST-STS (header: Authorization). System til system
			consumerId = tokenValidationContext.getFirstValidToken().get().getSubject();
		}

		if (isNotBlank(consumerId)) {
			MDC.put(MDCConstants.NAV_CONSUMER_ID, consumerId);
		}
	}


	String getCallId(HttpServletRequest request) {
		return Optional.of(request.getHeader(NavHeaders.NAV_CALLID)).orElse(UUID.randomUUID().toString());
	}

}
