package no.nav.regoppslag.treg001;

import static no.nav.regoppslag.util.TestUtil.findSingleNode;
import static no.nav.regoppslag.util.TestUtil.loadDocument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import no.nav.dok.brevdata.felles.v1.navfelles.Postadresse;
import no.nav.regoppslag.consumer.norg2.OrganisasjonEnhetKontaktinformasjonV1Consumer;
import no.nav.regoppslag.consumer.norg2.support.Norg2Mapper;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.service.PostnummerService;
import no.nav.regoppslag.xmlenricher.util.JaxbHelper;
import no.nav.regoppslag.xmlenricher.util.RegisteroppslagNamespaceContext;
import no.nav.regoppslag.xmlenricher.util.ValueMapKeys;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.Organisasjonsenhet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
public class NavOrgenhetPluginTest {
	public static final String BREVDATA1 = "src/test/resources/brevdata/eksempel1.xml";
	public static final String BREVDATA_IKKE_BERIK = "src/test/resources/brevdata/brevdata_ikkeBerik.xml";

	private static final String NAV_ENHET_NAVN = "Pensjon Inc.";
	private static final String DOKUMENTTYPEID = "I000003";
	
	private OrganisasjonEnhetKontaktinformasjonV1Consumer norgConsumer = Mockito.mock(OrganisasjonEnhetKontaktinformasjonV1Consumer.class);
	private PostnummerService postnummerService = new PostnummerService();
	private Norg2Mapper norg2Mapper;
	private NavOrgenhetPostadressePlugin norgPostadressePlugin;
	private NavOrgenhetBesoksadressePlugin norgBesoksadressePlugin;
	private SecurityContext securityContext = new SecurityContextImpl();
	private Map<String, Object> valueMap;
	
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Before
	public void setUp() throws Exception {
		valueMap = new HashMap<>();
		valueMap.put(ValueMapKeys.DOKUMENTTYPEID.name(), DOKUMENTTYPEID);
		valueMap.put(ValueMapKeys.PREFIXMAPPER.name(), null);
		SecurityContextHolder.setContext(securityContext);
		
		
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

		String expression1 = "/brevdata/*[local-name()='NAVFelles']//*[local-name()='kontaktinformasjon']/*[local-name()='postadresse']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);
		
		Node node = findSingleNode(xPathExpression, document);
		Node processed = norgPostadressePlugin.processElement(node, valueMap);
		
		JaxbHelper<Postadresse> enhetJaxbHelper = new JaxbHelper<Postadresse>(Postadresse.class);
		Postadresse postadresse = enhetJaxbHelper.unmarshal(processed);
		
		assertThat(postadresse.getEnhetsNavn(), is(NAV_ENHET_NAVN));
	}

	@Test
	public void testOrgEnhetPostadresseIkkeBerikPlugin() throws Exception {
		File xmlFile = new File(BREVDATA_IKKE_BERIK);

		Document document = loadDocument(xmlFile);

		String expression1 = "/brevdata/*[local-name()='NAVFelles']//*[local-name()='kontaktinformasjon']/*[local-name()='postadresse']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);
		Node processed = norgPostadressePlugin.processElement(node, valueMap);

		JaxbHelper<Postadresse> enhetJaxbHelper = new JaxbHelper<Postadresse>(Postadresse.class);
		Postadresse postadresse = enhetJaxbHelper.unmarshal(processed);

		assertThat(postadresse.getEnhetsNavn(), is("Ikke berik"));
		assertThat(postadresse.getAdresse().getAdresselinje1(), is("ikkeberiket linje1"));
	}

	@Test
	public void testOrgEnhetReturadressePlugin() throws Exception {
		File xmlFile = new File(BREVDATA1);

		Document document = loadDocument(xmlFile);

		String expression1 = "/brevdata/*[local-name()='NAVFelles']//*[local-name()='kontaktinformasjon']/*[local-name()='returadresse']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);
		Node processed = norgPostadressePlugin.processElement(node, valueMap);

		JaxbHelper<Postadresse> enhetJaxbHelper = new JaxbHelper<Postadresse>(Postadresse.class);
		Postadresse postadresse = enhetJaxbHelper.unmarshal(processed);

		assertThat(postadresse.getEnhetsNavn(), is(NAV_ENHET_NAVN));
	}

