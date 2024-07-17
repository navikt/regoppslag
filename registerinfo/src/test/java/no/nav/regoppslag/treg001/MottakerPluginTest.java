package no.nav.regoppslag.treg001;

import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.dok.brevdata.felles.v1.navfelles.UtenlandskPostadresse;
import no.nav.regoppslag.consumer.digdirkrr.DigitalKontaktinformasjon;
import no.nav.regoppslag.consumer.dokmet.DokmetConsumer;
import no.nav.regoppslag.consumer.ereg.EregConsumer;
import no.nav.regoppslag.consumer.ereg.support.Organisasjon;
import no.nav.regoppslag.consumer.ereg.support.OrganisasjonEregMapper;
import no.nav.regoppslag.consumer.pdl.PdlGraphQLConsumer;
import no.nav.regoppslag.consumer.pdl.to.HentPerson;
import no.nav.regoppslag.consumer.pdl.to.HentPerson.PersonNavn;
import no.nav.regoppslag.exceptions.FeilGrunnetHoeytVolumWorkaroundException;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.pdl.DoedsboAdresseService;
import no.nav.regoppslag.pdl.MapPDLResponse;
import no.nav.regoppslag.pdl.NorskAdresseService;
import no.nav.regoppslag.service.PostnummerService;
import no.nav.regoppslag.treg001.support.SpraakKodeMapper;
import no.nav.regoppslag.treg001.xmlenricher.util.JaxbHelper;
import no.nav.regoppslag.treg001.xmlenricher.util.ValueMapKeys;
import no.nav.regoppslag.util.TestDataUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.time.Clock;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;
import static no.nav.dok.brevdata.felles.v1.simpletypes.AktoerType.ORGANISASJON;
import static no.nav.dok.brevdata.felles.v1.simpletypes.AktoerType.PERSON;
import static no.nav.dok.brevdata.felles.v1.simpletypes.Spraakkode.NN;
import static no.nav.regoppslag.config.TimeConfig.OSLO_ZONE;
import static no.nav.regoppslag.treg001.util.CreateStubs.createTkatResponse;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
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

	private EregConsumer eregConsumer;
	private DokmetConsumer dokmetConsumer;
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
		dokmetConsumer = mock(DokmetConsumer.class);
		mapPDLResponse = new MapPDLResponse(new DoedsboAdresseService(postnummerService, pdlGraphQLConsumer), new NorskAdresseService(postnummerService), Clock.system(OSLO_ZONE));
		postnummerService = new PostnummerService();
		valueMap = new HashMap<>();
		valueMap.put(ValueMapKeys.DOKUMENTTYPEID.name(), DOKUMENTTYPEID);
		valueMap.put(ValueMapKeys.PREFIXMAPPER.name(), null);
		valueMap.put(ValueMapKeys.MAALFORM.name(), new SpraakKodeMapper());

		eregConsumer = mock(EregConsumer.class);
		OrganisasjonEregMapper organisasjonEregMapper = new OrganisasjonEregMapper(new PostnummerService());
		mapPdlForTreg001 = new MapPdlForTreg001(pdlGraphQLConsumer, mapPDLResponse, dokmetConsumer, digitalKontaktinformasjon, eregConsumer, organisasjonEregMapper);
		mottakerPlugin = new MottakerPlugin(mapPdlForTreg001);
	}

	@Test
	public void testMottakerPluginPerson() throws Exception {
		HentPerson hentPerson = createPdlHentPerson(createPersonNavn());

		when(pdlGraphQLConsumer.hentPerson(anyString())).thenReturn(hentPerson);

		File xmlFile = new File(BREVDATA1);
		Document document = loadDocument(xmlFile);

		String expression1 = "//*[local-name() = 'mottaker']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);

		Node processed = mottakerPlugin.processElement(node, valueMap);

		JaxbHelper<Mottaker> mottakerJaxbHelper = new JaxbHelper<>(Mottaker.class);
		Mottaker mottaker = mottakerJaxbHelper.unmarshal(processed);

		assertThat(mottaker.getNavn()).isEqualTo(FORNAVN + " " + ETTERNAVN);
	}

	@Test
	public void shouldUsePersonMaalform() throws Exception {
		HentPerson hentPerson = createPdlHentPerson(createPersonNavn());

		when(pdlGraphQLConsumer.hentPerson(anyString())).thenReturn(hentPerson);
		when(digitalKontaktinformasjon.hentSpraak(anyString(), anyBoolean())).thenReturn("EN");
		when(dokmetConsumer.hentDokumenttypeInfoSpraak(anyString())).thenReturn(createTkatResponse(Arrays.asList(SPRAAK_NB, "EN", "NN")));

		File xmlFile = new File(BREVDATA1);
		Document document = loadDocument(xmlFile);

		String expression1 = "//*[local-name() = 'mottaker']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);

		Node processed = mottakerPlugin.processElement(node, valueMap);

		JaxbHelper<Mottaker> mottakerJaxbHelper = new JaxbHelper<>(Mottaker.class);
		Mottaker mottaker = mottakerJaxbHelper.unmarshal(processed);
		assertThat(mottaker.getSpraakkode().value()).isEqualTo("EN");
	}

	@Test
	public void shouldUseMottakerMaalform() throws Exception {
		HentPerson hentPerson = createPdlHentPerson(createPersonNavn());

		when(pdlGraphQLConsumer.hentPerson(anyString())).thenReturn(hentPerson);
		when(digitalKontaktinformasjon.hentSpraak(anyString(), anyBoolean())).thenReturn("EN");
		when(dokmetConsumer.hentDokumenttypeInfoSpraak(anyString())).thenReturn(createTkatResponse(Arrays.asList(SPRAAK_NB, "EN")));

		File xmlFile = new File(BREVDATA_MOTTAKER_SPRAAKKODE_EN);
		Document document = loadDocument(xmlFile);

		String expression1 = "//*[local-name() = 'mottaker']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);

		Node processed = mottakerPlugin.processElement(node, valueMap);

		JaxbHelper<Mottaker> mottakerJaxbHelper = new JaxbHelper<>(Mottaker.class);
		Mottaker mottaker = mottakerJaxbHelper.unmarshal(processed);
		assertThat(mottaker.getSpraakkode().value()).isEqualTo("EN");
	}

	@Test
	public void testMottakerPluginPersonIkkeBerik() throws Exception {
		File xmlFile = new File(BREVDATA_IKKE_BERIK);
		Document document = loadDocument(xmlFile);

		String expression1 = "//*[local-name() = 'mottaker']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);

		Node processed = mottakerPlugin.processElement(node, valueMap);

		JaxbHelper<Mottaker> mottakerJaxbHelper = new JaxbHelper<>(Mottaker.class);
		Mottaker mottaker = mottakerJaxbHelper.unmarshal(processed);

		assertThat(mottaker.getNavn()).isEqualTo(IKKE_BERIK_FORNAVN + " " + IKKE_BERIK_ETTERNAVN);
		assertThat(mottaker.getId()).isEqualTo(MOTTAKER_ID);
		assertThat(mottaker.getTypeKode()).isEqualTo(PERSON);
		assertThat(((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1()).isEqualTo("ikkeberiket linje1");
	}

	@Test
	public void shouldMapMottakerPluginPersonFraPdl() throws Exception {
		when(pdlGraphQLConsumer.hentPerson(anyString())).thenReturn(createPdlHentPersonWithBostedsadresse());
		when(digitalKontaktinformasjon.hentSpraak(anyString(), anyBoolean())).thenReturn("NB");
		File xmlFile = new File(BREVDATA1);
		Document document = loadDocument(xmlFile);

		String expression1 = "//*[local-name() = 'mottaker']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);

		Node processed = mottakerPlugin.processElement(node, valueMap);

		JaxbHelper<Mottaker> mottakerJaxbHelper = new JaxbHelper<>(Mottaker.class);
		Mottaker mottaker = mottakerJaxbHelper.unmarshal(processed);
		NorskPostadresse adresse = (NorskPostadresse) mottaker.getMottakeradresse();

		assertThat(mottaker.getKortNavn()).isEqualTo(KORT_NAVN);
		assertThat(mottaker.getNavn()).isEqualTo(FULLT_NAVN);
		assertThat(adresse.getAdresselinje1()).isEqualTo(ADRESSENAVN_1);
		assertThat(adresse.getPoststed()).isEqualTo(POSTSTED);
		assertThat(adresse.getPostnummer()).isEqualTo(POSTNUMMER);
	}

	@Test
	public void shouldMapUtenlandskAdresseFraPdl() throws Exception {
		when(pdlGraphQLConsumer.hentPerson(anyString())).thenReturn(createPdlHentPersonUtenlandskAdresse());
		when(digitalKontaktinformasjon.hentSpraak(anyString(), anyBoolean())).thenReturn("NB");
		File xmlFile = new File(BREVDATA1);
		Document document = loadDocument(xmlFile);

		String expression1 = "//*[local-name() = 'mottaker']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);

		Node processed = mottakerPlugin.processElement(node, valueMap);

		JaxbHelper<Mottaker> mottakerJaxbHelper = new JaxbHelper<>(Mottaker.class);
		Mottaker mottaker = mottakerJaxbHelper.unmarshal(processed);
		UtenlandskPostadresse adresse = (UtenlandskPostadresse) mottaker.getMottakeradresse();

		assertThat(mottaker.getKortNavn()).isEqualTo(KORT_NAVN);
		assertThat(mottaker.getNavn()).isEqualTo(FULLT_NAVN);
		assertThat(adresse.getAdresselinje1()).isEqualTo(POSTBOKSNUMMERNAVN);
		assertThat(adresse.getAdresselinje2()).isEqualTo(POSTKODE_AND_BYSTED + ", " + STATE);
	}

	@Test
	public void testMottakerPluginOrganisasjon() throws Exception {
		when(eregConsumer.hentOrganisasjon(anyString())).thenReturn(createOrganisasjon());
		when(dokmetConsumer.hentDokumenttypeInfoSpraak(anyString())).thenReturn(createTkatResponse(singletonList(SPRAAK_NB)));
		when(dokmetConsumer.hentDokumenttypeInfoSpraak(anyString())).thenReturn(createTkatResponse(singletonList("NN")));

		File xmlFile = new File(BREVDATA_ORG);
		Document document = loadDocument(xmlFile);

		String expression1 = "/brevdata/*[local-name() = 'NAVFelles']//*[local-name() = 'mottaker']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);

		Node processed = mottakerPlugin.processElement(node, valueMap);
		JaxbHelper<Mottaker> mottakerJaxbHelper = new JaxbHelper<>(Mottaker.class);
		Mottaker mottaker = mottakerJaxbHelper.unmarshal(processed);

		assertThat(mottaker.getNavn()).isEqualTo(ORGNAVN);
		assertThat(mottaker.getTypeKode()).isEqualTo(ORGANISASJON);
		assertThat(mottaker.getId()).isEqualTo("974727854");
		assertThat(mottaker.getKortNavn()).isEqualTo(ORGNAVN);
		assertThat(mottaker.getSpraakkode()).isEqualTo(NN);
	}

	@Test
	public void shouldThrowExceptionWhenMottakerManglerAktoerType() throws Exception {
		File xmlFile = new File(BREVDATA_TYPE);
		Document document = loadDocument(xmlFile);

		String expression1 = "/brevdata/*[local-name() = 'NAVFelles']//*[local-name() = 'mottaker']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);
		assertThatExceptionOfType(FeilGrunnetHoeytVolumWorkaroundException.class)
				.isThrownBy(() -> mottakerPlugin.processElement(node, valueMap))
				.withMessageContaining("Feil i MottakerPlugin med feilmelding=Mottakerdata mangler AktoerType. AktoerType kan ikke være null.");
	}

	@Test
	public void shouldThrowExceptionWhenMottakerManglerMottakerId() throws Exception {
		File xmlFile = new File(BREVDATA_ID);
		Document document = loadDocument(xmlFile);

		String expression1 = "/brevdata/*[local-name() = 'NAVFelles']//*[local-name() = 'mottaker']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);
		assertThatExceptionOfType(FeilGrunnetHoeytVolumWorkaroundException.class)
				.isThrownBy(() -> mottakerPlugin.processElement(node, valueMap))
				.withMessageContaining("Feil i MottakerPlugin med feilmelding=Mottakerdata mangler mottakerId");
	}

	private static Organisasjon createOrganisasjon() {
		Organisasjon org = TestDataUtil.createOrganisasjon(ORGNAVN);
		settPostAdresse(org, "POSTADRESSE", 10000L);
		return org;
	}

	private PersonNavn createPersonNavn() {
		return PersonNavn.builder()
				.fornavn(FORNAVN)
				.etternavn(ETTERNAVN)
				.build();
	}
}