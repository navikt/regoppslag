package no.nav.regoppslag.consumer.ldap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import no.nav.regoppslag.config.ldap.LdapConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {LdapConfig.class, LdapAdeoUserLookupTest.Config.class})
@TestPropertySource("classpath:ldap.properties")
public class LdapAdeoUserLookupTest {

	@Inject
	private LdapAdeoUserLookup ldapAdeoUserLookup;

	@Test
	public void shouldHentFulltNavn() throws Exception {
		String fulltNavn = ldapAdeoUserLookup.hentFulltNavn("Z999990");
		assertThat(fulltNavn, is("REQ000000342741 testbrukere for kodeverksklienten"));
	}

	@Test
	public void shouldReturnNullWhenAdeoIdentNotFound() throws Exception {
		String fulltNavn = ldapAdeoUserLookup.hentFulltNavn("bxxxxxx");

		assertThat(fulltNavn, nullValue());
	}

	static class Config {
		@Bean
		static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
			return new PropertySourcesPlaceholderConfigurer();
		}
	}
}