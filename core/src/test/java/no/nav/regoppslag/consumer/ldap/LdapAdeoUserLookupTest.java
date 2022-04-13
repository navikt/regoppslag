package no.nav.regoppslag.consumer.ldap;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import no.nav.regoppslag.config.ldap.LdapConfig;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {LdapConfig.class, LdapAdeoUserLookupTest.Config.class})
@TestPropertySource("classpath:ldap.properties")
public class LdapAdeoUserLookupTest {

	private static final LdapTemplate ldapTemplateMock = mock(LdapTemplate.class);

	@Autowired
	private LdapAdeoUserLookup ldapAdeoUserLookup;

	@Test
	public void shouldHentFulltNavn() {
		when(ldapTemplateMock.search(any(LdapQuery.class), any(AttributesMapper.class))).thenReturn(Collections.singletonList("Itest Itestesen"));
		String fulltNavn = ldapAdeoUserLookup.hentFulltNavn("Z999990");
		assertEquals("Itest Itestesen", fulltNavn);
	}

	@Test
	public void shouldThrowExceptionWhenAdeoIdentNotFound() {
		when(ldapTemplateMock.search(any(LdapQuery.class), any(AttributesMapper.class))).thenReturn(Collections.emptyList());
		assertThrows(RegOppslagFunctionalException.class, () ->
				ldapAdeoUserLookup.hentFulltNavn("bxxxxxx"), "Ldap.hentFulltNavn finner ikke bruker med ident=bxxxxxx");
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