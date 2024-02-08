package no.nav.regoppslag.treg001;

import no.nav.dok.brevdata.felles.v1.navfelles.NavEnhet;
import no.nav.regoppslag.consumer.norg2.OrganisasjonsenhetConsumer;
import no.nav.regoppslag.consumer.norg2.support.Norg2Mapper;
import no.nav.regoppslag.consumer.norg2.to.EnhetNavn;
import no.nav.regoppslag.service.PostnummerService;
import no.nav.regoppslag.treg001.xmlenricher.util.JaxbHelper;
import no.nav.regoppslag.treg001.xmlenricher.util.ValueMapKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static no.nav.regoppslag.util.TestUtil.findSingleNode;
import static no.nav.regoppslag.util.TestUtil.loadDocument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class NavOrgenhetNavnPluginTest {

	public static final String BREVDATA1 = "src/test/resources/brevdata/eksempel1.xml";
	public static final String BREVDATA_8020 = "src/test/resources/brevdata/brevdata_behandlendeEnhet_8020.xml";
	public static final String BREVDATA_IKKE_BERIK = "src/test/resources/brevdata/brevdata_ikkeBerik.xml";

	private static final String NAV_ENHET_NAVN = "NAV Husnes";
	private static final String DOKUMENTTYPEID = "I000003";

	private NavOrgenhetNavnPlugin norgPlugin;
	private Map<String, Object> valueMap;

	@BeforeEach
	public void setUp() throws Exception {
		OrganisasjonsenhetConsumer norgConsumer = Mockito.mock(OrganisasjonsenhetConsumer.class);
		PostnummerService postnummerService = new PostnummerService();
		valueMap = new HashMap<>();
		valueMap.put(ValueMapKeys.DOKUMENTTYPEID.name(), DOKUMENTTYPEID);
		valueMap.put(ValueMapKeys.PREFIXMAPPER.name(), null);

		Norg2Mapper norg2Mapper = new Norg2Mapper(postnummerService);
		norgPlugin = new NavOrgenhetNavnPlugin(norgConsumer, norg2Mapper);
		when(norgConsumer.hentEnhetNavn(anyString())).thenReturn(createEnhet(NAV_ENHET_NAVN));
	}

	@Test
	public void testNavOrgenhetNavnPlugin() throws Exception {
		File xmlFile = new File(BREVDATA1);
		Document document = loadDocument(xmlFile);

		String expression1 = "/brevdata/*[local-name()='NAVFelles']//*[local-name()='signerendeSaksbehandler']/*[local-name()='navEnhet']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);

		Node processed = norgPlugin.processElement(node, valueMap);

		JaxbHelper<NavEnhet> enhetJaxbHelper = new JaxbHelper<>(NavEnhet.class);
		NavEnhet navEnhet = enhetJaxbHelper.unmarshal(processed);

		assertThat(navEnhet.getEnhetsNavn(), is(NAV_ENHET_NAVN));
	}

	@Test
	public void testNavOrgenhetNavnPluginWithBehandlendeEnhet() throws Exception {
		File xmlFile = new File(BREVDATA1);
		Document document = loadDocument(xmlFile);

		String expression1 = "/brevdata/*[local-name()='NAVFelles']//*[local-name()='behandlendeEnhet']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);

		Node processed = norgPlugin.processElement(node, valueMap);

		JaxbHelper<NavEnhet> enhetJaxbHelper = new JaxbHelper<>(NavEnhet.class);
		NavEnhet navEnhet = enhetJaxbHelper.unmarshal(processed);

		assertThat(navEnhet.getEnhetsNavn(), is(NAV_ENHET_NAVN));
	}

	@Test
	public void testNavOrgenhetNavnPluginIkkeBerik() throws Exception {
		File xmlFile = new File(BREVDATA_IKKE_BERIK);
		Document document = loadDocument(xmlFile);

		String expression1 = "/brevdata/*[local-name()='NAVFelles']//*[local-name()='signerendeSaksbehandler']/*[local-name()='navEnhet']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);

		Node processed = norgPlugin.processElement(node, valueMap);

		JaxbHelper<NavEnhet> enhetJaxbHelper = new JaxbHelper<>(NavEnhet.class);
		NavEnhet navEnhet = enhetJaxbHelper.unmarshal(processed);

		assertThat(navEnhet.getEnhetsNavn(), is("Ikke berik"));
	}

	@Test
	public void testNavOrgenhetNavnPluginIkkeBerikBehandlendeEnhetIfBerikIsFalse() throws Exception {
		File xmlFile = new File(BREVDATA_8020);
		Document document = loadDocument(xmlFile);

		String expression1 = "/brevdata/*[local-name()='NAVFelles']//*[local-name()='behandlendeEnhet']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);

		Node processed = norgPlugin.processElement(node, valueMap);

		JaxbHelper<NavEnhet> enhetJaxbHelper = new JaxbHelper<>(NavEnhet.class);
		NavEnhet navEnhet = enhetJaxbHelper.unmarshal(processed);

		assertThat(navEnhet.getEnhetsNavn(), nullValue());
	}


	private EnhetNavn createEnhet(String navEnhetNavn) {
		return EnhetNavn.builder()
				.navn(navEnhetNavn)
				.build();
	}
}