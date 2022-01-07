package no.nav.regoppslag.treg001;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import no.nav.dok.brevdata.felles.v1.navfelles.Sakspart;
import no.nav.regoppslag.consumer.organisasjonv4.OrganisasjonV4Consumer;
import no.nav.regoppslag.consumer.organisasjonv4.support.OrganisasjonV4Mapper;
import no.nav.regoppslag.consumer.pdl.PdlGraphQLConsumer;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.regoppslag.service.LandkodeService;
import no.nav.regoppslag.service.PostnummerService;
import no.nav.regoppslag.treg001.support.SpraakKodeMapper;
import no.nav.regoppslag.treg001.xmlenricher.util.JaxbHelper;
import no.nav.regoppslag.treg001.xmlenricher.util.ValueMapKeys;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.OrganisasjonsDetaljer;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjonsnavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static no.nav.regoppslag.util.PDLResponseUtil.FULLT_NAVN;
import static no.nav.regoppslag.util.TestDataUtil.dateToGregorian;
import static no.nav.regoppslag.util.TestUtil.findSingleNode;
import static no.nav.regoppslag.util.TestUtil.loadDocument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class SakspartPluginTest {
	private static final String BREVDATA1 = "src/test/resources/brevdata/eksempel1.xml";
	private static final String BREVDATA_IKKE_BERIK = "src/test/resources/brevdata/brevdata_ikkeBerik.xml";
	private static final String BREVDATA_ORG = "src/test/resources/brevdata/brevdata_organisasjon.xml";
	private static final String BREVDATA_TYPE = "src/test/resources/brevdata/brevdata_type.xml";
	private static final String BREVDATA_ID = "src/test/resources/brevdata/brevdata_id.xml";

	private static final String TEMA = "PEN";
	private static final String IKKE_BERIK_FORNAVN = "Ikke";
	private static final String IKKE_BERIK_ETTERNAVN = "Berik";
	private static final String ORGNAVN = "Orgnavn 1";
	private static final String ORGNAVN_2 = "Orgnavn_2";
	private static final String ORGKORTNAVN = "OrgKortnavn 1";
	private static final String ORGKORTNAVN_2 = "OrgKortnavn_2";
	private static final String DOKUMENTTYPEID = "I000003";

	private PostnummerService postnummerService;
	private LandkodeService landkodeService;
	private OrganisasjonV4Consumer organisasjonV4Consumer;
	private Map<String, Object> valueMap;
	private SecurityContext securityContext;
	private SakspartPlugin sakspartPlugin;
	private PdlGraphQLConsumer pdlGraphQLConsumer;

	@BeforeEach
	public void setUp() throws RegOppslagSecurityException, DatatypeConfigurationException, IOException {
		pdlGraphQLConsumer = mock(PdlGraphQLConsumer.class);
		landkodeService = new LandkodeService();
		organisasjonV4Consumer = mock(OrganisasjonV4Consumer.class);
		securityContext = new SecurityContextImpl();
		postnummerService = new PostnummerService();
		valueMap = new HashMap<>();
		valueMap.put(ValueMapKeys.DOKUMENTTYPEID.name(), DOKUMENTTYPEID);
		valueMap.put(ValueMapKeys.PREFIXMAPPER.name(), null);
		valueMap.put(ValueMapKeys.MAALFORM.name(), new SpraakKodeMapper());
		SecurityContextHolder.setContext(securityContext);

		MeterRegistry registry = new SimpleMeterRegistry();
		MicrometerMetrics metrics = mock(MicrometerMetrics.class);
		OrganisasjonV4Mapper organisasjonV4Mapper = new OrganisasjonV4Mapper(postnummerService, landkodeService, metrics);
		sakspartPlugin = new SakspartPlugin(organisasjonV4Consumer, organisasjonV4Mapper, metrics, pdlGraphQLConsumer);
		when(organisasjonV4Consumer.hentOrganisasjon(anyString())).thenReturn(createOrganisasjon(Arrays
				.asList(ORGNAVN, ORGNAVN_2), Arrays.asList(ORGKORTNAVN, ORGKORTNAVN_2)));
	}

	@Test
	public void testSakspartPluginPDL() throws Exception {

		when(pdlGraphQLConsumer.hentNavn(anyString(), anyString())).thenReturn(FULLT_NAVN);
		File xmlFile = new File(BREVDATA1);
		Document document = loadDocument(xmlFile);

		String expression1 = "//*[local-name() = 'sakspart']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);

		Node processed = sakspartPlugin.processElement(node, valueMap, TEMA);

		JaxbHelper<Sakspart> sakspartJaxbHelper = new JaxbHelper<>(Sakspart.class);
		Sakspart sakspart = sakspartJaxbHelper.unmarshal(processed);

		assertEquals(FULLT_NAVN, sakspart.getNavn());
	}

	@Test
	public void testSakspartPluginPDLReturnNull() throws Exception {

		when(pdlGraphQLConsumer.hentNavn(anyString(), anyString())).thenReturn(null);
		File xmlFile = new File(BREVDATA1);
		Document document = loadDocument(xmlFile);

		String expression1 = "//*[local-name() = 'sakspart']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);

		Node processed = sakspartPlugin.processElement(node, valueMap, TEMA);

		JaxbHelper<Sakspart> sakspartJaxbHelper = new JaxbHelper<>(Sakspart.class);
		Sakspart sakspart = sakspartJaxbHelper.unmarshal(processed);

		assertNull(sakspart.getNavn());
	}

	@Test
	public void testSakspartPluginPersonIkkeBerik() throws Exception {
		File xmlFile = new File(BREVDATA_IKKE_BERIK);
		Document document = loadDocument(xmlFile);

		String expression1 = "/brevdata/*[local-name() = 'NAVFelles']//*[local-name() = 'sakspart']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);

		Node processed = sakspartPlugin.processElement(node, valueMap, null);

		JaxbHelper<Sakspart> sakspartJaxbHelper = new JaxbHelper<>(Sakspart.class);
		Sakspart sakspart = sakspartJaxbHelper.unmarshal(processed);

		assertThat(sakspart.getNavn(), is(IKKE_BERIK_FORNAVN + " " + IKKE_BERIK_ETTERNAVN));
	}

	@Test
	public void testSakspartPluginOrganisasjon() throws Exception {
		File xmlFile = new File(BREVDATA_ORG);
		Document document = loadDocument(xmlFile);

		String expression1 = "/brevdata/*[local-name() = 'NAVFelles']//*[local-name() = 'sakspart']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);

		Node processed = sakspartPlugin.processElement(node, valueMap, null);
		JaxbHelper<Sakspart> sakspartJaxbHelper = new JaxbHelper<>(Sakspart.class);
		Sakspart sakspart = sakspartJaxbHelper.unmarshal(processed);

		assertThat(sakspart.getNavn(), is(ORGNAVN + " " + ORGNAVN_2));
	}

	@Test
	public void shouldThrowExceptionWhenMottakerManglerType() throws Exception {
		when(organisasjonV4Consumer.hentOrganisasjon(anyString())).thenReturn(null);
		File xmlFile = new File(BREVDATA_TYPE);
		Document document = loadDocument(xmlFile);

		String expression1 = "/brevdata/*[local-name() = 'NAVFelles']//*[local-name() = 'sakspart']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);
		assertThrows(RegOppslagFunctionalException.class,
				() -> sakspartPlugin.processElement(node, valueMap, null), "Feil i SakspartPlugin: Sakspart mangler AktoerTypeKode.");
	}

	@Test
	public void shouldThrowExceptionWhenMottakerManglerId() throws Exception {
		when(organisasjonV4Consumer.hentOrganisasjon(anyString())).thenReturn(null);
		File xmlFile = new File(BREVDATA_ID);
		Document document = loadDocument(xmlFile);

		String expression1 = "/brevdata/*[local-name() = 'NAVFelles']//*[local-name() = 'sakspart']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);
		assertThrows(RegOppslagFunctionalException.class,
				() -> sakspartPlugin.processElement(node, valueMap, null), "Feil i SakspartPlugin: Sakspart mangler id");

	}

	private Organisasjon createOrganisasjon(List<String> orgNavn, List<String> orgKortnavn) throws DatatypeConfigurationException {
		Organisasjon organisasjon = new Organisasjon();
		OrganisasjonsDetaljer organisasjonsDetaljer = new OrganisasjonsDetaljer();
		UstrukturertNavn organisasjonKortnavn = new UstrukturertNavn();
		organisasjonKortnavn.getNavnelinje().addAll(orgKortnavn);
		organisasjon.setNavn(organisasjonKortnavn);

		UstrukturertNavn orgDetNavn = new UstrukturertNavn();
		orgDetNavn.getNavnelinje().addAll(orgNavn);
		Organisasjonsnavn organisasjonsnavn = new Organisasjonsnavn();
		organisasjonsnavn.setNavn(orgDetNavn);
		organisasjonsnavn.setFomGyldighetsperiode(dateToGregorian(LocalDate.now().minusDays(1)));
		organisasjonsnavn.setFomBruksperiode(dateToGregorian(LocalDate.now().minusDays(1)));
		organisasjonsDetaljer.getNavn().add(organisasjonsnavn);

		organisasjon.setOrganisasjonDetaljer(organisasjonsDetaljer);

		return organisasjon;
	}
}