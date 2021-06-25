package no.nav.regoppslag.consumer.ldap;

import static no.nav.regoppslag.consumer.ldap.LdapAdeoUserLookup.HENT_FULLT_NAVN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import no.nav.regoppslag.config.ldap.LdapConfig;
import no.nav.regoppslag.itest.config.CacheTestConfig;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import java.util.ArrayList;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {LdapConfig.class, CacheTestConfig.class, LdapAdeoUserLookupCacheTest.Config.class})
@TestPropertySource("classpath:ldap.properties")
@ActiveProfiles("itest")
public class LdapAdeoUserLookupCacheTest {

	@Inject
	private CacheManager cacheManager;

	@Inject
	private LdapTemplate ldapTemplate;

	@Inject
	private LdapAdeoUserLookup ldapAdeoUserLookup;

	private static final String NAME1 = "Test Testesen";
	private static final String NAME2 = "Nils Nilsen";

	@Test
	public void shouldCache() throws Exception {
		cacheManager.getCache(HENT_FULLT_NAVN).clear();

		when(ldapTemplate.search(any(LdapQuery.class), ArgumentMatchers.<AttributesMapper<String>>any())).thenReturn(new ArrayList<String>() {{
			add(NAME1);
		}});
		ldapAdeoUserLookup.hentFulltNavn("Z999990");

		when(ldapTemplate.search(any(LdapQuery.class), ArgumentMatchers.<AttributesMapper<String>>any())).thenReturn(new ArrayList<String>() {{
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