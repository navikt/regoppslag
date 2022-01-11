package no.nav.regoppslag.treg001;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import no.nav.dok.brevdata.felles.v1.navfelles.Postadresse;
import no.nav.regoppslag.consumer.norg2.OrganisasjonEnhetKontaktinformasjonV1Consumer;
import no.nav.regoppslag.consumer.norg2.support.Norg2Mapper;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.regoppslag.service.PostnummerService;
import no.nav.regoppslag.treg001.xmlenricher.util.JaxbHelper;
import no.nav.regoppslag.treg001.xmlenricher.util.ValueMapKeys;
import no.nav.regoppslag.util.TestUtil;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.Organisasjonsenhet;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static no.nav.regoppslag.util.TestUtil.*;
import static no.nav.regoppslag.util.TestUtil.loadDocument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class NavOrgenhetPluginTest {
	public static final String BREVDATA1 = "src/test/resources/brevdata/eksempel1.xml";
	public static final String BREVDATA_IKKE_BERIK = "src/test/resources/brevdata/brevdata_ikkeBerik.xml";

	private static final String NAV_ENHET_NAVN = "Pensjon Inc.";
	private static final String DOKUMENTTYPEID = "I000003";

	private OrganisasjonEnhetKontaktinformasjonV1Consumer norgConsumer;
	private PostnummerService postnummerService;
	private Norg2Mapper norg2Mapper;
	private NavOrgenhetPostadressePlugin norgPostadressePlugin;
	private NavOrgenhetBesoksadressePlugin norgBesoksadressePlugin;
	private SecurityContext securityContext = new SecurityContextImpl();
	private Map<String, Object> valueMap;
	private MicrometerMetrics metrics;
	private MeterRegistry registry;

	@BeforeEach
	public void setUp() throws Exception {
		norgConsumer = mock(OrganisasjonEnhetKontaktinformasjonV1Consumer.class);
		valueMap = new HashMap<>();
		valueMap.put(ValueMapKeys.DOKUMENTTYPEID.name(), DOKUMENTTYPEID);
		valueMap.put(ValueMapKeys.PREFIXMAPPER.name(), null);
		SecurityContextHolder.setContext(securityContext);
		postnummerService = new PostnummerService();

		postnummerService.init();
		registry = mock(SimpleMeterRegistry.class);
		metrics = mock(MicrometerMetrics.class);
		norg2Mapper = new Norg2Mapper(postnummerService);
		norgPostadressePlugin = new NavOrgenhetPostadressePlugin(norgConsumer, norg2Mapper, metrics);
		norgBesoksadressePlugin = new NavOrgenhetBesoksadressePlugin(norgConsumer, norg2Mapper, metrics);

		when(norgConsumer.hentKontaktinformasjonForEnhet(anyString())).thenReturn(createEnhet(NAV_ENHET_NAVN));
	}

	@Test
	public void testOrgEnhetPostadressePlugin() throws Exception {
		File xmlFile = new File(BREVDATA1);

		Document document = loadDocument(xmlFile);

		String expression1 = "/brevdata/*[local-name()='NAVFelles']//*[local-name()='kontaktinformasjon']/*[local-name()='postadresse']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);
		Node processed = norgPostadressePlugin.processElement(node, valueMap, null);

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
		Node processed = norgPostadressePlugin.processElement(node, valueMap, null);

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
		Node processed = norgPostadressePlugin.processElement(node, valueMap, null);

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
		Node processed = norgPostadressePlugin.processElement(node, valueMap, null);

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
		Node processed = norgBesoksadressePlugin.processElement(node, valueMap, null);
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
		Node processed = norgBesoksadressePlugin.processElement(node, valueMap, null);
		JaxbHelper<Postadresse> enhetJaxbHelper = new JaxbHelper<Postadresse>(Postadresse.class);
		Postadresse postadresse = enhetJaxbHelper.unmarshal(processed);
		assertThat(postadresse.getEnhetsNavn(), is("Ikke beriket besøksadresse"));
		assertThat(postadresse.getAdresse().getAdresselinje1(), is("ikkeberiket besøksadresse linje1"));
	}

	private Organisasjonsenhet createEnhet(String navEnhetNavn) {
		Organisasjonsenhet enhet = new Organisasjonsenhet();
		enhet.setEnhetNavn(navEnhetNavn);
		return enhet;
	}
}