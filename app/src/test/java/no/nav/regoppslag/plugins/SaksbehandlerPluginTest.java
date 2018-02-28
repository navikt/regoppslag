package no.nav.regoppslag.plugins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static no.nav.regoppslag.util.TestUtil.findSingleNode;
import static no.nav.regoppslag.util.TestUtil.loadDocument;
import static no.nav.regoppslag.util.TestUtil.writeXml;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.nav.dok.metaforcemal.jaxb2.gen.Saksbehandler;
import no.nav.modig.core.util.LdapUtils;
import no.nav.regoppslag.config.ldap.LdapConfig;
import no.nav.regoppslag.ldap.LdapAdeoUserLookup;
import no.nav.regoppslag.ldap.LdapAdeoUserLookupTest;
import no.nav.regoppslag.ldap.support.SaksbehandlerMapper;
import no.nav.regoppslag.treg001.plugins.SaksbehandlerPlugin;
import no.nav.regoppslag.xmlenricher.util.JaxbHelper;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.inject.Inject;
import javax.xml.namespace.QName;
import java.io.File;
import java.util.ArrayList;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {LdapConfig.class, SaksbehandlerPluginTest.Config.class})
@TestPropertySource("classpath:ldap.properties")
public class SaksbehandlerPluginTest {
	public static final String BREVDATA1 = "src/test/resources/brevdata/eksempel1.xml";

	@Inject
	private LdapAdeoUserLookup ldapAdeoUserLookup;

	@Inject
	private SaksbehandlerPlugin saksbehandlerPlugin;

	@Test
	public void testPlugin1() throws Exception {
		when(ldapAdeoUserLookup.hentFulltNavn(any(String.class))).thenReturn("Test Testesen");

		File xmlFile = new File(BREVDATA1);
		Document document = loadDocument(xmlFile);

		QName qName = new QName("http://nav.no/dok/pesysbrev/felles/v1/PesysFelles","signerendeSaksbehandler");
		Node node = findSingleNode(qName, document);

		writeXml(node);

		Node processed = saksbehandlerPlugin.processElement(node);
		writeXml(processed);

		JaxbHelper<Saksbehandler> mottakerJaxbHelper = new JaxbHelper<Saksbehandler>(Saksbehandler.class);
		Saksbehandler saksbehandler= mottakerJaxbHelper.unmarshal(processed);

		assertThat(saksbehandler.getNavn(), is("Test Testesen"));
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