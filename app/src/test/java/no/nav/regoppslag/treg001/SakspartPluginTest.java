package no.nav.regoppslag.treg001;

import static no.nav.regoppslag.util.TestDataUtil.dateToGregorian;
import static no.nav.regoppslag.util.TestUtil.findSingleNode;
import static no.nav.regoppslag.util.TestUtil.loadDocument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import no.nav.dok.brevdata.felles.v1.navfelles.Sakspart;
import no.nav.dokkat.api.tkat020.v3.SpraakInfoTo;
import no.nav.regoppslag.consumer.dokkat.Tkat020DokumenttypeInfo;
import no.nav.regoppslag.consumer.organisasjonv4.OrganisasjonV4Consumer;
import no.nav.regoppslag.consumer.organisasjonv4.support.OrganisasjonV4Mapper;
import no.nav.regoppslag.consumer.personv3.PersonV3Consumer;
import no.nav.regoppslag.consumer.personv3.support.PersonV3Mapper;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.regoppslag.service.LandkodeService;
import no.nav.regoppslag.service.PostnummerService;
import no.nav.regoppslag.treg001.support.SpraakKodeMapper;
import no.nav.regoppslag.xmlenricher.util.JaxbHelper;
import no.nav.regoppslag.xmlenricher.util.ValueMapKeys;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.OrganisasjonsDetaljer;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjonsnavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personnavn;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
public class SakspartPluginTest {
	private static final String BREVDATA1 = "src/test/resources/brevdata/eksempel1.xml";
	private static final String BREVDATA_IKKE_BERIK = "src/test/resources/brevdata/brevdata_ikkeBerik.xml";
	private static final String BREVDATA_ORG = "src/test/resources/brevdata/brevdata_organisasjon.xml";
	private static final String BREVDATA_TYPE = "src/test/resources/brevdata/brevdata_type.xml";
	private static final String BREVDATA_ID = "src/test/resources/brevdata/brevdata_id.xml";

	private static final String IKKE_BERIK_FORNAVN = "Ikke";
	private static final String IKKE_BERIK_ETTERNAVN = "Berik";
	private static final String FORNAVN = "TOM";
	private static final String ETTERNAVN = "RIDDLE";
	private static final String ORGNAVN = "Orgnavn 1";
	private static final String ORGNAVN_2 = "Orgnavn_2";
	private static final String ORGKORTNAVN = "OrgKortnavn 1";
	private static final String ORGKORTNAVN_2 = "OrgKortnavn_2";
	private static final String DOKUMENTTYPEID = "I000003";

	private PersonV3Consumer personV3Consumer = mock(PersonV3Consumer.class);
	private PostnummerService postnummerService = new PostnummerService();
	private LandkodeService landkodeService = new LandkodeService();
	private OrganisasjonV4Consumer organisasjonV4Consumer = mock(OrganisasjonV4Consumer.class);
	private OrganisasjonV4Mapper organisasjonV4Mapper;
	private Tkat020DokumenttypeInfo tkat020DokumenttypeInfo = mock(Tkat020DokumenttypeInfo.class);
	private Map<String, Object> valueMap;
	private SecurityContext securityContext = new SecurityContextImpl();
	private MeterRegistry registry;
	private MicrometerMetrics metrics;
	private SakspartPlugin sakspartPlugin;
	private PersonV3Mapper personV3Mapper;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Before
	public void setUp() throws RegOppslagFunctionalException, RegOppslagTechnicalException, RegOppslagSecurityException, DatatypeConfigurationException {
		valueMap = new HashMap<>();
		valueMap.put(ValueMapKeys.DOKUMENTTYPEID.name(), DOKUMENTTYPEID);
		valueMap.put(ValueMapKeys.PREFIXMAPPER.name(), null);
		valueMap.put(ValueMapKeys.MAALFORM.name(), new SpraakKodeMapper());
		SecurityContextHolder.setContext(securityContext);

		registry = new SimpleMeterRegistry();
		metrics = mock(MicrometerMetrics.class);
		personV3Mapper = new PersonV3Mapper(postnummerService, landkodeService, metrics);
		organisasjonV4Mapper = new OrganisasjonV4Mapper(postnummerService, landkodeService, metrics);
		sakspartPlugin = new SakspartPlugin(personV3Consumer, personV3Mapper, organisasjonV4Consumer, organisasjonV4Mapper, tkat020DokumenttypeInfo, metrics);
		when(personV3Consumer.hentPerson(any(String.class), any(String.class), any(String.class))).thenReturn(createPerson(FORNAVN, null, ETTERNAVN));
		when(organisasjonV4Consumer.hentOrganisasjon(any(String.class), any(String.class))).thenReturn(createOrganisasjon(Arrays
				.asList(ORGNAVN, ORGNAVN_2), Arrays.asList(ORGKORTNAVN, ORGKORTNAVN_2)));
	}

