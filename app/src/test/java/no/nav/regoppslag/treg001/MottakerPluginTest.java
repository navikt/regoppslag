package no.nav.regoppslag.treg001;

import static no.nav.regoppslag.util.TestUtil.findSingleNode;
import static no.nav.regoppslag.util.TestUtil.loadDocument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.dokkat.api.tkat020.v3.SpraakInfoTo;
import no.nav.regoppslag.consumer.dokkat.Tkat020DokumenttypeInfo;
import no.nav.regoppslag.consumer.organisasjonv4.OrganisasjonV4Consumer;
import no.nav.regoppslag.consumer.organisasjonv4.support.OrganisasjonV4Mapper;
import no.nav.regoppslag.consumer.personv3.PersonV3Consumer;
import no.nav.regoppslag.consumer.personv3.support.PersonV3Mapper;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.service.LandkodeService;
import no.nav.regoppslag.service.PostnummerService;
import no.nav.regoppslag.treg001.support.Maalform;
import no.nav.regoppslag.xmlenricher.util.JaxbHelper;
import no.nav.regoppslag.xmlenricher.util.ValueMapKeys;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.OrganisasjonsDetaljer;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjonsnavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Landkoder;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personnavn;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Postadresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Postadressetyper;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.UstrukturertAdresse;
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

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
public class MottakerPluginTest {
	public static final String BREVDATA1 = "src/test/resources/brevdata/eksempel1.xml";
	public static final String BREVDATA_IKKE_BERIK = "src/test/resources/brevdata/brevdata_ikkeBerik.xml";
	public static final String BREVDATA_ORG = "src/test/resources/brevdata/brevdata_organisasjon.xml";
	public static final String BREVDATA_TYPE = "src/test/resources/brevdata/brevdata_type.xml";
	public static final String BREVDATA_ID = "src/test/resources/brevdata/brevdata_id.xml";

	private static final String IKKE_BERIK_FORNAVN = "Ikke";
	private static final String IKKE_BERIK_ETTERNAVN = "Berik";
	private static final String FORNAVN = "TOM";
	private static final String ETTERNAVN = "RIDDLE";
	private static final String ORGNAVN = "Orgnavn 1";
	private static final String ORGNAVN_2 = "Orgnavn_2";
	private static final String ORGKORTNAVN = "OrgKortnavn 1";
	private static final String ORGKORTNAVN_2 = "OrgKortnavn_2";
	private static final String DOKUMENTTYPEID = "I000003";
	private static final String SPRAAK_NB = "nb";
	
	private PersonV3Consumer personV3Consumer = mock(PersonV3Consumer.class);
	private PostnummerService postnummerService = new PostnummerService();
	private LandkodeService landkodeService = new LandkodeService();
	private PersonV3Mapper personV3Mapper = new PersonV3Mapper(postnummerService, landkodeService);
	private OrganisasjonV4Consumer organisasjonV4Consumer = mock(OrganisasjonV4Consumer.class);
	private OrganisasjonV4Mapper organisasjonV4Mapper = new OrganisasjonV4Mapper(postnummerService, landkodeService);
	private Tkat020DokumenttypeInfo tkat020DokumenttypeInfo = mock(Tkat020DokumenttypeInfo.class);
	private Maalform malform = new Maalform();
	private Map<String, Object> valueMap;
	private SecurityContext securityContext = new SecurityContextImpl();
	private MottakerPlugin mottakerPlugin = new MottakerPlugin(personV3Consumer, personV3Mapper, organisasjonV4Consumer, organisasjonV4Mapper, tkat020DokumenttypeInfo);
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Before
	public void setUp() throws RegOppslagFunctionalException, RegOppslagTechnicalException, RegOppslagSecurityException {
		valueMap = new HashMap<>();
		valueMap.put(ValueMapKeys.DOKUMENTTYPEID.name(), DOKUMENTTYPEID);
		valueMap.put(ValueMapKeys.PREFIXMAPPER.name(), null);
		valueMap.put(ValueMapKeys.MAALFORM.name(), new Maalform());
		SecurityContextHolder.setContext(securityContext);
		
		when(personV3Consumer.hentPerson(any(String.class), any(String.class), any(String.class))).thenReturn(createPerson(FORNAVN, null, ETTERNAVN));
		when(organisasjonV4Consumer.hentOrganisasjon(any(String.class), any(String.class))).thenReturn(createOrganisasjon(Arrays
				.asList(ORGNAVN, ORGNAVN_2), Arrays.asList(ORGKORTNAVN, ORGKORTNAVN_2)));
		when(tkat020DokumenttypeInfo.hentDokumenttypeInfoSpraak(any(String.class))).thenReturn(createTkatResponse(Arrays.asList(SPRAAK_NB)));
	}

