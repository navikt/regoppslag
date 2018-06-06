package no.nav.regoppslag.treg001;

import static no.nav.regoppslag.util.TestUtil.findSingleNode;
import static no.nav.regoppslag.util.TestUtil.loadDocument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import no.nav.dok.brevdata.felles.v1.navfelles.NavEnhet;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
public class NavOrgenhetNavnPluginTest {
	public static final String BREVDATA1 = "src/test/resources/brevdata/eksempel1.xml";

	private static final String NAV_ENHET_NAVN = "NAV Husnes";
	private static final String DOKUMENTTYPEID = "I000003";

	private OrganisasjonEnhetKontaktinformasjonV1Consumer norgConsumer = Mockito.mock(OrganisasjonEnhetKontaktinformasjonV1Consumer.class);
	private PostnummerService postnummerService = new PostnummerService();
	private Norg2Mapper norg2Mapper;
	private NavOrgenhetNavnPlugin norgPlugin;
	private Map<String, Object> valueMap;
	
	private SecurityContext securityContext = new SecurityContextImpl();
	private UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("username", "password");
	
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		securityContext.setAuthentication(token);
		valueMap = new HashMap<>();
		valueMap.put(ValueMapKeys.DOKUMENTTYPEID.name(), DOKUMENTTYPEID);
		valueMap.put(ValueMapKeys.PREFIXMAPPER.name(), null);
		SecurityContextHolder.setContext(securityContext);
		postnummerService.init();
		norg2Mapper = new Norg2Mapper(postnummerService);
		norgPlugin = new NavOrgenhetNavnPlugin(norgConsumer, norg2Mapper);
		when(norgConsumer.hentKontaktinformasjonForEnhet(any(String.class))).thenReturn(createEnhet(NAV_ENHET_NAVN));
	}

	@Test
	public void testNavOrgenhetNavnPlugin() throws Exception {
		File xmlFile = new File(BREVDATA1);
		Document document = loadDocument(xmlFile);

		String expression1 = "//felles:signerendeSaksbehandler/saksbehandler:navEnhet";
		XPath xPath = XPathFactory.newInstance().newXPath();
		NamespaceContext namespaceContext = new RegisteroppslagNamespaceContext();
		xPath.setNamespaceContext(namespaceContext);
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);
		
		Node processed = norgPlugin.processElement(node, valueMap);

		JaxbHelper<NavEnhet> enhetJaxbHelper = new JaxbHelper<NavEnhet>(NavEnhet.class);
		NavEnhet navEnhet = enhetJaxbHelper.unmarshal(processed);

		assertThat(navEnhet.getEnhetsNavn(), is(NAV_ENHET_NAVN));
	}

	@Test
	public void throwFuncErrorWhenNavOrgenhentIkkeFunnet() throws Exception {
		expectedException.expect(RegOppslagFunctionalException.class);
		expectedException.expectMessage("Feil i NavOrgenhetNavnPlugin:  Kunne ikke finne enhet. EnhetId=");
		when(norgConsumer.hentKontaktinformasjonForEnhet(any(String.class))).thenReturn(null);
		File xmlFile = new File(BREVDATA1);
		Document document = loadDocument(xmlFile);

		String expression1 = "//felles:signerendeSaksbehandler/saksbehandler:navEnhet";
		XPath xPath = XPathFactory.newInstance().newXPath();
		NamespaceContext namespaceContext = new RegisteroppslagNamespaceContext();
		xPath.setNamespaceContext(namespaceContext);
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);
		
		norgPlugin.processElement(node, valueMap);
	}

	private Organisasjonsenhet createEnhet(String navEnhetNavn) {
		Organisasjonsenhet enhet = new Organisasjonsenhet();
		enhet.setEnhetNavn(navEnhetNavn);
		return enhet;
	}
}