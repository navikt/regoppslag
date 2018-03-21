package no.nav.regoppslag.treg001.plugins;

import static no.nav.regoppslag.util.TestUtil.findSingleNode;
import static no.nav.regoppslag.util.TestUtil.loadDocument;
import static no.nav.regoppslag.util.TestUtil.writeXml;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.nav.dok.metaforcemal.jaxb2.gen.NavAnsatt;
import no.nav.regoppslag.config.ldap.LdapConfig;
import no.nav.regoppslag.consumer.ldap.LdapAdeoUserLookup;
import no.nav.regoppslag.consumer.ldap.support.SaksbehandlerMapper;
import no.nav.regoppslag.xmlenricher.util.JaxbHelper;
import no.nav.regoppslag.xmlenricher.util.RegisteroppslagNamespaceContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.inject.Inject;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {LdapConfig.class, SaksbehandlerPluginTest.Config.class})
@TestPropertySource("classpath:ldap.properties")
public class SaksbehandlerPluginTest {
	public static final String BREVDATA1 = "src/test/resources/brevdata/eksempel1.xml";
	private static final String DOKUMENTTYPEID = "I000003";

	@Inject
	private LdapAdeoUserLookup ldapAdeoUserLookup;

	@Inject
	private SaksbehandlerPlugin saksbehandlerPlugin;

	@Test
	public void testSaksbehandlerPlugin() throws Exception {
		when(ldapAdeoUserLookup.hentFulltNavn(any(String.class))).thenReturn("Test Testesen");

		File xmlFile = new File(BREVDATA1);
		Document document = loadDocument(xmlFile);

		String expression1 = "//felles:signerendeSaksbehandler/saksbehandler:navAnsatt";
		XPath xPath = XPathFactory.newInstance().newXPath();
		NamespaceContext namespaceContext = new RegisteroppslagNamespaceContext();
		xPath.setNamespaceContext(namespaceContext);
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);

		writeXml(node);

		Node processed = saksbehandlerPlugin.processElement(node, DOKUMENTTYPEID, null);
		writeXml(processed);

		JaxbHelper<NavAnsatt> mottakerJaxbHelper = new JaxbHelper<NavAnsatt>(NavAnsatt.class);
		NavAnsatt navAnsatt = mottakerJaxbHelper.unmarshal(processed);

		assertThat(navAnsatt.getNavn(), is("Test Testesen"));
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

	}
}