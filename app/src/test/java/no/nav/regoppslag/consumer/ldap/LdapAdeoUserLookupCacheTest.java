package no.nav.regoppslag.consumer.ldap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.nav.regoppslag.config.ldap.LdapConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.ArrayList;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {LdapConfig.class, LdapAdeoUserLookupCacheTest.Config.class})
@TestPropertySource("classpath:ldap.properties")
public class LdapAdeoUserLookupCacheTest {

	@Inject
	private LdapTemplate ldapTemplate;

	@Inject
	private LdapAdeoUserLookup ldapAdeoUserLookup;

	private static final String NAME1 = "Test Testesen";
	private static final String NAME2 = "Nils Nilsen";

	@Test
	public void shouldCache() throws Exception {
		when(ldapTemplate.search(Matchers.<LdapQuery>any(), Matchers.<AttributesMapper<String>>any())).thenReturn(new ArrayList<String>() {{
			add(NAME1);
		}});
		ldapAdeoUserLookup.hentFulltNavn("Z999990");

		when(ldapTemplate.search(Matchers.<LdapQuery>any(), Matchers.<AttributesMapper<String>>any())).thenReturn(new ArrayList<String>() {{
			add(NAME2);
		}});
		String fulltNavn = ldapAdeoUserLookup.hentFulltNavn("Z999990");

		assertEquals(fulltNavn, NAME1);
	}

	@Configuration
	static class Config {
		@Bean
		static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
			return new PropertySourcesPlaceholderConfigurer();
		}

		@Bean
		LdapTemplate ldapTemplate() {
			return mock(LdapTemplate.class);
		}

	}
}