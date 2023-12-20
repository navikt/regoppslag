package no.nav.regoppslag.treg001;

import no.nav.dok.brevdata.felles.v1.navfelles.Postadresse;
import no.nav.regoppslag.consumer.norg2.OrganisasjonsenhetConsumer;
import no.nav.regoppslag.consumer.norg2.support.Norg2Mapper;
import no.nav.regoppslag.consumer.norg2.to.EnhetKontaktinformasjon;
import no.nav.regoppslag.consumer.norg2.to.EnhetNavn;
import no.nav.regoppslag.service.PostnummerService;
import no.nav.regoppslag.treg001.xmlenricher.util.JaxbHelper;
import no.nav.regoppslag.treg001.xmlenricher.util.ValueMapKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class NavOrgenhetPluginTest {

	public static final String BREVDATA1 = "src/test/resources/brevdata/eksempel1.xml";
	public static final String BREVDATA_IKKE_BERIK = "src/test/resources/brevdata/brevdata_ikkeBerik.xml";

	private static final String NAV_ENHET_NAVN = "Pensjon Inc.";
	private static final String DOKUMENTTYPEID = "I000003";

	private NavOrgenhetPostadressePlugin norgPostadressePlugin;
	private NavOrgenhetBesoksadressePlugin norgBesoksadressePlugin;
	private Map<String, Object> valueMap;

	@BeforeEach
	public void setUp() throws Exception {
		OrganisasjonsenhetConsumer norgConsumer = mock(OrganisasjonsenhetConsumer.class);
		valueMap = new HashMap<>();
		valueMap.put(ValueMapKeys.DOKUMENTTYPEID.name(), DOKUMENTTYPEID);
		valueMap.put(ValueMapKeys.PREFIXMAPPER.name(), null);
		PostnummerService postnummerService = new PostnummerService();

		Norg2Mapper norg2Mapper = new Norg2Mapper(postnummerService);
		norgPostadressePlugin = new NavOrgenhetPostadressePlugin(norgConsumer, norg2Mapper);
		norgBesoksadressePlugin = new NavOrgenhetBesoksadressePlugin(norgConsumer, norg2Mapper);

		when(norgConsumer.hentEnhetNavn(anyString())).thenReturn(createEnhet(NAV_ENHET_NAVN));
		when(norgConsumer.hentEnhetKontaktinformasjon(anyString())).thenReturn(EnhetKontaktinformasjon.builder().build());
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

	private EnhetNavn createEnhet(String navEnhetNavn) {
		return EnhetNavn.builder()
				.navn(navEnhetNavn)
				.build();
	}
}