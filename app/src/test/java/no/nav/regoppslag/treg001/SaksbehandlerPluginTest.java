package no.nav.regoppslag.treg001;

import static no.nav.regoppslag.util.TestUtil.findSingleNode;
import static no.nav.regoppslag.util.TestUtil.loadDocument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import no.nav.dok.brevdata.felles.v1.navfelles.NavAnsatt;
import no.nav.regoppslag.config.ldap.LdapConfig;
import no.nav.regoppslag.consumer.ldap.LdapAdeoUserLookup;
import no.nav.regoppslag.consumer.ldap.support.SaksbehandlerMapper;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.regoppslag.xmlenricher.util.JaxbHelper;
import no.nav.regoppslag.xmlenricher.util.ValueMapKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.inject.Inject;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {LdapConfig.class, SaksbehandlerPluginTest.Config.class})
@TestPropertySource("classpath:ldap.properties")
public class SaksbehandlerPluginTest {
	public static final String BREVDATA1 = "src/test/resources/brevdata/eksempel1.xml";
	public static final String BREVDATA_IKKE_BERIK = "src/test/resources/brevdata/brevdata_ikkeBerik.xml";
	private static final String DOKUMENTTYPEID = "I000003";
	private SecurityContext securityContext = new SecurityContextImpl();
	private Map<String, Object> valueMap;
	private UsernamePasswordAuthenticationToken token;

	@Inject
	private LdapAdeoUserLookup ldapAdeoUserLookup;

	@Inject
	private SaksbehandlerPlugin saksbehandlerPlugin;

	@BeforeEach
	public void init() {
		token = new UsernamePasswordAuthenticationToken("username", "password");
		securityContext.setAuthentication(token);
		valueMap = new HashMap<>();
		valueMap.put(ValueMapKeys.DOKUMENTTYPEID.name(), DOKUMENTTYPEID);
		valueMap.put(ValueMapKeys.PREFIXMAPPER.name(), null);
		SecurityContextHolder.setContext(securityContext);

	}

	@Test
	public void testSaksbehandlerPlugin() throws Exception {
		when(ldapAdeoUserLookup.hentFulltNavn(anyString())).thenReturn("Test Testesen");

		File xmlFile = new File(BREVDATA1);
		Document document = loadDocument(xmlFile);

		String expression1 = "/brevdata/*[local-name()='NAVFelles']//*[local-name()='signerendeSaksbehandler']/*[local-name()='navAnsatt']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);

		Node processed = saksbehandlerPlugin.processElement(node, valueMap, null);

		JaxbHelper<NavAnsatt> mottakerJaxbHelper = new JaxbHelper<NavAnsatt>(NavAnsatt.class);
		NavAnsatt navAnsatt = mottakerJaxbHelper.unmarshal(processed);

		assertThat(navAnsatt.getNavn(), is("Test Testesen"));
	}

	@Test
	public void testSaksbehandlerPluginIkkeBerik() throws Exception {
		when(ldapAdeoUserLookup.hentFulltNavn(anyString())).thenReturn("Test Testesen");

		File xmlFile = new File(BREVDATA_IKKE_BERIK);
		Document document = loadDocument(xmlFile);

		String expression1 = "/brevdata/*[local-name()='NAVFelles']//*[local-name()='signerendeSaksbehandler']/*[local-name()='navAnsatt']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);

		Node processed = saksbehandlerPlugin.processElement(node, valueMap, null);

		JaxbHelper<NavAnsatt> mottakerJaxbHelper = new JaxbHelper<NavAnsatt>(NavAnsatt.class);
		NavAnsatt navAnsatt = mottakerJaxbHelper.unmarshal(processed);

		assertThat(navAnsatt.getNavn(), is("Ikke Berik"));
	}

	@Configuration
	static class Config {
		@Bean
		static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
			return new PropertySourcesPlaceholderConfigurer();
		}

		@Bean
		LdapAdeoUserLookup ldapAdeoUserLookup() {
			return mock(LdapAdeoUserLookup.class);
		}

		@Bean
		public SaksbehandlerPlugin saksbehandlerPlugin() {
			return new SaksbehandlerPlugin();
		}

		@Bean
		SaksbehandlerMapper saksbehandlerMapper() {
			return new SaksbehandlerMapper();
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