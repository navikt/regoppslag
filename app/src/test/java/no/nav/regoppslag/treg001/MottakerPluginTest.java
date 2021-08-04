package no.nav.regoppslag.treg001;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.dok.brevdata.felles.v1.simpletypes.Spraakkode;
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
import no.nav.regoppslag.util.TestDataUtil;
import no.nav.regoppslag.xmlenricher.util.JaxbHelper;
import no.nav.regoppslag.xmlenricher.util.ValueMapKeys;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personnavn;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Postadresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Postadressetyper;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Spraak;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.UstrukturertAdresse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static no.nav.dok.brevdata.felles.v1.simpletypes.AktoerType.ORGANISASJON;
import static no.nav.dok.brevdata.felles.v1.simpletypes.AktoerType.PERSON;
import static no.nav.regoppslag.util.TestDataUtil.settStrukturertAdresse;
import static no.nav.regoppslag.util.TestUtil.findSingleNode;
import static no.nav.regoppslag.util.TestUtil.loadDocument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
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
    private static final String ORGNAVN_2 = "Orgnavn_2";
    private static final String ORGKORTNAVN = "OrgKortnavn 1";
    private static final String ORGKORTNAVN_2 = "OrgKortnavn_2";
    private static final String DOKUMENTTYPEID = "I000003";
    private static final String SPRAAK_NB = "NB";
    private static final String MOTTAKER_ID = "30085849677";

    private static PersonV3Consumer personV3Consumer = mock(PersonV3Consumer.class);
    private static PostnummerService postnummerService = new PostnummerService();
    private static LandkodeService landkodeService = new LandkodeService();
    private static OrganisasjonV4Consumer organisasjonV4Consumer = mock(OrganisasjonV4Consumer.class);
    private static OrganisasjonV4Mapper organisasjonV4Mapper;
    private static Tkat020DokumenttypeInfo tkat020DokumenttypeInfo = mock(Tkat020DokumenttypeInfo.class);
    private static Map<String, Object> valueMap;
    private static SecurityContext securityContext = new SecurityContextImpl();
    private static PersonV3Mapper personV3Mapper;
    private static MottakerPlugin mottakerPlugin;

    @BeforeAll
    public static void setUp() throws RegOppslagSecurityException, DatatypeConfigurationException {
        valueMap = new HashMap<>();
        valueMap.put(ValueMapKeys.DOKUMENTTYPEID.name(), DOKUMENTTYPEID);
        valueMap.put(ValueMapKeys.PREFIXMAPPER.name(), null);
        valueMap.put(ValueMapKeys.MAALFORM.name(), new SpraakKodeMapper());
        SecurityContextHolder.setContext(securityContext);

        MicrometerMetrics metrics = new MicrometerMetrics();
        MeterRegistry registry = new SimpleMeterRegistry();
        ReflectionTestUtils.setField(metrics, "registry", registry);
        personV3Mapper = new PersonV3Mapper(postnummerService, landkodeService, metrics);
        organisasjonV4Mapper = new OrganisasjonV4Mapper(postnummerService, landkodeService, metrics);
        mottakerPlugin = new MottakerPlugin(personV3Consumer, personV3Mapper, organisasjonV4Consumer, organisasjonV4Mapper, tkat020DokumenttypeInfo, metrics);

        when(personV3Consumer.hentPerson(anyString(), anyString())).thenReturn(createPerson(FORNAVN, null, ETTERNAVN));
        when(organisasjonV4Consumer.hentOrganisasjon(anyString())).thenReturn(createOrganisasjon());
        when(tkat020DokumenttypeInfo.hentDokumenttypeInfoSpraak(anyString())).thenReturn(createTkatResponse(Collections.singletonList(SPRAAK_NB)));

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

        JaxbHelper<Mottaker> mottakerJaxbHelper = new JaxbHelper<>(Mottaker.class);
        Mottaker mottaker = mottakerJaxbHelper.unmarshal(processed);

        assertThat(mottaker.getNavn(), is(FORNAVN + " " + ETTERNAVN));
    }

    @Test
    public void shouldUsePersonMaalform() throws Exception {
        Bruker person = createPerson(FORNAVN, null, ETTERNAVN);

        Spraak spraak = new Spraak();
        spraak.setValue("EN");
        person.setMaalform(spraak);
        when(personV3Consumer.hentPerson(anyString(), anyString())).thenReturn(person);
        when(tkat020DokumenttypeInfo.hentDokumenttypeInfoSpraak(anyString())).thenReturn(createTkatResponse(Arrays.asList(SPRAAK_NB, "EN", "NN")));

        File xmlFile = new File(BREVDATA1);
        Document document = loadDocument(xmlFile);

        String expression1 = "//*[local-name() = 'mottaker']";
        XPath xPath = XPathFactory.newInstance().newXPath();
        XPathExpression xPathExpression = xPath.compile(expression1);

        Node node = findSingleNode(xPathExpression, document);

        Node processed = mottakerPlugin.processElement(node, valueMap);

        JaxbHelper<Mottaker> mottakerJaxbHelper = new JaxbHelper<>(Mottaker.class);
        Mottaker mottaker = mottakerJaxbHelper.unmarshal(processed);
        assertThat(mottaker.getSpraakkode().value(), is("EN"));
    }

    @Test
    public void shouldUseMottakerMaalform() throws Exception {
        Bruker person = createPerson(FORNAVN, null, ETTERNAVN);

        when(personV3Consumer.hentPerson(anyString(), anyString())).thenReturn(person);
        when(tkat020DokumenttypeInfo.hentDokumenttypeInfoSpraak(anyString())).thenReturn(createTkatResponse(Arrays.asList(SPRAAK_NB, "EN")));

        File xmlFile = new File(BREVDATA_MOTTAKER_SPRAAKKODE_EN);
        Document document = loadDocument(xmlFile);

        String expression1 = "//*[local-name() = 'mottaker']";
        XPath xPath = XPathFactory.newInstance().newXPath();
        XPathExpression xPathExpression = xPath.compile(expression1);

        Node node = findSingleNode(xPathExpression, document);

        Node processed = mottakerPlugin.processElement(node, valueMap);

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

        Node processed = mottakerPlugin.processElement(node, valueMap);

        JaxbHelper<Mottaker> mottakerJaxbHelper = new JaxbHelper<>(Mottaker.class);
        Mottaker mottaker = mottakerJaxbHelper.unmarshal(processed);

        assertThat(mottaker.getNavn(), is(IKKE_BERIK_FORNAVN + " " + IKKE_BERIK_ETTERNAVN));
        assertThat(mottaker.getId(), is(MOTTAKER_ID));
        assertThat(mottaker.getTypeKode(), is(PERSON));
        assertThat(((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1(), is("ikkeberiket linje1"));
    }

    @Test
    public void testMottakerPluginOrganisasjon() throws Exception {
        when(tkat020DokumenttypeInfo.hentDokumenttypeInfoSpraak(anyString())).thenReturn(createTkatResponse(Collections.singletonList("NN")));

        File xmlFile = new File(BREVDATA_ORG);
        Document document = loadDocument(xmlFile);

        String expression1 = "/brevdata/*[local-name() = 'NAVFelles']//*[local-name() = 'mottaker']";
        XPath xPath = XPathFactory.newInstance().newXPath();
        XPathExpression xPathExpression = xPath.compile(expression1);

        Node node = findSingleNode(xPathExpression, document);

        Node processed = mottakerPlugin.processElement(node, valueMap);
        JaxbHelper<Mottaker> mottakerJaxbHelper = new JaxbHelper<>(Mottaker.class);
        Mottaker mottaker = mottakerJaxbHelper.unmarshal(processed);

        assertThat(mottaker.getNavn(), is(ORGNAVN + " " + ORGNAVN_2));
        assertThat(mottaker.getTypeKode(), is(ORGANISASJON));
        assertThat(mottaker.getId(), is("974727854"));
        assertThat(mottaker.getKortNavn(), is("OrgKortnavn 1 OrgKortnavn_2"));
        assertThat(mottaker.getSpraakkode(), is(Spraakkode.NN));
    }


    @Test
    public void shouldThrowExceptionWhenMottakerManglerType() throws Exception {
        when(organisasjonV4Consumer.hentOrganisasjon(anyString())).thenReturn(null);
        File xmlFile = new File(BREVDATA_TYPE);
        Document document = loadDocument(xmlFile);

        String expression1 = "/brevdata/*[local-name() = 'NAVFelles']//*[local-name() = 'mottaker']";
        XPath xPath = XPathFactory.newInstance().newXPath();
        XPathExpression xPathExpression = xPath.compile(expression1);

        Node node = findSingleNode(xPathExpression, document);
        RegOppslagFunctionalException exception = assertThrows(RegOppslagFunctionalException.class,
                () -> mottakerPlugin.processElement(node, valueMap),
                "Feil i MottakerPlugin: Mottakerdata mangler AktoerType. AktoerType kan ikke være null.");
        assertEquals(exception.getMessage(), "Feil i MottakerPlugin: Mottakerdata mangler AktoerType. AktoerType kan ikke være null.");

    }

    @Test
    public void shouldThrowExceptionWhenMottakerManglerId() throws Exception {
        when(organisasjonV4Consumer.hentOrganisasjon(anyString())).thenReturn(null);
        File xmlFile = new File(BREVDATA_ID);
        Document document = loadDocument(xmlFile);

        String expression1 = "/brevdata/*[local-name() = 'NAVFelles']//*[local-name() = 'mottaker']";
        XPath xPath = XPathFactory.newInstance().newXPath();
        XPathExpression xPathExpression = xPath.compile(expression1);

        Node node = findSingleNode(xPathExpression, document);
        assertThrows(RegOppslagFunctionalException.class,
                () -> mottakerPlugin.processElement(node, valueMap), "Feil i MottakerPlugin: Mottakerdata mangler mottakerId");

    }

    private static Bruker createPerson(String fornavn, String mellomnavn, String etternavn) {
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

    private static void settPostadresse(Bruker person) {
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

    private static List<SpraakInfoTo> createTkatResponse(List<String> langs) {
        List<SpraakInfoTo> list = new ArrayList<>();
        langs.forEach(lang -> {
            SpraakInfoTo spraakInfoTo = new SpraakInfoTo();
            spraakInfoTo.setSpraaklag(lang);
            list.add(spraakInfoTo);
        });
        return list;
    }

    private static Organisasjon createOrganisasjon() throws DatatypeConfigurationException {
        Organisasjon org = TestDataUtil.createOrganisasjon(Arrays
                .asList(ORGNAVN, ORGNAVN_2), Arrays.asList(ORGKORTNAVN, ORGKORTNAVN_2));
        settStrukturertAdresse(org, "POSTADRESSE");
        return org;
    }
}