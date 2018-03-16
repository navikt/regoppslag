package no.nav.regoppslag.config.security.provider.rest;

import static no.nav.regoppslag.rest.RegisteroppslagRestController.REST;

import no.nav.regoppslag.config.fasit.LdapAlias;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.util.ReflectionUtils;

import javax.inject.Inject;
import java.lang.reflect.Field;

/**
 * Spring boot security config for Basic Authentication and authorisation with LDAP users as authenthic users.
 *
 * @author Jarl Øystein Samseth, Visma Consulting
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	public static final String LDAP_CACHE_RS_LOGIN = "ldapCacheRestServiceLogin";
	private final LdapAlias ldapAlias;
	private final CacheManager cacheManager;
	
	@Inject
	public SecurityConfig(LdapAlias ldapAlias, CacheManager cacheManager) {
		this.ldapAlias = ldapAlias;
		this.cacheManager = cacheManager;
	}
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				.antMatchers("/isAlive","/isReady","/internal/**").permitAll();
		http.authorizeRequests().antMatchers(REST+"**")
				.fullyAuthenticated()
				.and().httpBasic();
		
		http.csrf().disable(); //Innloggingen er stateless og uten cookies, så dette er trygt.
	}
	
	
	@Override
	public void configure(AuthenticationManagerBuilder auth) throws Exception {
		
		auth.ldapAuthentication()
				.contextSource()
				.url(ldapAlias.getUrl())
				.managerDn(ldapAlias.getUsername())
				.managerPassword(ldapAlias.getPassword())
				.and()
				.userSearchBase(ldapAlias.getServiceuser().getBasedn())
				.userSearchFilter("cn={0}")
				.groupSearchBase(ldapAlias.getRequiredroledn() + "," + ldapAlias.getBasedn())
				.groupSearchFilter("member={0}")
				.addObjectPostProcessor(addCachingWrapper());
	}
	
	private ObjectPostProcessor<LdapAuthenticationProvider> addCachingWrapper() {
		return new ObjectPostProcessor<LdapAuthenticationProvider>() {

			@Override
			public CachingLdapAuthenticationProvider postProcess(LdapAuthenticationProvider ldapAuthenticationProvider) {
				Field authenticatorField = ReflectionUtils.findField(LdapAuthenticationProvider.class, "authenticator");
				ReflectionUtils.makeAccessible(authenticatorField);
				Field authoritiesPopulatorField = ReflectionUtils.findField(LdapAuthenticationProvider.class, "authoritiesPopulator");
				ReflectionUtils.makeAccessible(authoritiesPopulatorField);
				LdapAuthenticator authenticator = (LdapAuthenticator) ReflectionUtils.getField(authenticatorField, ldapAuthenticationProvider);
				LdapAuthoritiesPopulator authoritiesPopulator = (LdapAuthoritiesPopulator) ReflectionUtils.getField(authoritiesPopulatorField, ldapAuthenticationProvider);
				
				return new CachingLdapAuthenticationProvider(authenticator, authoritiesPopulator, cacheManager);
			}
		};
	}
}
