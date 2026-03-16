package no.nav.regoppslag.treg001;

import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.dok.brevdata.felles.v1.navfelles.Person;
import no.nav.dok.brevdata.felles.v1.navfelles.UtenlandskPostadresse;
import no.nav.dok.brevdata.felles.v1.simpletypes.Spraakkode;
import no.nav.regoppslag.consumer.digdirkrr.DigitalKontaktinformasjon;
import no.nav.regoppslag.consumer.dokmet.DokmetConsumer;
import no.nav.regoppslag.consumer.ereg.EregConsumer;
import no.nav.regoppslag.consumer.pdl.PdlGraphQLConsumer;
import no.nav.regoppslag.pdl.DoedsboAdresseService;
import no.nav.regoppslag.pdl.MapPDLResponse;
import no.nav.regoppslag.treg001.util.CreateStubs;
import no.nav.regoppslag.util.TestDataUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;

import static no.nav.dok.brevdata.felles.v1.simpletypes.AktoerType.ORGANISASJON;
import static no.nav.dok.brevdata.felles.v1.simpletypes.AktoerType.PERSON;
import static no.nav.dok.brevdata.felles.v1.simpletypes.Spraakkode.NB;
import static no.nav.regoppslag.config.TimeConfig.OSLO_ZONE;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MapPdlForTreg001Test {

	private static final String DOKUMENTTYPEID = "I000003";
	private static final String SPRAAK_NB = "NB";
	private static final String ORGNAVN = "Firma AS";

	private PdlGraphQLConsumer pdlGraphQLConsumer;
	private EregConsumer eregConsumer;
	private DigitalKontaktinformasjon digitalKontaktinformasjon;
	private DokmetConsumer dokmetConsumer;

	@InjectMocks
	private MapPDLResponse mapPDLResponse;
	@InjectMocks
	private MapPdlForTreg001 pdlForTreg001;

	@BeforeEach
	public void setUp() throws IOException {
		pdlGraphQLConsumer = mock(PdlGraphQLConsumer.class);
		mapPDLResponse = new MapPDLResponse(new DoedsboAdresseService(pdlGraphQLConsumer), Clock.system(OSLO_ZONE));
		digitalKontaktinformasjon = mock(DigitalKontaktinformasjon.class);
		dokmetConsumer = mock(DokmetConsumer.class);
		eregConsumer = mock(EregConsumer.class);

		pdlForTreg001 = new MapPdlForTreg001(pdlGraphQLConsumer, mapPDLResponse, dokmetConsumer, digitalKontaktinformasjon, eregConsumer);
	}

	@Test
	public void shouldMapTreg001MottakerAdresseFraPdl() {
		when(pdlGraphQLConsumer.hentPerson(PERSON_IDENT)).thenReturn(createPdlHentPersonWithBostedsadresse());
		when(digitalKontaktinformasjon.hentSpraak(anyString())).thenReturn("NB");
		when(dokmetConsumer.hentDokumenttypeInfoSpraak(anyString())).thenReturn(CreateStubs.createTkatResponse(Arrays.asList(SPRAAK_NB, "EN", "NN")));
		Mottaker mottaker = pdlForTreg001.getMottakerFraPdl(createPersonMottaker(), DOKUMENTTYPEID);
		NorskPostadresse adresse = (NorskPostadresse) mottaker.getMottakeradresse();

		assertEquals(PERSON_IDENT, mottaker.getId());
		assertEquals(KORT_NAVN, mottaker.getKortNavn());
		assertEquals(FULLT_NAVN, mottaker.getNavn());
		assertEquals(ADRESSENAVN_1, adresse.getAdresselinje1());
		assertEquals(POSTSTED, adresse.getPoststed());
		assertEquals(POSTNUMMER, adresse.getPostnummer());
		assertEquals(NB, mottaker.getSpraakkode());
	}

	@Test
	public void shouldMapUtenlandskAdresseFraPdl() {
		when(pdlGraphQLConsumer.hentPerson(PERSON_IDENT)).thenReturn(createPdlHentPersonUtenlandskAdresse());
		when(digitalKontaktinformasjon.hentSpraak(anyString())).thenReturn("EN");
		when(dokmetConsumer.hentDokumenttypeInfoSpraak(anyString())).thenReturn(CreateStubs.createTkatResponse(Arrays.asList(SPRAAK_NB, "EN", "NN")));
		Mottaker mottaker = pdlForTreg001.getMottakerFraPdl(createPersonMottaker(), DOKUMENTTYPEID);
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
	void shouldMapNorskOrganisasjon() {
		when(eregConsumer.hentOrganisasjon(anyString())).thenReturn(createOrganisasjon());
		when(dokmetConsumer.hentDokumenttypeInfoSpraak(anyString())).thenReturn(CreateStubs.createTkatResponse(Collections.singletonList(SPRAAK_NB)));
		Mottaker mottaker = pdlForTreg001.getMottakerFraPdl(createOrganisasjonMottaker(), DOKUMENTTYPEID);
		NorskPostadresse adresse = (NorskPostadresse) mottaker.getMottakeradresse();

		assertEquals(ORGANISASJONNUMMER, mottaker.getId());
		assertEquals(ORGNAVN, mottaker.getKortNavn());
		assertEquals(ORGNAVN, mottaker.getNavn());
		assertEquals(GATENAVN + " " + HUSNR + HUSBOKSTAV, adresse.getAdresselinje1());
		assertEquals("HUSNES", adresse.getPoststed());
		assertEquals(POSTNR, adresse.getPostnummer());
		assertEquals(NB, mottaker.getSpraakkode());
	}

	private Mottaker createPersonMottaker() {
		Mottaker mottaker = new Person();
		mottaker.setId(PERSON_IDENT);
		mottaker.setBerik(true);
		mottaker.setTypeKode(PERSON);
		return mottaker;
	}

	private Mottaker createOrganisasjonMottaker() {
		Mottaker mottaker = new Person();
		mottaker.setId(ORGANISASJONNUMMER);
		mottaker.setBerik(true);
		mottaker.setTypeKode(ORGANISASJON);
		return mottaker;
	}

	private static no.nav.regoppslag.consumer.ereg.support.Organisasjon createOrganisasjon() {
		no.nav.regoppslag.consumer.ereg.support.Organisasjon org = TestDataUtil.createOrganisasjon(ORGNAVN);
		settPostAdresse(org, "POSTADRESSE", 10000L);
		return org;
	}
}