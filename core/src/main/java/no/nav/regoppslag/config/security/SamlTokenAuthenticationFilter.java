package no.nav.regoppslag.config.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.rt.security.claims.Claim;
import org.apache.cxf.rt.security.saml.utils.SAMLUtils;
import org.slf4j.MDC;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.w3c.dom.Element;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Enumeration;
import java.util.Set;
import java.util.UUID;

import static no.nav.regoppslag.config.security.SamlTokenUtils.elementToSamlAssertionWrapper;
import static no.nav.regoppslag.config.security.SamlTokenUtils.samlTokenToElement;
import static no.nav.regoppslag.util.MDCConstants.CALL_ID;
import static no.nav.regoppslag.util.MDCConstants.CONSUMER_ID;
import static no.nav.regoppslag.util.MDCConstants.UKJENT;
import static no.nav.regoppslag.util.MDCConstants.USER_ID;
import static org.springframework.security.core.authority.AuthorityUtils.NO_AUTHORITIES;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Slf4j
public class SamlTokenAuthenticationFilter extends OncePerRequestFilter {

	private static final Set<String> ALLOWED_PATHS = Set.of("/isReady", "/isAlive", "/internal/selftest", "/actuator/prometheus", "/rest/postadresse", "/v3/api-docs", "/swagger-ui");

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		SecurityContextHolder.clearContext();

		//In case the application have several authorization headers
		String header = getSamlAuthHeader(request.getHeaders("Authorization"));
		MDC.put(CALL_ID, getOrCreateCallId(request));

		if (header == null || !header.startsWith("SAML ")) {
			if (StringUtils.startsWithAny(request.getServletPath(), ALLOWED_PATHS.toArray(new String[0]))) {
				filterChain.doFilter(request, response);
				return;
			} else {
				MDC.put(CONSUMER_ID, UKJENT);
				MDC.put(USER_ID, UKJENT);
				log.error("Authorization SAML token mangler. Returnerer Unauthorized til konsument. path={}", request.getServletPath());
				// Gir 401 Unauthorized til alle som ikke kan vise et SAML token i Authorization headeren.
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}
		}

		String decodedToken = extractAndDecodeHeader(header);
		UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(getConsumerId(decodedToken), decodedToken, NO_AUTHORITIES);
		SecurityContextHolder.getContext().setAuthentication(authRequest);

		MDC.put(CONSUMER_ID, authRequest.getName());
		MDC.put(USER_ID, getUserId(decodedToken));

		filterChain.doFilter(request, response);
	}

	private String extractAndDecodeHeader(String header) {

		byte[] base64Token = header.substring(5).getBytes(StandardCharsets.UTF_8);
		byte[] decoded;

		try {
			decoded = Base64.getDecoder().decode(base64Token);
		} catch (IllegalArgumentException e) {
			log.warn("Kunne ikke dekode SAML authentication token", e);
			throw new BadCredentialsException(
					"Kunne ikke dekode SAML authentication token");
		}

		return new String(decoded, StandardCharsets.UTF_8);
	}


	private String getSamlAuthHeader(Enumeration<String> headers) {

		while (headers.hasMoreElements()) {
			String header = headers.nextElement();
			if (header.startsWith("SAML ")) {
				return header;
			}
		}

		return null;
	}

	private String getUserId(String decodedToken) {
		Element element = samlTokenToElement(decodedToken);
		return elementToSamlAssertionWrapper(element).getSubjectName();
	}

	private String getConsumerId(String decodedToken) {
		Element element = samlTokenToElement(decodedToken);
		String consumerId = null;

		try {
			consumerId = (String) SAMLUtils.getClaims(elementToSamlAssertionWrapper(element))
					.stream()
					.filter(claim -> claim.getClaimType().getPath().equalsIgnoreCase(CONSUMER_ID))
					.findAny()
					.orElse(new Claim())
					.getValues()
					.get(0);
		} catch (Exception e) {
			//Do nothing
			log.warn("Feil ved henting av consumerId fra SAML token", e);
		}

		if (consumerId == null) {
			return UKJENT;
		}

		return consumerId;
	}

	private String getOrCreateCallId(HttpServletRequest request) {
		Enumeration<String> headers = request.getHeaders(CALL_ID);
		if (headers.hasMoreElements()) {
			return headers.nextElement();
		}
		return UUID.randomUUID().toString();
	}
}
