package no.nav.regoppslag.config.security;

import static org.springframework.security.core.authority.AuthorityUtils.NO_AUTHORITIES;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Enumeration;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Slf4j
public class SamlTokenAuthenticationHandler extends OncePerRequestFilter {
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		SecurityContextHolder.clearContext();
		
		String header = getSamlAuthHeader(request.getHeaders("Authorization"));//In case the application have several authorization headers;
		
		if (header == null || !header.startsWith("SAML ")) {
			filterChain.doFilter(request, response);
			return;
		}
		
		String decodedToken = extractAndDecodeHeader(header);
		UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken("SAMLtoken", decodedToken, NO_AUTHORITIES);
		SecurityContextHolder.getContext().setAuthentication(authRequest);
		
		filterChain.doFilter(request, response);
	}
	
	private String extractAndDecodeHeader(String header) {
		
		byte[] base64Token = header.substring(5).getBytes(StandardCharsets.UTF_8);
		byte[] decoded;
		
		try {
			decoded = Base64.getDecoder().decode(base64Token);
		} catch (IllegalArgumentException e) {
			throw new BadCredentialsException(
					"Failed to decode SAML authentication token");
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
	
}