	@Test
	public void testMottakerPluginPerson() throws Exception {
		File xmlFile = new File(BREVDATA1);
		Document document = loadDocument(xmlFile);

		String expression1 = "//*[local-name() = 'mottaker']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);
		
		Node node = findSingleNode(xPathExpression, document);
		
		Node processed = mottakerPlugin.processElement(node, valueMap);
		
		JaxbHelper<Mottaker> mottakerJaxbHelper = new JaxbHelper<Mottaker>(Mottaker.class);
		Mottaker mottaker = mottakerJaxbHelper.unmarshal(processed);
		
		assertThat(mottaker.getNavn(), is(FORNAVN + " " + ETTERNAVN));
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

		JaxbHelper<Mottaker> mottakerJaxbHelper = new JaxbHelper<Mottaker>(Mottaker.class);
		Mottaker mottaker = mottakerJaxbHelper.unmarshal(processed);

		assertThat(mottaker.getNavn(), is(IKKE_BERIK_FORNAVN + " " + IKKE_BERIK_ETTERNAVN));
		assertThat(((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1(), is("ikkeberiket linje1"));
	}

	@Test
	public void testMottakerPluginOrganisasjon() throws Exception {
		File xmlFile = new File(BREVDATA_ORG);
		Document document = loadDocument(xmlFile);

		String expression1 = "/brevdata/*[local-name() = 'NAVFelles']//*[local-name() = 'mottaker']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);
		
		Node node = findSingleNode(xPathExpression, document);
		
		Node processed = mottakerPlugin.processElement(node, valueMap);
		JaxbHelper<Mottaker> mottakerJaxbHelper = new JaxbHelper<Mottaker>(Mottaker.class);
		Mottaker mottaker = mottakerJaxbHelper.unmarshal(processed);
		
		assertThat(mottaker.getNavn(), is(ORGNAVN + " " + ORGNAVN_2));
	}
	
	@Test
	public void shouldThrowExceptionWhenPersonIkkeFunnet() throws Exception {
		expectedException.expect(RegOppslagFunctionalException.class);
		expectedException.expectMessage("Feil i MottakerPlugin:  Kunne ikke finne person. ");
		when(personV3Consumer.hentPerson(any(String.class), any(String.class), any(String.class))).thenReturn(null);
		File xmlFile = new File(BREVDATA1);
		Document document = loadDocument(xmlFile);

		String expression1 = "/brevdata/*[local-name() = 'NAVFelles']//*[local-name() = 'mottaker']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);
		
		Node node = findSingleNode(xPathExpression, document);
		
		mottakerPlugin.processElement(node, valueMap);
	}
	
	@Test
	public void shouldThrowExceptionWhenOrganisasjonIkkeFunnet() throws Exception {
		expectedException.expect(RegOppslagFunctionalException.class);
		expectedException.expectMessage("Feil i MottakerPlugin:  Kunne ikke finne organisasjon. ");
		when(organisasjonV4Consumer.hentOrganisasjon(any(String.class), any(String.class))).thenReturn(null);
		File xmlFile = new File(BREVDATA_ORG);
		Document document = loadDocument(xmlFile);

		String expression1 = "/brevdata/*[local-name() = 'NAVFelles']//*[local-name() = 'mottaker']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);
		
		Node node = findSingleNode(xPathExpression, document);
		
		mottakerPlugin.processElement(node, valueMap);
	}
	
	@Test
	public void shouldThrowExceptionWhenMottakerManglerType() throws Exception {
		expectedException.expect(RegOppslagFunctionalException.class);
		expectedException.expectMessage("Feil i MottakerPlugin: Mottakerdata mangler AktoerType. AktoerType kan ikke være null.");
		when(organisasjonV4Consumer.hentOrganisasjon(any(String.class), any(String.class))).thenReturn(null);
		File xmlFile = new File(BREVDATA_TYPE);
		Document document = loadDocument(xmlFile);

		String expression1 = "/brevdata/*[local-name() = 'NAVFelles']//*[local-name() = 'mottaker']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);
		
		Node node = findSingleNode(xPathExpression, document);
		mottakerPlugin.processElement(node, valueMap);
	}
	
	@Test
	public void shouldThrowExceptionWhenMottakerManglerId() throws Exception {
		expectedException.expect(RegOppslagFunctionalException.class);
		expectedException.expectMessage("Feil i MottakerPlugin: Mottakerdata mangler mottakerId");
		when(organisasjonV4Consumer.hentOrganisasjon(any(String.class), any(String.class))).thenReturn(null);
		File xmlFile = new File(BREVDATA_ID);
		Document document = loadDocument(xmlFile);

		String expression1 = "/brevdata/*[local-name() = 'NAVFelles']//*[local-name() = 'mottaker']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(expression1);
		
		Node node = findSingleNode(xPathExpression, document);
		
		mottakerPlugin.processElement(node, valueMap);
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
		settPostadresse(person);
		return person;
	}
	private void settPostadresse(Bruker person) {
		Postadressetyper postadressetyper = new Postadressetyper();
		postadressetyper.setKodeverksRef("POSTADRESSE");
		postadressetyper.setValue("POSTADRESSE");
		person.setGjeldendePostadressetype(postadressetyper);

		UstrukturertAdresse ustrukturertAdresse = new UstrukturertAdresse();
		ustrukturertAdresse.setAdresselinje1("test");

		Postadresse postadresse = new Postadresse();
		postadresse.setUstrukturertAdresse(ustrukturertAdresse);

		person.setPostadresse(postadresse);
	}

	
	private Organisasjon createOrganisasjon(List<String> orgNavn, List<String> orgKortnavn) {
		Organisasjon organisasjon = new Organisasjon();
		OrganisasjonsDetaljer organisasjonsDetaljer = new OrganisasjonsDetaljer();
		UstrukturertNavn organisasjonKortnavn = new UstrukturertNavn();
		organisasjonKortnavn.getNavnelinje().addAll(orgKortnavn);
		organisasjon.setNavn(organisasjonKortnavn);
		
		UstrukturertNavn orgDetNavn = new UstrukturertNavn();
		orgDetNavn.getNavnelinje().addAll(orgNavn);
		Organisasjonsnavn organisasjonsnavn = new Organisasjonsnavn();
		organisasjonsnavn.setNavn(orgDetNavn);
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