package no.nav.regoppslag.config.security.provider.rest;

import static no.nav.regoppslag.config.security.provider.rest.SecurityConfig.LDAP_CACHE_RS_LOGIN;
import static org.springframework.security.core.authority.AuthorityUtils.NO_AUTHORITIES;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;

/**
 * @author Hans Petter Simonsen - Miles
 */
public class CachingLdapAuthenticationProvider extends LdapAuthenticationProvider {

	private final CacheManager cacheManager;

	CachingLdapAuthenticationProvider(LdapAuthenticator authenticator, LdapAuthoritiesPopulator authoritiesPopulator, CacheManager cacheManager) {
		super(authenticator, authoritiesPopulator);
		this.cacheManager = cacheManager;
	}

	@Override
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		Cache ldapCache = cacheManager.getCache(LDAP_CACHE_RS_LOGIN);
		String userName = authentication.getName();
		Integer cachedAuthHash = ldapCache.get(userName, Integer.class);
		if(cachedAuthHash == null) {
			Authentication authSuccess = super.authenticate(authentication);
			ldapCache.put(userName, authentication.hashCode());
			return authSuccess;
		} else {
			return cachedAuthenticate(cachedAuthHash, authentication);
		}
	}

	private Authentication cachedAuthenticate(Integer cachedAuthHash, Authentication requestAuth) {
		if(cachedAuthHash == requestAuth.hashCode()) {
			return new UsernamePasswordAuthenticationToken(requestAuth.getPrincipal(), requestAuth.getCredentials(), NO_AUTHORITIES);
		} else {
			throw new BadCredentialsException(this.messages.getMessage(
					"LdapAuthenticationProvider.badCredentials", "Bad credentials"));
		}
	}
}
