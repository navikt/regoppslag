package no.nav.regoppslag.config.ldap;

import no.nav.regoppslag.consumer.ldap.LdapAdeoUserLookup;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Configuration
public class LdapConfig {

	@Autowired
	MicrometerMetrics metrics;
	
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
	LdapAdeoUserLookup ldapUserLookup(LdapTemplate ldapTemplate,
									  @Value("${ldap_user_basedn}") final String userBaseDn) {
		return new LdapAdeoUserLookup(ldapTemplate, userBaseDn, metrics);
	}
	
}
