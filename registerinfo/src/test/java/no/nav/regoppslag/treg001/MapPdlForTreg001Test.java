package no.nav.regoppslag.treg001;


import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.dok.brevdata.felles.v1.navfelles.Person;
import no.nav.dok.brevdata.felles.v1.navfelles.UtenlandskPostadresse;
import no.nav.dok.brevdata.felles.v1.simpletypes.AktoerType;
import no.nav.dok.brevdata.felles.v1.simpletypes.Spraakkode;
import no.nav.dokkat.api.tkat020.v3.SpraakInfoTo;
import no.nav.dokkat.api.tkat020.v4.SpraakInfoToV4;
import no.nav.regoppslag.consumer.digdirkrr.DigitalKontaktinformasjon;
import no.nav.regoppslag.consumer.dokkat.Tkat020DokumenttypeInfo;
import no.nav.regoppslag.consumer.ereg.EregConsumer;
import no.nav.regoppslag.consumer.ereg.support.OrganisasjonEregMapper;
import no.nav.regoppslag.consumer.pdl.PdlGraphQLConsumer;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.regoppslag.pdl.MapPDLResponse;
import no.nav.regoppslag.service.LandkodeService;
import no.nav.regoppslag.service.PostnummerService;
import no.nav.regoppslag.treg001.util.CreateStubs;
import no.nav.regoppslag.util.TestDataUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.xml.datatype.DatatypeConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static no.nav.regoppslag.util.PDLResponseUtil.ADRESSENAVN_1;
import static no.nav.regoppslag.util.PDLResponseUtil.FULLT_NAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.KORT_NAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.ORGANISASJONNUMMER;
import static no.nav.regoppslag.util.PDLResponseUtil.PERSON_IDENT;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTBOKSNUMMERNAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTKODE_AND_BYSTED;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTNUMMER;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTSTED;
import static no.nav.regoppslag.util.PDLResponseUtil.STATE;
import static no.nav.regoppslag.util.PDLResponseUtil.SWEDEN_UTENLANDSK;
import static no.nav.regoppslag.util.PDLResponseUtil.createPdlHentPersonUtenlandskAdresse;
import static no.nav.regoppslag.util.PDLResponseUtil.createPdlHentPersonWithBostedsadresse;
import static no.nav.regoppslag.util.TestDataUtil.GATENAVN;
import static no.nav.regoppslag.util.TestDataUtil.HUSBOKSTAV;
import static no.nav.regoppslag.util.TestDataUtil.HUSNR;
import static no.nav.regoppslag.util.TestDataUtil.POSTNR;
import static no.nav.regoppslag.util.TestDataUtil.settPostAdresse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MapPdlForTreg001Test {

	private static final String TEMA = "PEN";
	private static final String DOKUMENTTYPEID = "I000003";
	private static final String SPRAAK_NB = "NB";
	private static final String ORGNAVN = "Firma AS";

	private PdlGraphQLConsumer pdlGraphQLConsumer;
	private EregConsumer eregConsumer;
	private OrganisasjonEregMapper organisasjonEregMapper;
	private LandkodeService landkodeService;
	private DigitalKontaktinformasjon digitalKontaktinformasjon;
	private Tkat020DokumenttypeInfo tkat020DokumenttypeInfo;
	@InjectMocks
	private MapPDLResponse mapPDLResponse;
	private PostnummerService postnummerService;
	@InjectMocks
	private MapPdlForTreg001 pdlForTreg001;


	@BeforeEach
	public void setUp() throws IOException {
		landkodeService = new LandkodeService();
		postnummerService = new PostnummerService();
		pdlGraphQLConsumer = mock(PdlGraphQLConsumer.class);
		mapPDLResponse = new MapPDLResponse(postnummerService, landkodeService, pdlGraphQLConsumer);
		digitalKontaktinformasjon = mock(DigitalKontaktinformasjon.class);
		tkat020DokumenttypeInfo = mock(Tkat020DokumenttypeInfo.class);
		eregConsumer = mock(EregConsumer.class);
		organisasjonEregMapper = new OrganisasjonEregMapper(new PostnummerService(), new LandkodeService(), mock(MicrometerMetrics.class));
		pdlForTreg001 = new MapPdlForTreg001(pdlGraphQLConsumer, mapPDLResponse, landkodeService, tkat020DokumenttypeInfo, digitalKontaktinformasjon, eregConsumer, organisasjonEregMapper);

	}

	@Test
	public void shouldMapTreg001MottakerAdresseFraPdl() {
		when(pdlGraphQLConsumer.hentPerson(PERSON_IDENT, TEMA)).thenReturn(createPdlHentPersonWithBostedsadresse());
		when(digitalKontaktinformasjon.hentSpraak(anyString(), anyBoolean())).thenReturn("NB");
		when(tkat020DokumenttypeInfo.hentDokumenttypeInfoSpraak(anyString())).thenReturn(CreateStubs.createTkatResponse(Arrays.asList(SPRAAK_NB, "EN", "NN")));
		Mottaker mottaker = pdlForTreg001.getMottakerFraPdl(TEMA, createPersonMottaker(), DOKUMENTTYPEID);
		NorskPostadresse adresse = (NorskPostadresse) mottaker.getMottakeradresse();

		assertEquals(PERSON_IDENT, mottaker.getId());
		assertEquals(KORT_NAVN, mottaker.getKortNavn());
		assertEquals(FULLT_NAVN, mottaker.getNavn());
		assertEquals(ADRESSENAVN_1, adresse.getAdresselinje1());
		assertEquals(POSTSTED, adresse.getPoststed());
		assertEquals(POSTNUMMER, adresse.getPostnummer());
		assertEquals(Spraakkode.NB, mottaker.getSpraakkode());
	}

	@Test
	public void shouldMapUtenlandskAdresseFraPdl() {
		when(pdlGraphQLConsumer.hentPerson(PERSON_IDENT, TEMA)).thenReturn(createPdlHentPersonUtenlandskAdresse());
		when(digitalKontaktinformasjon.hentSpraak(anyString(), anyBoolean())).thenReturn("EN");
		when(tkat020DokumenttypeInfo.hentDokumenttypeInfoSpraak(anyString())).thenReturn(CreateStubs.createTkatResponse(Arrays.asList(SPRAAK_NB, "EN", "NN")));
		Mottaker mottaker = pdlForTreg001.getMottakerFraPdl(TEMA, createPersonMottaker(), DOKUMENTTYPEID);
		UtenlandskPostadresse adresse = (UtenlandskPostadresse) mottaker.getMottakeradresse();

		assertEquals(PERSON_IDENT, mottaker.getId());
		assertEquals(KORT_NAVN, mottaker.getKortNavn());
		assertEquals(FULLT_NAVN, mottaker.getNavn());
		assertEquals(POSTBOKSNUMMERNAVN, adresse.getAdresselinje1());
		assertEquals(POSTKODE_AND_BYSTED + ", " + STATE, adresse.getAdresselinje2());
		assertEquals(SWEDEN_UTENLANDSK, adresse.getLand());
		assertEquals(Spraakkode.EN, mottaker.getSpraakkode());
	}

	@Test
	void shouldMapNorskOrganisasjon() throws DatatypeConfigurationException {
		when(eregConsumer.hentOrganisasjon(anyString())).thenReturn(createOrganisasjon());
		when(tkat020DokumenttypeInfo.hentDokumenttypeInfoSpraak(anyString())).thenReturn(CreateStubs.createTkatResponse(Collections.singletonList(SPRAAK_NB)));
		Mottaker mottaker = pdlForTreg001.getMottakerFraPdl(TEMA, createOrganisasjonMottaker(), DOKUMENTTYPEID);
		NorskPostadresse adresse = (NorskPostadresse) mottaker.getMottakeradresse();

		assertEquals(ORGANISASJONNUMMER, mottaker.getId());
		assertEquals(ORGNAVN, mottaker.getKortNavn());
		assertEquals(ORGNAVN, mottaker.getNavn());
		assertEquals(GATENAVN + " " + HUSNR + HUSBOKSTAV, adresse.getAdresselinje1());
		assertEquals("HUSNES", adresse.getPoststed());
		assertEquals(POSTNR, adresse.getPostnummer());
		assertEquals(Spraakkode.NB, mottaker.getSpraakkode());
	}


	private Mottaker createPersonMottaker() {
		Mottaker mottaker = new Person();
		mottaker.setId(PERSON_IDENT);
		mottaker.setBerik(true);
		mottaker.setTypeKode(AktoerType.PERSON);
		return mottaker;
	}

	private Mottaker createOrganisasjonMottaker() {
		Mottaker mottaker = new Person();
		mottaker.setId(ORGANISASJONNUMMER);
		mottaker.setBerik(true);
		mottaker.setTypeKode(AktoerType.ORGANISASJON);
		return mottaker;
	}

	private static no.nav.regoppslag.consumer.ereg.support.Organisasjon createOrganisasjon() throws DatatypeConfigurationException {
		no.nav.regoppslag.consumer.ereg.support.Organisasjon org = TestDataUtil.createOrganisasjon(ORGNAVN);
		settPostAdresse(org, "POSTADRESSE", 10000L);
		return org;
	}
}