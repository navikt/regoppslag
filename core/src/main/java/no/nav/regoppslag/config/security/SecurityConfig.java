package no.nav.regoppslag.config.security;

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * Spring boot security config for Basic Authentication and authorisation with LDAP users as authenthic users.
 *
 * @author Jarl Øystein Samseth, Visma Consulting
 */
@EnableWebSecurity
@EnableJwtTokenValidation(ignore = {"org.springframework", "org.springdoc"})
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.addFilterBefore(new SamlTokenAuthenticationFilter(), BasicAuthenticationFilter.class);
		http.csrf().disable();
	}
}
