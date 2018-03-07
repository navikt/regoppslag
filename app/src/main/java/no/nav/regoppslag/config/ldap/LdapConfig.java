package no.nav.regoppslag.config.ldap;

import static no.nav.regoppslag.config.security.provider.rest.SecurityConfig.LDAP_CACHE_RS_LOGIN;
import static no.nav.regoppslag.ldap.LdapAdeoUserLookup.HENT_FULLT_NAVN;

import com.github.benmanes.caffeine.cache.Caffeine;
import no.nav.regoppslag.ldap.LdapAdeoUserLookup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

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
		//TODO BRUKER
		//TODO ORGANISASJON
		//TODO Virksomhetsadresse EREG
		CaffeineCache cacheHentFulltNavn = new CaffeineCache(HENT_FULLT_NAVN, Caffeine.newBuilder()
				.expireAfterAccess(2, TimeUnit.DAYS)
				.maximumSize(2000)
				.build());
		cacheManager.setCaches(Arrays.asList(cacheHentFulltNavn,
				new ConcurrentMapCache(LDAP_CACHE_RS_LOGIN)));
		return cacheManager;
	}
	
}
