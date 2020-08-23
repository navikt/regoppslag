package no.nav.regoppslag.consumer.ldap;

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
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {LdapConfig.class, LdapAdeoUserLookupTest.Config.class})
@TestPropertySource("classpath:ldap.properties")
public class LdapAdeoUserLookupTest {

	private static final LdapTemplate ldapTemplateMock = mock(LdapTemplate.class);

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Inject
	private LdapAdeoUserLookup ldapAdeoUserLookup;

	@Test
	public void shouldHentFulltNavn() throws Exception {
		when(ldapTemplateMock.search(any(LdapQuery.class), any(AttributesMapper.class))).thenReturn(Collections.singletonList("Itest Itestesen"));
		String fulltNavn = ldapAdeoUserLookup.hentFulltNavn("Z999990");
		assertThat(fulltNavn, is("Itest Itestesen"));
	}

	@Test
	public void shouldThrowExceptionWhenAdeoIdentNotFound() throws Exception {
		when(ldapTemplateMock.search(any(LdapQuery.class), any(AttributesMapper.class))).thenReturn(Collections.emptyList());
		thrown.expect(RegOppslagFunctionalException.class);
		thrown.expectMessage("Ldap.hentFulltNavn finner ikke bruker med ident=bxxxxxx");
		ldapAdeoUserLookup.hentFulltNavn("bxxxxxx");
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

		@Bean
		public LdapTemplate ldapTemplate() {
			return ldapTemplateMock;
		}

	}
}