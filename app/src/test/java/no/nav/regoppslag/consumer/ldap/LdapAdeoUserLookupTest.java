package no.nav.regoppslag.consumer.ldap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import no.nav.regoppslag.config.ldap.LdapConfig;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Inject
	private LdapAdeoUserLookup ldapAdeoUserLookup;

	@Test
	public void shouldHentFulltNavn() throws Exception {
		String fulltNavn = ldapAdeoUserLookup.hentFulltNavn("Z999990");
		assertThat(fulltNavn, is("REQ000000342741 testbrukere for kodeverksklienten"));
	}

	@Test
	public void shouldThrowExceptionWhenAdeoIdentNotFound() throws Exception {
		thrown.expect(RegOppslagFunctionalException.class);
		thrown.expectMessage("Ldap.hentFulltNavn finner ikke bruker med ident=bxxxxxx");
		String fulltNavn = ldapAdeoUserLookup.hentFulltNavn("bxxxxxx");
	}

	static class Config {
		@Bean
		static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
			return new PropertySourcesPlaceholderConfigurer();
		}

		@Bean
		public MeterRegistry registry() {
			return new SimpleMeterRegistry();
		}

		@Bean
		public MicrometerMetrics metrics() {
			return new MicrometerMetrics();
		}

	}
}