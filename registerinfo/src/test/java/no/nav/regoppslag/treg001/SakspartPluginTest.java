package no.nav.regoppslag.treg001;

import no.nav.dok.brevdata.felles.v1.navfelles.Sakspart;
import no.nav.regoppslag.consumer.ereg.EregConsumer;
import no.nav.regoppslag.consumer.ereg.support.Bruksperiode;
import no.nav.regoppslag.consumer.ereg.support.Gyldighetsperiode;
import no.nav.regoppslag.consumer.ereg.support.Navn;
import no.nav.regoppslag.consumer.ereg.support.Organisasjon;
import no.nav.regoppslag.consumer.ereg.support.OrganisasjonDetaljer;
import no.nav.regoppslag.consumer.ereg.support.OrganisasjonEregMapper;
import no.nav.regoppslag.consumer.pdl.PdlGraphQLConsumer;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.regoppslag.service.LandkodeService;
import no.nav.regoppslag.service.PostnummerService;
import no.nav.regoppslag.treg001.support.SpraakKodeMapper;
import no.nav.regoppslag.treg001.xmlenricher.util.JaxbHelper;
import no.nav.regoppslag.treg001.xmlenricher.util.ValueMapKeys;
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
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static no.nav.regoppslag.util.PDLResponseUtil.FULLT_NAVN;
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
	private static final String ORGNAVN = "Firma AS";
	private static final String DOKUMENTTYPEID = "I000003";

	private PostnummerService postnummerService;
	private LandkodeService landkodeService;
	private EregConsumer eregConsumer;
	private Map<String, Object> valueMap;
	private SecurityContext securityContext;
	private SakspartPlugin sakspartPlugin;
	private PdlGraphQLConsumer pdlGraphQLConsumer;

	@BeforeEach
	public void setUp() throws RegOppslagSecurityException, IOException {
		pdlGraphQLConsumer = mock(PdlGraphQLConsumer.class);
		landkodeService = new LandkodeService();
		eregConsumer = mock(EregConsumer.class);
		securityContext = new SecurityContextImpl();
		postnummerService = new PostnummerService();
		valueMap = new HashMap<>();
		valueMap.put(ValueMapKeys.DOKUMENTTYPEID.name(), DOKUMENTTYPEID);
		valueMap.put(ValueMapKeys.PREFIXMAPPER.name(), null);
		valueMap.put(ValueMapKeys.MAALFORM.name(), new SpraakKodeMapper());
		SecurityContextHolder.setContext(securityContext);

		MicrometerMetrics metrics = mock(MicrometerMetrics.class);
		OrganisasjonEregMapper organisasjonEregMapper = new OrganisasjonEregMapper(postnummerService, landkodeService, metrics);
		sakspartPlugin = new SakspartPlugin(metrics, pdlGraphQLConsumer, eregConsumer, organisasjonEregMapper);
		when(eregConsumer.hentOrganisasjon(anyString())).thenReturn(createOrganisasjon(ORGNAVN));
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

		assertThat(sakspart.getNavn(), is(ORGNAVN));
	}

	@Test
	public void shouldThrowExceptionWhenMottakerManglerType() throws Exception {
		when(eregConsumer.hentOrganisasjon(anyString())).thenReturn(null);
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
		when(eregConsumer.hentOrganisasjon(anyString())).thenReturn(null);
		File xmlFile = new File(BREVDATA_ID);
		Document document = loadDocument(xmlFile);

		String expression1 = "/brevdata/*[local-name() = 'NAVFelles']//*[local-name() = 'sakspart']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);
		assertThrows(RegOppslagFunctionalException.class,
				() -> sakspartPlugin.processElement(node, valueMap, null), "Feil i SakspartPlugin: Sakspart mangler id");

	}

	public static Organisasjon createOrganisasjon(String navn) {
		Organisasjon organisasjon = new Organisasjon();
		OrganisasjonDetaljer organisasjonsDetaljer = new OrganisasjonDetaljer();
		Navn organisasjonKortnavn = new Navn();
		organisasjonKortnavn.setNavnelinje1(navn);
		organisasjonKortnavn.setSammensattnavn(navn);
		organisasjon.setNavn(organisasjonKortnavn);

		Navn organisasjonsnavn = new Navn();
		organisasjonsnavn.setNavnelinje1(navn);
		organisasjonsnavn.setSammensattnavn(navn);
		Bruksperiode bruksperiode = new Bruksperiode();
		bruksperiode.setFom(LocalDateTime.now().minusDays(1));
		organisasjonsnavn.setBruksperiode(bruksperiode);
		Gyldighetsperiode gyldighetsperiode = new Gyldighetsperiode();
		gyldighetsperiode.setFom(LocalDate.now().minusDays(1));
		organisasjonsnavn.setGyldighetsperiode(gyldighetsperiode);
		organisasjonsDetaljer.setNavn(Collections.singletonList(organisasjonsnavn));

		organisasjonsDetaljer.setMaalform("NB");
		organisasjonsDetaljer.setOpphoersdato(LocalDate.now().plusDays(10));
		organisasjon.setOrganisasjonDetaljer(organisasjonsDetaljer);

		return organisasjon;
	}
}