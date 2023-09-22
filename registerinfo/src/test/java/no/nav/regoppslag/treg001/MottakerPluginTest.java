package no.nav.regoppslag.treg001;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.dok.brevdata.felles.v1.navfelles.UtenlandskPostadresse;
import no.nav.dok.brevdata.felles.v1.simpletypes.Spraakkode;
import no.nav.regoppslag.consumer.digdirkrr.DigitalKontaktinformasjon;
import no.nav.regoppslag.consumer.dokmet.Tkat020DokumenttypeInfo;
import no.nav.regoppslag.consumer.ereg.EregConsumer;
import no.nav.regoppslag.consumer.ereg.support.Organisasjon;
import no.nav.regoppslag.consumer.ereg.support.OrganisasjonEregMapper;
import no.nav.regoppslag.consumer.pdl.PdlGraphQLConsumer;
import no.nav.regoppslag.consumer.pdl.to.HentPerson;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.regoppslag.pdl.DoedsboAdresseService;
import no.nav.regoppslag.pdl.MapPDLResponse;
import no.nav.regoppslag.pdl.NorskAdresseService;
import no.nav.regoppslag.service.PostnummerService;
import no.nav.regoppslag.treg001.support.SpraakKodeMapper;
import no.nav.regoppslag.treg001.util.CreateStubs;
import no.nav.regoppslag.treg001.xmlenricher.util.JaxbHelper;
import no.nav.regoppslag.treg001.xmlenricher.util.ValueMapKeys;
import no.nav.regoppslag.util.TestDataUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static no.nav.dok.brevdata.felles.v1.simpletypes.AktoerType.ORGANISASJON;
import static no.nav.dok.brevdata.felles.v1.simpletypes.AktoerType.PERSON;
import static no.nav.regoppslag.config.TimeConfig.OSLO_ZONE;
import static no.nav.regoppslag.util.PDLResponseUtil.ADRESSENAVN_1;
import static no.nav.regoppslag.util.PDLResponseUtil.FULLT_NAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.KORT_NAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTBOKSNUMMERNAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTKODE_AND_BYSTED;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTNUMMER;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTSTED;
import static no.nav.regoppslag.util.PDLResponseUtil.STATE;
import static no.nav.regoppslag.util.PDLResponseUtil.createPdlHentPerson;
import static no.nav.regoppslag.util.PDLResponseUtil.createPdlHentPersonUtenlandskAdresse;
import static no.nav.regoppslag.util.PDLResponseUtil.createPdlHentPersonWithBostedsadresse;
import static no.nav.regoppslag.util.TestDataUtil.settPostAdresse;
import static no.nav.regoppslag.util.TestUtil.findSingleNode;
import static no.nav.regoppslag.util.TestUtil.loadDocument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MottakerPluginTest {

	private static final String BREVDATA1 = "src/test/resources/brevdata/eksempel1.xml";
	private static final String BREVDATA_MOTTAKER_SPRAAKKODE_EN = "src/test/resources/brevdata/brevdata_mottaker_spraakkode_en.xml";
	private static final String BREVDATA_IKKE_BERIK = "src/test/resources/brevdata/brevdata_ikkeBerik.xml";
	private static final String BREVDATA_ORG = "src/test/resources/brevdata/brevdata_organisasjon.xml";
	private static final String BREVDATA_TYPE = "src/test/resources/brevdata/brevdata_type.xml";
	private static final String BREVDATA_ID = "src/test/resources/brevdata/brevdata_id.xml";

	private static final String IKKE_BERIK_FORNAVN = "Ikke";
	private static final String IKKE_BERIK_ETTERNAVN = "Berik";
	private static final String FORNAVN = "TOM";
	private static final String ETTERNAVN = "RIDDLE";
	private static final String ORGNAVN = "Orgnavn 1";
	private static final String DOKUMENTTYPEID = "I000003";
	private static final String SPRAAK_NB = "NB";
	private static final String MOTTAKER_ID = "30085849677";
	private static final String TEMA = "PEN";

	private EregConsumer eregConsumer;
	private Tkat020DokumenttypeInfo tkat020DokumenttypeInfo;
	private Map<String, Object> valueMap;
	private MottakerPlugin mottakerPlugin;
	private DigitalKontaktinformasjon digitalKontaktinformasjon;
	private PdlGraphQLConsumer pdlGraphQLConsumer;

	@InjectMocks
	private PostnummerService postnummerService;
	@InjectMocks
	private MapPdlForTreg001 mapPdlForTreg001;


	@InjectMocks
	private MapPDLResponse mapPDLResponse;

	@BeforeEach
	public void setUp() throws RegOppslagSecurityException, IOException {
		pdlGraphQLConsumer = mock(PdlGraphQLConsumer.class);
		digitalKontaktinformasjon = mock(DigitalKontaktinformasjon.class);
		tkat020DokumenttypeInfo = mock(Tkat020DokumenttypeInfo.class);
		mapPDLResponse = new MapPDLResponse(new DoedsboAdresseService(postnummerService, pdlGraphQLConsumer), new NorskAdresseService(postnummerService), Clock.system(OSLO_ZONE));
		postnummerService = new PostnummerService();
		valueMap = new HashMap<>();
		valueMap.put(ValueMapKeys.DOKUMENTTYPEID.name(), DOKUMENTTYPEID);
		valueMap.put(ValueMapKeys.PREFIXMAPPER.name(), null);
		valueMap.put(ValueMapKeys.MAALFORM.name(), new SpraakKodeMapper());

		MicrometerMetrics metrics = new MicrometerMetrics();
		MeterRegistry registry = new SimpleMeterRegistry();
		ReflectionTestUtils.setField(metrics, "registry", registry);

		eregConsumer = mock(EregConsumer.class);
		OrganisasjonEregMapper organisasjonEregMapper = new OrganisasjonEregMapper(new PostnummerService(), mock(MicrometerMetrics.class));
		mapPdlForTreg001 = new MapPdlForTreg001(pdlGraphQLConsumer, mapPDLResponse, tkat020DokumenttypeInfo, digitalKontaktinformasjon, eregConsumer, organisasjonEregMapper);
		mottakerPlugin = new MottakerPlugin(mapPdlForTreg001, metrics);
	}

	@Test
	public void testMottakerPluginPerson() throws Exception {
		HentPerson hentPerson = createPdlHentPerson(createPersonNavn());

		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(hentPerson);

		File xmlFile = new File(BREVDATA1);
		Document document = loadDocument(xmlFile);

		String expression1 = "//*[local-name() = 'mottaker']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);

		Node processed = mottakerPlugin.processElement(node, valueMap, "GEN");

		JaxbHelper<Mottaker> mottakerJaxbHelper = new JaxbHelper<>(Mottaker.class);
		Mottaker mottaker = mottakerJaxbHelper.unmarshal(processed);

		assertThat(mottaker.getNavn(), is(FORNAVN + " " + ETTERNAVN));
	}

	@Test
	public void shouldUsePersonMaalform() throws Exception {
		HentPerson hentPerson = createPdlHentPerson(createPersonNavn());

		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(hentPerson);
		when(digitalKontaktinformasjon.hentSpraak(anyString(), anyBoolean())).thenReturn("EN");
		when(tkat020DokumenttypeInfo.hentDokumenttypeInfoSpraak(anyString())).thenReturn(CreateStubs.createTkatResponse(Arrays.asList(SPRAAK_NB, "EN", "NN")));

		File xmlFile = new File(BREVDATA1);
		Document document = loadDocument(xmlFile);

		String expression1 = "//*[local-name() = 'mottaker']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);

		Node processed = mottakerPlugin.processElement(node, valueMap, "FOR");

		JaxbHelper<Mottaker> mottakerJaxbHelper = new JaxbHelper<>(Mottaker.class);
		Mottaker mottaker = mottakerJaxbHelper.unmarshal(processed);
		assertThat(mottaker.getSpraakkode().value(), is("EN"));
	}

	@Test
	public void shouldUseMottakerMaalform() throws Exception {
		HentPerson hentPerson = createPdlHentPerson(createPersonNavn());

		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(hentPerson);
		when(digitalKontaktinformasjon.hentSpraak(anyString(), anyBoolean())).thenReturn("EN");
		when(tkat020DokumenttypeInfo.hentDokumenttypeInfoSpraak(anyString())).thenReturn(CreateStubs.createTkatResponse(Arrays.asList(SPRAAK_NB, "EN")));

		File xmlFile = new File(BREVDATA_MOTTAKER_SPRAAKKODE_EN);
		Document document = loadDocument(xmlFile);

		String expression1 = "//*[local-name() = 'mottaker']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);

		Node processed = mottakerPlugin.processElement(node, valueMap, "GEN");

		JaxbHelper<Mottaker> mottakerJaxbHelper = new JaxbHelper<>(Mottaker.class);
		Mottaker mottaker = mottakerJaxbHelper.unmarshal(processed);
		assertThat(mottaker.getSpraakkode().value(), is("EN"));
	}

	@Test
	public void testMottakerPluginPersonIkkeBerik() throws Exception {
		File xmlFile = new File(BREVDATA_IKKE_BERIK);
		Document document = loadDocument(xmlFile);

		String expression1 = "//*[local-name() = 'mottaker']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);

		Node processed = mottakerPlugin.processElement(node, valueMap, null);

		JaxbHelper<Mottaker> mottakerJaxbHelper = new JaxbHelper<>(Mottaker.class);
		Mottaker mottaker = mottakerJaxbHelper.unmarshal(processed);

		assertThat(mottaker.getNavn(), is(IKKE_BERIK_FORNAVN + " " + IKKE_BERIK_ETTERNAVN));
		assertThat(mottaker.getId(), is(MOTTAKER_ID));
		assertThat(mottaker.getTypeKode(), is(PERSON));
		assertThat(((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1(), is("ikkeberiket linje1"));
	}

	@Test
	public void shouldMapMottakerPluginPersonFraPdl() throws Exception {
		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(createPdlHentPersonWithBostedsadresse());
		when(digitalKontaktinformasjon.hentSpraak(anyString(), anyBoolean())).thenReturn("NB");
		File xmlFile = new File(BREVDATA1);
		Document document = loadDocument(xmlFile);

		String expression1 = "//*[local-name() = 'mottaker']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);

		Node processed = mottakerPlugin.processElement(node, valueMap, TEMA);

		JaxbHelper<Mottaker> mottakerJaxbHelper = new JaxbHelper<>(Mottaker.class);
		Mottaker mottaker = mottakerJaxbHelper.unmarshal(processed);
		NorskPostadresse adresse = (NorskPostadresse) mottaker.getMottakeradresse();

		assertEquals(KORT_NAVN, mottaker.getKortNavn());
		assertEquals(FULLT_NAVN, mottaker.getNavn());
		assertEquals(ADRESSENAVN_1, adresse.getAdresselinje1());
		assertEquals(POSTSTED, adresse.getPoststed());
		assertEquals(POSTNUMMER, adresse.getPostnummer());
	}

	@Test
	public void shouldMapUtenlandskAdresseFraPdl() throws Exception {
		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(createPdlHentPersonUtenlandskAdresse());
		when(digitalKontaktinformasjon.hentSpraak(anyString(), anyBoolean())).thenReturn("NB");
		File xmlFile = new File(BREVDATA1);
		Document document = loadDocument(xmlFile);

		String expression1 = "//*[local-name() = 'mottaker']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);

		Node processed = mottakerPlugin.processElement(node, valueMap, TEMA);

		JaxbHelper<Mottaker> mottakerJaxbHelper = new JaxbHelper<>(Mottaker.class);
		Mottaker mottaker = mottakerJaxbHelper.unmarshal(processed);
		UtenlandskPostadresse adresse = (UtenlandskPostadresse) mottaker.getMottakeradresse();

		assertEquals(KORT_NAVN, mottaker.getKortNavn());
		assertEquals(FULLT_NAVN, mottaker.getNavn());
		assertEquals(POSTBOKSNUMMERNAVN, adresse.getAdresselinje1());
		assertEquals(POSTKODE_AND_BYSTED + ", " + STATE, adresse.getAdresselinje2());
	}

	@Test
	public void testMottakerPluginOrganisasjon() throws Exception {
		when(eregConsumer.hentOrganisasjon(anyString())).thenReturn(createOrganisasjon());
		when(tkat020DokumenttypeInfo.hentDokumenttypeInfoSpraak(anyString())).thenReturn(CreateStubs.createTkatResponse(Collections.singletonList(SPRAAK_NB)));
		when(tkat020DokumenttypeInfo.hentDokumenttypeInfoSpraak(anyString())).thenReturn(CreateStubs.createTkatResponse(Collections.singletonList("NN")));

		File xmlFile = new File(BREVDATA_ORG);
		Document document = loadDocument(xmlFile);

		String expression1 = "/brevdata/*[local-name() = 'NAVFelles']//*[local-name() = 'mottaker']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);

		Node processed = mottakerPlugin.processElement(node, valueMap, null);
		JaxbHelper<Mottaker> mottakerJaxbHelper = new JaxbHelper<>(Mottaker.class);
		Mottaker mottaker = mottakerJaxbHelper.unmarshal(processed);

		assertThat(mottaker.getNavn(), is(ORGNAVN));
		assertThat(mottaker.getTypeKode(), is(ORGANISASJON));
		assertThat(mottaker.getId(), is("974727854"));
		assertThat(mottaker.getKortNavn(), is(ORGNAVN));
		assertThat(mottaker.getSpraakkode(), is(Spraakkode.NN));
	}

	@Test
	public void shouldThrowExceptionWhenMottakerManglerType() throws Exception {
		File xmlFile = new File(BREVDATA_TYPE);
		Document document = loadDocument(xmlFile);

		String expression1 = "/brevdata/*[local-name() = 'NAVFelles']//*[local-name() = 'mottaker']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);
		RegOppslagFunctionalException exception = assertThrows(RegOppslagFunctionalException.class,
				() -> mottakerPlugin.processElement(node, valueMap, null),
				"Feil i MottakerPlugin: Mottakerdata mangler AktoerType. AktoerType kan ikke være null.");
		assertEquals(exception.getMessage(), "Feil i MottakerPlugin: Mottakerdata mangler AktoerType. AktoerType kan ikke være null.");
	}

	@Test
	public void shouldThrowExceptionWhenMottakerManglerId() throws Exception {
		File xmlFile = new File(BREVDATA_ID);
		Document document = loadDocument(xmlFile);

		String expression1 = "/brevdata/*[local-name() = 'NAVFelles']//*[local-name() = 'mottaker']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);
		assertThrows(RegOppslagFunctionalException.class,
				() -> mottakerPlugin.processElement(node, valueMap, null), "Feil i MottakerPlugin: Mottakerdata mangler mottakerId");

	}

	private static Organisasjon createOrganisasjon() {
		Organisasjon org = TestDataUtil.createOrganisasjon(ORGNAVN);
		settPostAdresse(org, "POSTADRESSE", 10000L);
		return org;
	}

	private HentPerson.PersonNavn createPersonNavn() {
		return HentPerson.PersonNavn.builder().fornavn(FORNAVN).etternavn(ETTERNAVN).build();
	}
}