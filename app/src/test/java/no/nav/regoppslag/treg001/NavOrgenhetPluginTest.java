package no.nav.regoppslag.treg001;

import static no.nav.regoppslag.util.TestUtil.findSingleNode;
import static no.nav.regoppslag.util.TestUtil.loadDocument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import no.nav.dok.metaforcemal.jaxb2.gen.Postadresse;
import no.nav.regoppslag.consumer.norg2.OrganisasjonEnhetKontaktinformasjonV1Consumer;
import no.nav.regoppslag.consumer.norg2.support.Norg2Mapper;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.service.PostnummerService;
import no.nav.regoppslag.xmlenricher.util.JaxbHelper;
import no.nav.regoppslag.xmlenricher.util.RegisteroppslagNamespaceContext;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.Organisasjonsenhet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;

@RunWith(SpringJUnit4ClassRunner.class)
public class NavOrgenhetPluginTest {
	public static final String BREVDATA1 = "src/test/resources/brevdata/eksempel1.xml";

	private static final String NAV_ENHET_NAVN = "Pensjon Inc.";
	private static final String DOKUMENTTYPEID = "I000003";

	private OrganisasjonEnhetKontaktinformasjonV1Consumer norgConsumer = Mockito.mock(OrganisasjonEnhetKontaktinformasjonV1Consumer.class);
	private PostnummerService postnummerService = new PostnummerService();
	private Norg2Mapper norg2Mapper;
	private NavOrgenhetPostadressePlugin norgPostadressePlugin;
	private NavOrgenhetBesoksadressePlugin norgBesoksadressePlugin;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		postnummerService.init();
		norg2Mapper = new Norg2Mapper(postnummerService);
		norgPostadressePlugin = new NavOrgenhetPostadressePlugin(norgConsumer, norg2Mapper);
		norgBesoksadressePlugin = new NavOrgenhetBesoksadressePlugin(norgConsumer, norg2Mapper);
		when(norgConsumer.hentKontaktinformasjonForEnhet(any(String.class))).thenReturn(createEnhet(NAV_ENHET_NAVN));
	}
	@Test
	public void testOrgEnhetPostadressePlugin() throws Exception {
		File xmlFile = new File(BREVDATA1);

		Document document = loadDocument(xmlFile);

		String expression1 = "//felles:kontaktinformasjon/kontaktinformasjon:postadresse";
		XPath xPath = XPathFactory.newInstance().newXPath();
		NamespaceContext namespaceContext = new RegisteroppslagNamespaceContext();
		xPath.setNamespaceContext(namespaceContext);
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);
		Node processed = norgPostadressePlugin.processElement(node, DOKUMENTTYPEID, null);

		JaxbHelper<Postadresse> enhetJaxbHelper = new JaxbHelper<Postadresse>(Postadresse.class);
		Postadresse postadresse = enhetJaxbHelper.unmarshal(processed);

		assertThat(postadresse.getEnhetsNavn(), is(NAV_ENHET_NAVN));
	}

	@Test
	public void testOrgEnhetBesoksadressePlugin() throws Exception {
		File xmlFile = new File(BREVDATA1);

		Document document = loadDocument(xmlFile);

		String expression1 = "//felles:kontaktinformasjon/kontaktinformasjon:besoksadresse";
		XPath xPath = XPathFactory.newInstance().newXPath();
		NamespaceContext namespaceContext = new RegisteroppslagNamespaceContext();
		xPath.setNamespaceContext(namespaceContext);
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);
		Node processed = norgBesoksadressePlugin.processElement(node, DOKUMENTTYPEID, null);
		JaxbHelper<Postadresse> enhetJaxbHelper = new JaxbHelper<Postadresse>(Postadresse.class);
		Postadresse postadresse = enhetJaxbHelper.unmarshal(processed);
		assertThat(postadresse.getEnhetsNavn(), is(NAV_ENHET_NAVN));
	}

	@Test
	public void throwFuncErrorWhenOrgEnhetIkkeFunnetPostadressePlugin() throws Exception {
		expectedException.expect(RegOppslagFunctionalException.class);
		expectedException.expectMessage("Feil i NavOrgenhetPostadressePlugin:  Kunne ikke finne enhet. enhetId=");
		when(norgConsumer.hentKontaktinformasjonForEnhet(any(String.class))).thenReturn(null);
		File xmlFile = new File(BREVDATA1);

		Document document = loadDocument(xmlFile);

		String expression1 = "//felles:kontaktinformasjon/kontaktinformasjon:postadresse";
		XPath xPath = XPathFactory.newInstance().newXPath();
		NamespaceContext namespaceContext = new RegisteroppslagNamespaceContext();
		xPath.setNamespaceContext(namespaceContext);
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);
		norgPostadressePlugin.processElement(node, DOKUMENTTYPEID, null);
	}

	@Test
	public void throwFuncErrorWhenOrgEnhetIkkeFunnetBesoksadressePlugin() throws Exception {
		expectedException.expect(RegOppslagFunctionalException.class);
		expectedException.expectMessage("Feil i NavOrgenhetBesoksadressePlugin:  Kunne ikke finne enhet. enhetId=");
		when(norgConsumer.hentKontaktinformasjonForEnhet(any(String.class))).thenReturn(null);
		File xmlFile = new File(BREVDATA1);

		Document document = loadDocument(xmlFile);

		String expression1 = "//felles:kontaktinformasjon/kontaktinformasjon:besoksadresse";
		XPath xPath = XPathFactory.newInstance().newXPath();
		NamespaceContext namespaceContext = new RegisteroppslagNamespaceContext();
		xPath.setNamespaceContext(namespaceContext);
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);
		norgBesoksadressePlugin.processElement(node, DOKUMENTTYPEID, null);
	}


	private Organisasjonsenhet createEnhet(String navEnhetNavn) {
		Organisasjonsenhet enhet = new Organisasjonsenhet();
		enhet.setEnhetNavn(navEnhetNavn);
		return enhet;
	}
}