package no.nav.regoppslag.treg001.support;

import no.nav.regoppslag.treg001.MottakerPlugin;
import no.nav.regoppslag.treg001.SakspartPlugin;
import no.nav.regoppslag.treg001.xmlenricher.util.Payload;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class PluginUtil {
	
	public static SecurityContext createNewSecurityContext(Authentication oldAuthentication, boolean isAuthenticated) {
		if (oldAuthentication==null){
			return new SecurityContextImpl();
		}
		
		SecurityContext newSecurityContext = new SecurityContextImpl();
		newSecurityContext.setAuthentication(createNewAuthentication(oldAuthentication, isAuthenticated));
		return newSecurityContext;
	}
	
	private static UsernamePasswordAuthenticationToken createNewAuthentication(Authentication oldAuthentication, boolean isAuthenticated){
		UsernamePasswordAuthenticationToken newAuthentication;
		if (isAuthenticated) {
			newAuthentication = new UsernamePasswordAuthenticationToken(oldAuthentication.getName(), oldAuthentication.getCredentials(), AuthorityUtils.NO_AUTHORITIES);
		}else {
			newAuthentication = new UsernamePasswordAuthenticationToken(oldAuthentication.getName(), oldAuthentication.getCredentials());
		}
		return newAuthentication;
	}
	
	public static boolean securityContextIsUsedForAuthentication(Payload payload) {
		return payload.getPlugin() instanceof MottakerPlugin || payload.getPlugin() instanceof SakspartPlugin;
	}
	
}
