package no.nav.regoppslag.treg001.itest.config;

import static org.mockito.Mockito.mock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.ldap.core.LdapTemplate;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
@Configuration
public class MockLdapTestConfig {
	
	
		@Bean
		static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
			return new PropertySourcesPlaceholderConfigurer();
		}
		
		@Bean
		LdapTemplate ldapTemplate() {
			return mock(LdapTemplate.class);
		}
		
}
