package no.nav.regoppslag.config.ldap;

import static no.nav.regoppslag.config.security.provider.rest.SecurityConfig.LDAP_CACHE_RS_LOGIN;
import static no.nav.regoppslag.consumer.dokkat.Tkat020DokumenttypeInfo.HENT_DOKKAT_SPRAAKINFO;
import static no.nav.regoppslag.consumer.ldap.LdapAdeoUserLookup.HENT_FULLT_NAVN;
import static no.nav.regoppslag.consumer.norg2.OrganisasjonEnhetKontaktinformasjonV1Consumer.HENT_ENHET_NAVN;


import com.github.benmanes.caffeine.cache.Caffeine;
import no.nav.regoppslag.consumer.ldap.LdapAdeoUserLookup;
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
	
}
