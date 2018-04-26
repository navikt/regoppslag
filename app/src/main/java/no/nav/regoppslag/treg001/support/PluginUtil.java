package no.nav.regoppslag.treg001.support;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class PluginUtil {
	
	public static void updateSecurityContext(SecurityContext securityContext, boolean isAuthenticated) {
		
		if (securityContext != null) {
			
			if (securityContext.getAuthentication() != null) {
				securityContext.getAuthentication().setAuthenticated(isAuthenticated);
			}
			
			SecurityContextHolder.getContext().setAuthentication(securityContext.getAuthentication());
		}
	}
	
	public static void updateSecurityContext(SecurityContext securityContext) {
		
		if (securityContext != null) {
			SecurityContextHolder.getContext().setAuthentication(securityContext.getAuthentication());
		}
	}
}
