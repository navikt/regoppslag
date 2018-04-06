package no.nav.regoppslag.config.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * Spring boot security config for Basic Authentication and authorisation with LDAP users as authenthic users.
 *
 * @author Jarl Øystein Samseth, Visma Consulting
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.addFilterBefore(new AuthenticationHandler(), BasicAuthenticationFilter.class);
		http.csrf().disable(); //Innloggingen er stateless og uten cookies, så dette er trygt.
	}
}
