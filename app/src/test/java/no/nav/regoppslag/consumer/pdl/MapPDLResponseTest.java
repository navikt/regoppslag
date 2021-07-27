package no.nav.regoppslag.consumer.pdl;

import no.nav.regoppslag.consumer.pdl.map.MapPDLResponse;
import no.nav.regoppslag.exceptions.UkjentAdresseException;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.regoppslag.service.PostnummerService;
import no.nav.regoppslag.util.PDLResponseUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static no.nav.regoppslag.metrics.MetricLabels.SERVICE_CODE_TREG002;
import static no.nav.regoppslag.util.PDLResponseUtil.ADRESSENAVN_1;
import static no.nav.regoppslag.util.PDLResponseUtil.COADRESSENAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.CO_ORGINASJON_NAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.CO_PERSON_NAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.DOEDSDATO;
import static no.nav.regoppslag.util.PDLResponseUtil.FOEDSELDATO;
import static no.nav.regoppslag.util.PDLResponseUtil.FORKOORETNAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.FULLT_NAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.LANDKODE_NORGE;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTNUMMER;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTSTED;
import static no.nav.regoppslag.util.PDLResponseUtil.UTENLANDSK_LANDKODE;
import static no.nav.regoppslag.util.PDLResponseUtil.UTENLANDSK_POSTKODE;
import static no.nav.regoppslag.util.PDLResponseUtil.createPdlHentPersonWithPersonDoedOgAdvokatSomKontakt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MapPDLResponseTest {

	private PdlGraphQLConsumer pdlGraphQLConsumer;
	private MapPDLResponse mapPDLResponse;
	private PostnummerService postnummerService;
	private MicrometerMetrics metrics;

	@BeforeEach
	public void setUp() {
		pdlGraphQLConsumer = mock(PdlGraphQLConsumer.class);
		postnummerService = new PostnummerService();
		mapPDLResponse = new MapPDLResponse(postnummerService, metrics);
	}

	@Test
	public void shouldMapMottkerInfoForDoedWithAdvokatSomKontkat() {
		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(createPdlHentPersonWithPersonDoedOgAdvokatSomKontakt());
		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(createPdlHentPersonWithPersonDoedOgAdvokatSomKontakt(), SERVICE_CODE_TREG002);

		assertEquals(FOEDSELDATO, mottakerInfo.getFoedselsdato());
		assertEquals(DOEDSDATO, mottakerInfo.getDoedsdato());
		assertEquals(FULLT_NAVN, mottakerInfo.getNavn());
		assertEquals(COADRESSENAVN, mottakerInfo.getPostadresse().getAdresselinje1());
		assertEquals(ADRESSENAVN_1, mottakerInfo.getPostadresse().getAdresselinje2());
		assertEquals(PDLConstant.POSTADRESSE_INNLAND, mottakerInfo.getPostadresse().getAdresseType());
		assertEquals(POSTNUMMER, mottakerInfo.getPostadresse().getPostnummer());
		assertEquals(POSTSTED, mottakerInfo.getPostadresse().getPoststed());
		assertEquals(LANDKODE_NORGE, mottakerInfo.getPostadresse().getLandkode());
	}

	@Test
	public void shouldMapMidlretidigKontaktWithVegadresse() {
		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(PDLResponseUtil.createPdlHentPersonWithVegadresse());
		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(PDLResponseUtil.createPdlHentPersonWithVegadresse(), SERVICE_CODE_TREG002);

		assertEquals(FOEDSELDATO, mottakerInfo.getFoedselsdato());
		assertNull(mottakerInfo.getDoedsdato());
		assertEquals(FULLT_NAVN, mottakerInfo.getNavn());
		assertEquals(ADRESSENAVN_1, mottakerInfo.getPostadresse().getAdresselinje1());
		assertNull(mottakerInfo.getPostadresse().getAdresselinje2());
		assertEquals(PDLConstant.POSTADRESSE_INNLAND, mottakerInfo.getPostadresse().getAdresseType());
		assertEquals(POSTNUMMER, mottakerInfo.getPostadresse().getPostnummer());
		assertEquals(POSTSTED, mottakerInfo.getPostadresse().getPoststed());
		assertEquals(LANDKODE_NORGE, mottakerInfo.getPostadresse().getLandkode());
	}

	@Test
	public void shouldMapMidlretidigKontaktWithOppholdsadresse() {
		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(PDLResponseUtil.createPdlHentPersonWithVegadresse());
		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(PDLResponseUtil.createPdlHentPersonWithOppholdsadresse(), SERVICE_CODE_TREG002);

		assertNull(mottakerInfo.getDoedsdato());
		assertEquals(FULLT_NAVN, mottakerInfo.getNavn());
		assertEquals(FORKOORETNAVN, mottakerInfo.getKortNavn());
		assertEquals(ADRESSENAVN_1, mottakerInfo.getPostadresse().getAdresselinje1());
		assertNull(mottakerInfo.getPostadresse().getAdresselinje2());
		assertEquals(PDLConstant.POSTADRESSE_INNLAND, mottakerInfo.getPostadresse().getAdresseType());
		assertEquals(POSTNUMMER, mottakerInfo.getPostadresse().getPostnummer());
		assertEquals(POSTSTED, mottakerInfo.getPostadresse().getPoststed());
		assertEquals(LANDKODE_NORGE, mottakerInfo.getPostadresse().getLandkode());
	}

	@Test
	public void shouldMapMidlretidigKontaktWithBostedsadresse() {
		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(PDLResponseUtil.createPdlHentPersonWithVegadresse());
		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(PDLResponseUtil.createPdlHentPersonWithBostedsadresse(), SERVICE_CODE_TREG002);

		assertNull(mottakerInfo.getDoedsdato());
		assertEquals(FULLT_NAVN, mottakerInfo.getNavn());
		assertEquals(FORKOORETNAVN, mottakerInfo.getKortNavn());
		assertEquals( ADRESSENAVN_1, mottakerInfo.getPostadresse().getAdresselinje1());
		assertNull(mottakerInfo.getPostadresse().getAdresselinje2());
		assertEquals(PDLConstant.POSTADRESSE_INNLAND, mottakerInfo.getPostadresse().getAdresseType());
		assertEquals(POSTNUMMER, mottakerInfo.getPostadresse().getPostnummer());
		assertEquals(POSTSTED, mottakerInfo.getPostadresse().getPoststed());
		assertEquals(LANDKODE_NORGE, mottakerInfo.getPostadresse().getLandkode());
	}

	@Test
	public void shouldThrowUkjentAdresseExceptionWhenNoAddess() {
		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(PDLResponseUtil.createPdlHentPersonWithVegadresse());

		assertThrows(UkjentAdresseException.class,
				() -> mapPDLResponse.mapHentPerson(PDLResponseUtil.createPdlHentPersonWithNoAdresse(), SERVICE_CODE_TREG002), "Fant ikke adresse for personen i PDL");
	}







	@Test
	public void ShouldMapAndValidateKontaktadresseForInnlandAddresseWithVegadresse() {
		Vegadresse adresse = PDLResponseUtil.createVegadresse();
		Kontaktadresse kontaktadresse = Kontaktadresse.builder()
				.vegadresse(adresse)
				.type(PDLConstant.POSTADRESSE_INNLAND)
				.build();

		PostadresseTo response = mapPDLResponse.mapKontaktadresse(kontaktadresse, null);

		assertEquals(ADRESSENAVN_1, response.getAdresselinje1());
		assertEquals(null, response.getAdresselinje2());
		assertEquals(null, response.getAdresselinje3());

		assertEquals(PDLConstant.POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		assertEquals(adresse.getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
	}


	@Test
	public void ShouldMapAndValidateKontaktadresseForInnlandAddresseWithPostadresseIFrittFormatAndCoAdressenavn() {
		Kontaktadresse.PostadresseIFrittFormat adresse = PDLResponseUtil.createPostadresseIFrittFormat();
		Kontaktadresse kontaktadresse = Kontaktadresse.builder()
				.postadresseIFrittFormat(adresse)
				.type(PDLConstant.POSTADRESSE_INNLAND)
				.coAdressenavn(COADRESSENAVN)
				.build();

		PostadresseTo response = mapPDLResponse.mapKontaktadresse(kontaktadresse, null);

		assertEquals(COADRESSENAVN, response.getAdresselinje1());
		assertEquals(adresse.getAdresselinje1(), response.getAdresselinje2());
		assertEquals(adresse.getAdresselinje2(), response.getAdresselinje3());

		assertEquals(PDLConstant.POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		assertEquals(adresse.getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
	}

	@Test
	public void ShouldMapAndValidateKontaktadresseForInnlandAddresseWithPostadresseIFrittFormat() {
		Kontaktadresse.PostadresseIFrittFormat adresse = PDLResponseUtil.createPostadresseIFrittFormat();
		Kontaktadresse kontaktadresse = Kontaktadresse.builder()
				.postadresseIFrittFormat(adresse)
				.type(PDLConstant.POSTADRESSE_INNLAND)
				.build();

		PostadresseTo response = mapPDLResponse.mapKontaktadresse(kontaktadresse, null);

		assertEquals(adresse.getAdresselinje1(), response.getAdresselinje1());
		assertEquals(adresse.getAdresselinje2(), response.getAdresselinje2());
		assertEquals(adresse.getAdresselinje3(), response.getAdresselinje3());

		assertEquals(PDLConstant.POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		assertEquals(adresse.getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
	}

	@Test
	public void ShouldMapAndValidateKontaktadresseForInnlandAddresseWithPostboksAdresse() {
		Kontaktadresse.Postboksadresse adresse = PDLResponseUtil.createPostboksadresse();
		Kontaktadresse kontaktadresse = Kontaktadresse.builder()
				.postboksadresse(adresse)
				.type(PDLConstant.POSTADRESSE_INNLAND)
				.build();

		PostadresseTo response = mapPDLResponse.mapKontaktadresse(kontaktadresse, null);

		assertEquals("Postboks " + adresse.getPostboks(), response.getAdresselinje1());
		assertEquals(null, response.getAdresselinje2());
		assertEquals(null, response.getAdresselinje3());

		assertEquals(PDLConstant.POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(null, response.getLandkode());
		assertEquals(adresse.getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
	}

	@Test
	public void ShouldMapAndValidateKontaktadresseForUtlandWithUtlandsAddresse() {
		UtenlandskAdresse adresse = PDLResponseUtil.createUtenlandskAdresse();
		Kontaktadresse kontaktadresse = Kontaktadresse.builder()
				.UtenlandskAdresse(adresse)
				.type(PDLConstant.POSTADRESSE_UTLAND)
				.build();

		PostadresseTo response = mapPDLResponse.mapKontaktadresse(kontaktadresse, null);

		assertEquals(adresse.getPostboksNummerNavn(), response.getAdresselinje1());
		assertEquals(adresse.getPostkode(), response.getAdresselinje2());
		assertEquals(adresse.getBySted(), response.getAdresselinje3());

		assertEquals(PDLConstant.POSTADRESSE_UTLAND, response.getAdresseType());
		assertEquals(UTENLANDSK_LANDKODE, response.getLandkode());
		assertEquals(null, response.getPostnummer());
		assertEquals(null, response.getPoststed());
	}

	@Test
	public void ShouldMapAndValidateKontaktadresseForUtlandWithUtenlandskAdresseIFrittFormat() {
		Kontaktadresse.UtenlandskAdresseIFrittFormat adresse = PDLResponseUtil.createUtenlandskAdresseIFrittFormat();
		Kontaktadresse kontaktadresse = Kontaktadresse.builder()
				.utenlandskAdresseIFrittFormat(adresse)
				.type(PDLConstant.POSTADRESSE_UTLAND)
				.build();

		PostadresseTo response = mapPDLResponse.mapKontaktadresse(kontaktadresse, null);

		assertEquals(adresse.getAdresselinje1(), response.getAdresselinje1());
		assertEquals(UTENLANDSK_POSTKODE, response.getAdresselinje2());
		assertEquals(POSTSTED, response.getAdresselinje3());

		assertEquals(PDLConstant.POSTADRESSE_UTLAND, response.getAdresseType());
		assertEquals(UTENLANDSK_LANDKODE, response.getLandkode());
		assertEquals(null, response.getPostnummer());
		assertEquals(null, response.getPoststed());
	}

	@Test
	public void shlouldMapAndValidateKontaktinformasjonForDoedsWhenRecievingKontinformasjonWithAdvokatAsContact() {
		KontaktinformasjonForDoedsbo kontaktinformasjon = PDLResponseUtil.createKontaktinformasjonForDoedsbo();
		PostadresseTo response = mapPDLResponse.mapAndValidateKontaktinformasjonForDoeds(kontaktinformasjon);

		assertEquals(COADRESSENAVN, response.getAdresselinje1());
		assertEquals(kontaktinformasjon.getAdresse().getAdresselinje1(), response.getAdresselinje2());
		assertEquals(null, response.getAdresselinje3());

		assertEquals(PDLConstant.POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		assertEquals(kontaktinformasjon.getAdresse().getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
	}

	@Test
	public void shouldMapAndValidateKontaktinformasjonForDoedsWhenRecievingKontinformasjonWithPersonAsContact() {
		KontaktinformasjonForDoedsbo kontaktinformasjon = PDLResponseUtil.createKontaktinformasjonForDoedsboWithPerson();
		PostadresseTo response = mapPDLResponse.mapAndValidateKontaktinformasjonForDoeds(kontaktinformasjon);

		assertEquals(CO_PERSON_NAVN, response.getAdresselinje1());
		assertEquals(kontaktinformasjon.getAdresse().getAdresselinje1(), response.getAdresselinje2());
		assertEquals(null, response.getAdresselinje3());

		assertEquals(PDLConstant.POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		assertEquals(kontaktinformasjon.getAdresse().getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
	}

	@Test
	public void shouldMapAndValidateKontaktinformasjonForDoedsWhenRecievingKontinformasjonWithOrginasjonAsContact() {
		KontaktinformasjonForDoedsbo kontaktinformasjon = PDLResponseUtil.createKontaktinformasjonForDoedsboWithOrginasjon();
		PostadresseTo response = mapPDLResponse.mapAndValidateKontaktinformasjonForDoeds(kontaktinformasjon);

		assertEquals(CO_ORGINASJON_NAVN, response.getAdresselinje1());
		assertEquals(kontaktinformasjon.getAdresse().getAdresselinje1(), response.getAdresselinje2());
		assertEquals(null, response.getAdresselinje3());

		assertEquals(PDLConstant.POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(null, response.getLandkode());
		assertEquals(kontaktinformasjon.getAdresse().getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
	}

	@Test
	public void shouldMapAndValidateKontaktinformasjonForDoedsWhenRecievingKontinformasjonWithNOContact() {
		KontaktinformasjonForDoedsbo kontaktinformasjon = PDLResponseUtil.createKontaktinformasjonForDoedsboWithNoContact();
		PostadresseTo response = mapPDLResponse.mapAndValidateKontaktinformasjonForDoeds(kontaktinformasjon);

		assertNull(response);
	}

	@Test
	public void ShouldmapVegadresseWhenCoAdressenavnIsSett () {
		Vegadresse vegadresse = PDLResponseUtil.createVegadresse();
		PostadresseTo response = mapPDLResponse.mapVegadresse(vegadresse, "coAddressenavn").build();

		assertEquals("coAddressenavn", response.getAdresselinje1());
		assertEquals(vegadresse.getAdressenavn() + " " + vegadresse.getHusnummer(), response.getAdresselinje2());
		assertEquals(null, response.getAdresselinje3());

		assertEquals(PDLConstant.POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		assertEquals(vegadresse.getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
	}

	@Test
	public void ShouldmapVegadresseWhenCoAdressenavnAsNull () {
		Vegadresse vegadresse = PDLResponseUtil.createVegadresse();
		PostadresseTo response = mapPDLResponse.mapVegadresse(vegadresse, null).build();


		assertEquals( vegadresse.getAdressenavn() + " " + vegadresse.getHusnummer(), response.getAdresselinje1());
		assertEquals( null, response.getAdresselinje2());
		assertEquals(null, response.getAdresselinje3());

		assertEquals(PDLConstant.POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		assertEquals(vegadresse.getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
	}
}
