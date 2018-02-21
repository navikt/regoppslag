package no.nav.regoppslag.config.ldap;

import static no.nav.regoppslag.ldap.LdapAdeoUserLookup.HENT_FULLT_NAVN;

import no.nav.regoppslag.ldap.LdapAdeoUserLookup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

import java.util.Arrays;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Configuration
@EnableCaching
public class LdapConfig {

	@Bean
	LdapContextSource ldapContextSource(@Value("${ldap_url}") final String ldapgwUrl,
										@Value("${ldap_username}") final String username,
										@Value("${ldap_password}") final String password) {
		LdapContextSource ldapContextSource = new LdapContextSource();
		ldapContextSource.setUrl(ldapgwUrl);
		ldapContextSource.setUserDn(username);
		ldapContextSource.setPassword(password);
		return ldapContextSource;
	}

	@Bean
	LdapTemplate ldapTemplate(ContextSource ldapContextSource) {
		return new LdapTemplate(ldapContextSource);
	}

	@Bean
	LdapAdeoUserLookup ldapUserLookup(LdapTemplate ldapTemplate, @Value("${ldap_user_basedn}") final String userBaseDn) {
		return new LdapAdeoUserLookup(ldapTemplate, userBaseDn);

	}


	@Bean
	public CacheManager cacheManager() {
		// configure and return an implementation of Spring's CacheManager SPI
		SimpleCacheManager cacheManager = new SimpleCacheManager();
		cacheManager.setCaches(Arrays.asList(new ConcurrentMapCache(HENT_FULLT_NAVN)));
		return cacheManager;
	}

}