	@Test
	public void testSakspartPluginPerson() throws Exception {
		File xmlFile = new File(BREVDATA1);
		Document document = loadDocument(xmlFile);

		String expression1 = "//*[local-name() = 'sakspart']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);
		
		Node node = findSingleNode(xPathExpression, document);
		
		Node processed = sakspartPlugin.processElement(node, valueMap);
		
		JaxbHelper<Sakspart> sakspartJaxbHelper = new JaxbHelper<>(Sakspart.class);
		Sakspart sakspart = sakspartJaxbHelper.unmarshal(processed);
		
		assertThat(sakspart.getNavn(), is(FORNAVN + " " + ETTERNAVN));
	}

	@Test
	public void testSakspartPluginPersonIkkeBerik() throws Exception {
		File xmlFile = new File(BREVDATA_IKKE_BERIK);
		Document document = loadDocument(xmlFile);

		String expression1 = "/brevdata/*[local-name() = 'NAVFelles']//*[local-name() = 'sakspart']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);

		Node processed = sakspartPlugin.processElement(node, valueMap);

		JaxbHelper<Sakspart> sakspartJaxbHelper = new JaxbHelper<Sakspart>(Sakspart.class);
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
		
		Node processed = sakspartPlugin.processElement(node, valueMap);
		JaxbHelper<Sakspart> sakspartJaxbHelper = new JaxbHelper<>(Sakspart.class);
		Sakspart sakspart = sakspartJaxbHelper.unmarshal(processed);
		
		assertThat(sakspart.getNavn(), is(ORGNAVN + " " + ORGNAVN_2));
	}

	@Test
	public void shouldThrowExceptionWhenMottakerManglerType() throws Exception {
		expectedException.expect(RegOppslagFunctionalException.class);
		expectedException.expectMessage("Feil i SakspartPlugin: Sakspart mangler AktoerTypeKode.");
		when(organisasjonV4Consumer.hentOrganisasjon(any(String.class), any(String.class))).thenReturn(null);
		File xmlFile = new File(BREVDATA_TYPE);
		Document document = loadDocument(xmlFile);

		String expression1 = "/brevdata/*[local-name() = 'NAVFelles']//*[local-name() = 'sakspart']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);
		
		Node node = findSingleNode(xPathExpression, document);
		sakspartPlugin.processElement(node, valueMap);
	}
	
	@Test
	public void shouldThrowExceptionWhenMottakerManglerId() throws Exception {
		expectedException.expect(RegOppslagFunctionalException.class);
		expectedException.expectMessage("Feil i SakspartPlugin: Sakspart mangler id");
		when(organisasjonV4Consumer.hentOrganisasjon(any(String.class), any(String.class))).thenReturn(null);
		File xmlFile = new File(BREVDATA_ID);
		Document document = loadDocument(xmlFile);

		String expression1 = "/brevdata/*[local-name() = 'NAVFelles']//*[local-name() = 'sakspart']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);
		
		Node node = findSingleNode(xPathExpression, document);
		
		sakspartPlugin.processElement(node, valueMap);
	}
	
	private Bruker createPerson(String fornavn, String mellomnavn, String etternavn) {
		Personnavn personnavn = new Personnavn();
		personnavn.setFornavn(fornavn);
		if (mellomnavn != null) {
			personnavn.setMellomnavn(mellomnavn);
			personnavn.setSammensattNavn(fornavn + " " + mellomnavn + " " + etternavn);
		} else {
			personnavn.setSammensattNavn(fornavn + " " + etternavn);
		}
		personnavn.setEtternavn(etternavn);
		Bruker person = new Bruker();
		person.setPersonnavn(personnavn);
		return person;
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
	
	private List<SpraakInfoTo> createTkatResponse(List<String> langs) {
		List<SpraakInfoTo> list = new ArrayList<>();
		langs.forEach(lang -> {
			SpraakInfoTo spraakInfoTo = new SpraakInfoTo();
			spraakInfoTo.setSpraaklag(lang);
			list.add(spraakInfoTo);
		});
		return list;
	}
}