	@Test
	public void testOrgEnhetReturadresseIkkeBerikPlugin() throws Exception {
		File xmlFile = new File(BREVDATA_IKKE_BERIK);

		Document document = loadDocument(xmlFile);

		String expression1 = "/brevdata/*[local-name()='NAVFelles']//*[local-name()='kontaktinformasjon']/*[local-name()='returadresse']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);
		Node processed = norgPostadressePlugin.processElement(node, valueMap);

		JaxbHelper<Postadresse> enhetJaxbHelper = new JaxbHelper<Postadresse>(Postadresse.class);
		Postadresse postadresse = enhetJaxbHelper.unmarshal(processed);

		assertThat(postadresse.getEnhetsNavn(), is("Ikke beriket returadresse"));
		assertThat(postadresse.getAdresse().getAdresselinje1(), is("ikkeberiket returadresse linje1"));
	}

	@Test
	public void testOrgEnhetBesoksadressePlugin() throws Exception {
		File xmlFile = new File(BREVDATA1);
		
		Document document = loadDocument(xmlFile);

		String expression1 = "/brevdata/*[local-name()='NAVFelles']//*[local-name()='kontaktinformasjon']/*[local-name()='besoksadresse']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);
		
		Node node = findSingleNode(xPathExpression, document);
		Node processed = norgBesoksadressePlugin.processElement(node, valueMap);
		JaxbHelper<Postadresse> enhetJaxbHelper = new JaxbHelper<Postadresse>(Postadresse.class);
		Postadresse postadresse = enhetJaxbHelper.unmarshal(processed);
		assertThat(postadresse.getEnhetsNavn(), is(NAV_ENHET_NAVN));
	}

	@Test
	public void testOrgEnhetBesoksadresseIkkeBerikPlugin() throws Exception {
		File xmlFile = new File(BREVDATA_IKKE_BERIK);

		Document document = loadDocument(xmlFile);

		String expression1 = "/brevdata/*[local-name()='NAVFelles']//*[local-name()='kontaktinformasjon']/*[local-name()='besoksadresse']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);
		Node processed = norgBesoksadressePlugin.processElement(node, valueMap);
		JaxbHelper<Postadresse> enhetJaxbHelper = new JaxbHelper<Postadresse>(Postadresse.class);
		Postadresse postadresse = enhetJaxbHelper.unmarshal(processed);
		assertThat(postadresse.getEnhetsNavn(), is("Ikke beriket besøksadresse"));
		assertThat(postadresse.getAdresse().getAdresselinje1(), is("ikkeberiket besøksadresse linje1"));
	}

	@Test
	public void throwFuncErrorWhenOrgEnhetIkkeFunnetPostadressePlugin() throws Exception {
		expectedException.expect(RegOppslagFunctionalException.class);
		expectedException.expectMessage("Feil i NavOrgenhetPostadressePlugin:  Kunne ikke finne enhet. EnhetId=TKN5427");
		when(norgConsumer.hentKontaktinformasjonForEnhet(any(String.class))).thenReturn(null);
		File xmlFile = new File(BREVDATA1);
		
		Document document = loadDocument(xmlFile);
		
		String expression1 = "/brevdata/*[local-name()='NAVFelles']//*[local-name()='kontaktinformasjon']/*[local-name()='postadresse']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);
		
		Node node = findSingleNode(xPathExpression, document);
		norgPostadressePlugin.processElement(node, valueMap);
	}
	
	@Test
	public void throwFuncErrorWhenOrgEnhetIkkeFunnetBesoksadressePlugin() throws Exception {
		expectedException.expect(RegOppslagFunctionalException.class);
		expectedException.expectMessage("Feil i NavOrgenhetBesoksadressePlugin:  Kunne ikke finne enhet. EnhetsId=TKN5427");
		when(norgConsumer.hentKontaktinformasjonForEnhet(any(String.class))).thenReturn(null);
		File xmlFile = new File(BREVDATA1);
		
		Document document = loadDocument(xmlFile);

		String expression1 = "/brevdata/*[local-name()='NAVFelles']//*[local-name()='kontaktinformasjon']/*[local-name()='besoksadresse']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);
		
		Node node = findSingleNode(xPathExpression, document);
		norgBesoksadressePlugin.processElement(node, valueMap);
	}
	
	
	private Organisasjonsenhet createEnhet(String navEnhetNavn) {
		Organisasjonsenhet enhet = new Organisasjonsenhet();
		enhet.setEnhetNavn(navEnhetNavn);
		return enhet;
	}
}