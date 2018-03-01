package no.nav.regoppslag.config;

import static no.nav.regoppslag.rest.RegisteroppslagRestController.REGISTEROPPSLAG_URI_PATH;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

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
		http.authorizeRequests()
				.antMatchers("/isAlive","/isReady","/internal/**").permitAll();
		http.authorizeRequests()
				.antMatchers(REGISTEROPPSLAG_URI_PATH).hasRole("USER").and().httpBasic();
		http.csrf().disable();//TODO: skal vi ha med denne? kan være kjekt å ha den enabled
	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.inMemoryAuthentication().withUser("ola").password("nordmann").roles("USER");
	}
}
