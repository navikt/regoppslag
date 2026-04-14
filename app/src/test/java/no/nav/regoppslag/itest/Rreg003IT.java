package no.nav.regoppslag.itest;

import no.nav.regoppslag.rreg003.Adresse;
import no.nav.regoppslag.rreg003.PostadresseRequest;
import no.nav.regoppslag.rreg003.PostadresseResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpClientErrorException.NotFound;
import org.springframework.web.client.HttpServerErrorException;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static java.util.Set.of;
import static no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode.BOSTEDSADRESSE;
import static no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode.KONTAKTADRESSE;
import static no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode.KONTAKTINFORMASJONFORDØDSBO;
import static no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode.OPPHOLDSADRESSE;
import static no.nav.regoppslag.pdl.MapPDLResponse.FORTROLIG;
import static no.nav.regoppslag.pdl.MapPDLResponse.STRENGT_FORTROLIG;
import static no.nav.regoppslag.pdl.MapPDLResponse.STRENGT_FORTROLIG_UTLAND;
import static no.nav.regoppslag.pdl.MapPDLResponse.UKJENT_ADRESSE_REASON_CODE;
import static no.nav.regoppslag.rest.PostAdresseController.BEHANDLINGSNUMMER_HEADER;
import static no.nav.regoppslag.rest.PostAdresseController.POSTADRESSE_URI_PATH;
import static no.nav.regoppslag.rest.RegisteroppslagRestController.REST;
import static no.nav.regoppslag.rreg003.PostadresseServiceValidator.ADRESSEBESKYTTELSE_TYPE;
import static no.nav.regoppslag.rreg003.PostadresseType.NORSKPOSTADRESSE;
import static no.nav.regoppslag.rreg003.PostadresseType.UTENLANDSKPOSTADRESSE;
import static no.nav.regoppslag.util.NavHeaders.NAV_REASON_CODE;
import static no.nav.regoppslag.util.PDLResponseUtil.postPdlGraphql;
import static no.nav.regoppslag.util.PDLResponseUtil.postPdlGraphqlWithCustomBehandlingsnummer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.GONE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class Rreg003IT extends AbstractIT {

	private static final String VALID_IDENT = "01020304051";
	private static final String INVALID_IDENT_TOO_SHORT = "123";
	private static final String INVALID_IDENT_NOT_NUMERIC = "123456abc";
	private static final String INVALID_BEHANDLINGSNUMMER_TOO_LONG = "B1234";
	private static final String INVALID_BEHANDLINGSNUMMER_TOO_SHORT = "B12";
	private static final String INVALID_BEHANDLINGSNUMMER_BAD_FORMAT_SMALL_FIRST_LETTER = "b123";
	private static final String INVALID_BEHANDLINGSNUMMER_BAD_FORMAT_TWO_LETTERS = "BB13";
	private static final String INVALID_BEHANDLINGSNUMMER_BAD_FORMAT_TWO_LETTERS_THREE_NUMBERS = "BB123";
	private static final String ORG_IDENT = "889640782";

	@Test
	public void shouldThrowUnauthorizedWithoutValidToken() {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth("Bearer combustible potato");
		PostadresseRequest postadresseRequest = createPostadresseRequest(VALID_IDENT);

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, new HttpEntity<>(postadresseRequest, headers), PostadresseResponse.class));

		assertEquals(UNAUTHORIZED, e.getStatusCode());
	}

	@ParameterizedTest
	@MethodSource
	public void shouldReturnBadRequestForInvalidInput(String ident, String behandlingsnummer, String feilmelding) {
		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequestWithBehandlingsnummer(ident, behandlingsnummer), PostadresseResponse.class));

		assertEquals(BAD_REQUEST, e.getStatusCode());
		assertThat(e.getMessage()).contains(feilmelding);
	}

	private static Stream<Arguments> shouldReturnBadRequestForInvalidInput() {
		return Stream.of(
				Arguments.of(null, null, "Ident kan ikke være null"),
				Arguments.of(INVALID_IDENT_TOO_SHORT, null, "Ident må ha lengde på 9, 11 eller 13 siffer"),
				Arguments.of(INVALID_IDENT_NOT_NUMERIC, null, "Ident kan kun bestå av tall"),
				Arguments.of(VALID_IDENT, INVALID_BEHANDLINGSNUMMER_TOO_LONG, "Behandlingsnummer må bestå av en stor bokstav og tre etterfølgende siffer. Eks B123"),
				Arguments.of(VALID_IDENT, INVALID_BEHANDLINGSNUMMER_TOO_SHORT, "Behandlingsnummer må bestå av en stor bokstav og tre etterfølgende siffer. Eks B123"),
				Arguments.of(VALID_IDENT, INVALID_BEHANDLINGSNUMMER_BAD_FORMAT_SMALL_FIRST_LETTER, "Behandlingsnummer må bestå av en stor bokstav og tre etterfølgende siffer. Eks B123"),
				Arguments.of(VALID_IDENT, INVALID_BEHANDLINGSNUMMER_BAD_FORMAT_TWO_LETTERS, "Behandlingsnummer må bestå av en stor bokstav og tre etterfølgende siffer. Eks B123"),
				Arguments.of(VALID_IDENT, INVALID_BEHANDLINGSNUMMER_BAD_FORMAT_TWO_LETTERS_THREE_NUMBERS, "Behandlingsnummer må bestå av en stor bokstav og tre etterfølgende siffer. Eks B123")
		);
	}

	@ParameterizedTest
	@CsvSource(value = {"B123", "null"}, nullValues = {"null"})
	public void shouldReturnOkForValidBehandlingsnummer(String behandlingsnummer) {
		postPdlGraphqlWithCustomBehandlingsnummer(OK.value(), "pdl/postbokskontaktadresse.json", behandlingsnummer);
		ResponseEntity<PostadresseResponse> response = restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequestWithBehandlingsnummer(VALID_IDENT, behandlingsnummer), PostadresseResponse.class);

		assertEquals(OK, response.getStatusCode());
	}

	@Test
	public void shouldReturnOkForCommaSeparatedBehandlingsnummerListe() {
		postPdlGraphqlWithCustomBehandlingsnummer(OK.value(), "pdl/postbokskontaktadresse.json", "B123,A456");
		ResponseEntity<PostadresseResponse> response = restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequestWithBehandlingsnummer(VALID_IDENT, "B123,A456"), PostadresseResponse.class);

		assertEquals(OK, response.getStatusCode());
	}

	@Test
	public void shouldReturnBadRequestForInvalidBehandlingsnummerInCommaSeparatedListe() {
		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequestWithBehandlingsnummer(VALID_IDENT, "B123,invalid"), PostadresseResponse.class));

		assertEquals(BAD_REQUEST, e.getStatusCode());
		assertThat(e.getMessage()).contains("Behandlingsnummer må bestå av en stor bokstav og tre etterfølgende siffer. Eks B123");
	}

	@Test
	public void shouldGetPersonMedNorskPostadresse() {
		postPdlGraphql(OK.value(), "pdl/postbokskontaktadresse.json");

		PostadresseResponse reponse = hentPostadresse();

		assertThat(reponse.getNavn()).isEqualTo("GYNGEHEST A. ÅPENHJERTIG");

		Adresse actualAdresse = reponse.getAdresse();
		assertThat(actualAdresse.getAdresselinje1()).isEqualTo("C/O Finnesveien 27");
		assertThat(actualAdresse.getAdresselinje2()).isEqualTo("Postboks 7320");
		assertThat(actualAdresse.getAdresselinje3()).isNull();
		assertThat(actualAdresse.getPostnummer()).isEqualTo("7320");
		assertThat(actualAdresse.getPoststed()).isEqualTo("FANNREM");
		assertThat(actualAdresse.getLand()).isEqualTo("NORGE");
		assertThat(actualAdresse.getLandkode()).isEqualTo("NO");
	}

	@Test
	public void shouldMapAndGetKomplettBrevdataForBostedsadresseWithNullGyldigFraOgMed() {
		postPdlGraphql(OK.value(), "pdl/bosattadresse_with_null_gyldigFraOgMed.json");
		PostadresseResponse reponse = hentPostadresse();

		assertThat(reponse.getNavn()).isEqualTo("BJARNE BETJENT");

		Adresse actualAdresse = reponse.getAdresse();
		assertThat(actualAdresse.getType()).isEqualTo(UTENLANDSKPOSTADRESSE);
		assertThat(actualAdresse.getAdresseKilde()).isEqualTo(BOSTEDSADRESSE);
		assertThat(actualAdresse.getAdresselinje1()).isEqualTo("ALLEE DE NARCASTET");
		assertThat(actualAdresse.getAdresselinje2()).isEqualTo("LOTISSEMENT NARCASTET");
		assertThat(actualAdresse.getAdresselinje3()).isEqualTo("60000 NARCASTET");
		assertThat(actualAdresse.getPostnummer()).isNull();
		assertThat(actualAdresse.getPoststed()).isNull();
		assertThat(actualAdresse.getLandkode()).isEqualTo("FR");
		assertThat(actualAdresse.getLand()).isEqualTo("FRANKRIKE");
	}

	@Test
	public void shouldChooseKontaktadresseOverBostedsadresseDespiteNewerGyldigFraOgMed() {
		postPdlGraphql(OK.value(), "pdl/kontaktadresse_registrert_foer_bostedsadresse.json");
		PostadresseResponse reponse = hentPostadresse();

		assertThat(reponse.getNavn()).isEqualTo("BJARNE BETJENT");

		Adresse actualAdresse = reponse.getAdresse();
		assertThat(actualAdresse.getType()).isEqualTo(NORSKPOSTADRESSE);
		assertThat(actualAdresse.getAdresseKilde()).isEqualTo(KONTAKTADRESSE);
		assertThat(actualAdresse.getAdresselinje1()).isEqualToIgnoringCase("Postboks 001");
		assertThat(actualAdresse.getAdresselinje2()).isNull();
		assertThat(actualAdresse.getAdresselinje3()).isNull();
		assertThat(actualAdresse.getPostnummer()).isEqualTo("0306");
		assertThat(actualAdresse.getPoststed()).isEqualToIgnoringCase("OSLO");
		assertThat(actualAdresse.getLandkode()).isEqualTo("NO");
		assertThat(actualAdresse.getLand()).isEqualTo("NORGE");
	}

	@Test
	public void shouldChooseNorskBostedsadresseWhenKontaktadresseHasExpired() {
		postPdlGraphql(OK.value(), "pdl/norsk_bosattadresse_kontaktadresse_gyldigTilOgMed_passert.json");
		PostadresseResponse reponse = hentPostadresse();

		assertThat(reponse.getNavn()).isEqualTo("BJARNE B. TJENT");

		Adresse actualAdresse = reponse.getAdresse();
		assertThat(actualAdresse.getType()).isEqualTo(NORSKPOSTADRESSE);
		assertThat(actualAdresse.getAdresseKilde()).isEqualTo(BOSTEDSADRESSE);
		assertThat(actualAdresse.getAdresselinje1()).isEqualToIgnoringCase("Finnesveien 45B");
		assertThat(actualAdresse.getAdresselinje2()).isNull();
		assertThat(actualAdresse.getAdresselinje3()).isNull();
		assertThat(actualAdresse.getPostnummer()).isEqualTo("7320");
		assertThat(actualAdresse.getPoststed()).isEqualToIgnoringCase("FANNREM");
		assertThat(actualAdresse.getLandkode()).isEqualTo("NO");
		assertThat(actualAdresse.getLand()).isEqualTo("NORGE");
	}

	@Test
	void shouldChooseUtenlandskKontaktadresseWhenGyldigAndUkjentNorskPostnummer() {
		postPdlGraphql(OK.value(), "pdl/utenlandsk_kontaktadresse_ugyldig_norsk_postnummer.json");
		PostadresseResponse reponse = hentPostadresse();

		assertThat(reponse.getNavn()).isEqualTo("BJARNE BETJENT");

		Adresse actualAdresse = reponse.getAdresse();
		assertThat(actualAdresse.getType()).isEqualTo(UTENLANDSKPOSTADRESSE);
		assertThat(actualAdresse.getAdresseKilde()).isEqualTo(KONTAKTADRESSE);
		assertThat(actualAdresse.getAdresselinje1()).isEqualTo("Prahagata 1");
		assertThat(actualAdresse.getAdresselinje2()).isEqualTo("10000 PRAHA");
		assertThat(actualAdresse.getAdresselinje3()).isNull();
		assertThat(actualAdresse.getPostnummer()).isNull();
		assertThat(actualAdresse.getPoststed()).isNull();
		assertThat(actualAdresse.getLandkode()).isEqualTo("CZ");
		assertThat(actualAdresse.getLand()).isEqualTo("TSJEKKIA");
	}

	@Test
	void shouldChooseNewestNameWhenMultipleMastersWithDifferentNames() {
		postPdlGraphql(OK.value(), "pdl/utenlandsk_kontaktadresse_flere_navn.json");
		PostadresseResponse reponse = hentPostadresse();

		assertThat(reponse.getNavn()).isEqualTo("BJARNE STASJONSMESTER");
	}

	@Test
	public void shouldSettSinglePostboksStringAsPrefixFromPDLAdresseWhichStartsWithPostboks() {
		postPdlGraphql(OK.value(), "pdl/kontaktadresse_with_postboks_prefix.json");

		PostadresseResponse reponse = hentPostadresse();

		assertThat(reponse.getNavn()).isEqualTo("GYNGEHEST A. ÅPENHJERTIG");

		Adresse actualAdresse = reponse.getAdresse();
		assertThat(actualAdresse.getAdresselinje1()).isEqualTo("C/O Finnesveien 27");
		assertThat(actualAdresse.getAdresselinje2()).isEqualTo("Postboks 7320");
		assertThat(actualAdresse.getAdresselinje3()).isNull();
		assertThat(actualAdresse.getPostnummer()).isEqualTo("7320");
		assertThat(actualAdresse.getPoststed()).isEqualTo("FANNREM");
		assertThat(actualAdresse.getLand()).isEqualTo("NORGE");
		assertThat(actualAdresse.getLandkode()).isEqualTo("NO");
	}

	@Test
	public void shouldThrowUkjentAdresseExceptionWhenPostboksAdresseIsNull() {
		postPdlGraphql(OK.value(), "pdl/kontaktadresse_with_null_postboks.json");

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, this::hentPostadresse);

		assertThat(e.getMessage()).contains("Fant ikke adresse for personen i PDL");
		assertThat(e.getResponseHeaders().get(NAV_REASON_CODE)).contains(UKJENT_ADRESSE_REASON_CODE);
	}

	@Test
	public void shouldGetPersonMedNorskPostadresseOgCoAdresseUtenPrefix() {
		postPdlGraphql(OK.value(), "pdl/kontaktadressemedcoadresseutenco.json");

		PostadresseResponse reponse = hentPostadresse();

		assertThat(reponse.getNavn()).isEqualTo("TRIVIELL SKILPADDE");

		Adresse actualAdresse = reponse.getAdresse();
		assertThat(actualAdresse.getAdresselinje1()).isEqualTo("C/O Max Mekker");
		assertThat(actualAdresse.getAdresselinje2()).isEqualTo("Sesam Stasjon 1A");
		assertThat(actualAdresse.getAdresselinje3()).isNull();
		assertThat(actualAdresse.getPostnummer()).isEqualTo("1461");
		assertThat(actualAdresse.getPoststed()).isEqualTo("LØRENSKOG");
		assertThat(actualAdresse.getLand()).isEqualTo("NORGE");
		assertThat(actualAdresse.getLandkode()).isEqualTo("NO");
	}

	@Test
	void shouldGetSisteGyldigeKontaktAdresseWhenFlereGyldigeKontaktadresser() {
		postPdlGraphql(OK.value(), "pdl/kontaktadresse_flere_gyldige.json");

		PostadresseResponse response = hentPostadresse();

		assertThat(response.getNavn()).isEqualTo("FLERE GYLDIGE KONTAKTADRESSER");

		Adresse actualAdresse = response.getAdresse();
		assertThat(actualAdresse.getAdresselinje1()).isEqualTo("Postboks 9000 Grønland");
		assertThat(actualAdresse.getAdresselinje2()).isNull();
		assertThat(actualAdresse.getAdresselinje3()).isNull();
		assertThat(actualAdresse.getPostnummer()).isEqualTo("0134");
		assertThat(actualAdresse.getPoststed()).isEqualTo("OSLO");
		assertThat(actualAdresse.getLand()).isEqualTo("NORGE");
		assertThat(actualAdresse.getLandkode()).isEqualTo("NO");
	}

	@Test
	void shouldGetGyldigKontaktadresseWhenBostedsadresseUkjentBostedAndEqualOrMoreRecentGyldighetsdato() {
		postPdlGraphql(OK.value(), "pdl/bostedsadresse_ukjent_gyldig_kontaktadresse.json");

		PostadresseResponse response = hentPostadresse();

		assertThat(response.getNavn()).isEqualTo("BJARNE BETJENT");

		Adresse actualAdresse = response.getAdresse();
		assertThat(actualAdresse.getAdresselinje1()).isEqualTo("Polengata 1");
		assertThat(actualAdresse.getAdresselinje2()).isEqualTo("01-001 LODZ");
		assertThat(actualAdresse.getAdresselinje3()).isEqualTo("POLEN");
		assertThat(actualAdresse.getPostnummer()).isNull();
		assertThat(actualAdresse.getPoststed()).isNull();
		assertThat(actualAdresse.getLand()).isEqualTo("POLEN");
		assertThat(actualAdresse.getLandkode()).isEqualTo("PL");
	}

	@Test
	void shouldGetGyldigKontaktadresseWhenLastEndringAfterBostedsadresseGyldigFraOgMed() {
		postPdlGraphql(OK.value(), "pdl/kontaktadresse_with_endring_after_bostedsadresse.json");

		PostadresseResponse response = hentPostadresse();

		assertThat(response.getNavn()).isEqualTo("BJARNE BETJENT");

		Adresse actualAdresse = response.getAdresse();
		assertThat(actualAdresse.getAdresseKilde()).isEqualTo(KONTAKTADRESSE);
		assertThat(actualAdresse.getAdresselinje1()).isEqualTo("Polengata 1");
		assertThat(actualAdresse.getAdresselinje2()).isEqualTo("01-001 LODZ");
		assertThat(actualAdresse.getAdresselinje3()).isEqualTo("POLEN");
		assertThat(actualAdresse.getPostnummer()).isNull();
		assertThat(actualAdresse.getPoststed()).isNull();
		assertThat(actualAdresse.getLand()).isEqualTo("POLEN");
		assertThat(actualAdresse.getLandkode()).isEqualTo("PL");
	}

	@Test
	void shouldGetGyldigoppholdsadresseWhenLastEndringAfterBostedsadresseGyldigFraOgMed() {
		postPdlGraphql(OK.value(), "pdl/Oppholdsadresse_with_endring_after_bostedsadresse.json");

		PostadresseResponse response = hentPostadresse();

		assertThat(response.getNavn()).isEqualTo("AREMARK TESTFAMILIEN");

		Adresse actualAdresse = response.getAdresse();
		assertThat(actualAdresse.getAdresseKilde()).isEqualTo(OPPHOLDSADRESSE);
		assertThat(actualAdresse.getAdresselinje1()).isEqualTo("1KOLEJOWA 6/5");
		assertThat(actualAdresse.getAdresselinje2()).isNull();
		assertThat(actualAdresse.getAdresselinje3()).isNull();
		assertThat(actualAdresse.getPostnummer()).isNull();
		assertThat(actualAdresse.getPoststed()).isNull();
		assertThat(actualAdresse.getLand()).isEqualTo("POLEN");
		assertThat(actualAdresse.getLandkode()).isEqualTo("PL");
	}

	private PostadresseResponse hentPostadresse() {
		ResponseEntity<PostadresseResponse> response = restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequest(VALID_IDENT), PostadresseResponse.class);

		assertEquals(OK, response.getStatusCode());
		PostadresseResponse postadresseResponse = response.getBody();
		assertNotNull(postadresseResponse);

		return postadresseResponse;
	}

	@Test
	void shouldGetUtenlandskAdresseMedBystedOgPostkode() {
		postPdlGraphql(OK.value(), "pdl/utenlandskadresse_med_bysted_postkode.json");
		PostadresseResponse reponse = hentPostadresse();

		assertThat(reponse.getNavn()).isEqualTo("BJARNE BETJENT");

		Adresse actualAdresse = reponse.getAdresse();
		assertThat(actualAdresse.getType()).isEqualTo(UTENLANDSKPOSTADRESSE);
		assertThat(actualAdresse.getAdresseKilde()).isEqualTo(KONTAKTADRESSE);
		assertThat(actualAdresse.getAdresselinje1()).isEqualTo("799 E Dragram Suite 5A");
		assertThat(actualAdresse.getAdresselinje2()).isEqualTo("85705 Southampton");
		assertThat(actualAdresse.getAdresselinje3()).isNull();
		assertThat(actualAdresse.getPostnummer()).isNull();
		assertThat(actualAdresse.getPoststed()).isNull();
		assertThat(actualAdresse.getLandkode()).isEqualTo("GB");
		assertThat(actualAdresse.getLand()).isEqualTo("STORBRITANNIA");
	}


	/*
	 * Dersom den prioriterte adressen fra PDL ikke innholder grunnlaget for adrsselinje1,
	 * skal adresselinje2 flyttes til adresselinje1.
	 */
	@Test
	void shouldGetUtenlandskAdresseUtenGrunnlagForAdresselinje1() {
		postPdlGraphql(OK.value(), "pdl/utenlandsk_bostedsadressse_uten_adressenavnnummer.json");
		PostadresseResponse reponse = hentPostadresse();

		assertThat(reponse.getNavn()).isEqualTo("BJARNE BETJENT");

		Adresse actualAdresse = reponse.getAdresse();
		assertThat(actualAdresse.getType()).isEqualTo(UTENLANDSKPOSTADRESSE);
		assertThat(actualAdresse.getAdresseKilde()).isEqualTo(BOSTEDSADRESSE);
		assertThat(actualAdresse.getAdresselinje1()).isEqualTo("82-550 KLECZEWO");
		assertThat(actualAdresse.getAdresselinje2()).isNull();
		assertThat(actualAdresse.getAdresselinje3()).isNull();
		assertThat(actualAdresse.getPostnummer()).isNull();
		assertThat(actualAdresse.getPoststed()).isNull();
		assertThat(actualAdresse.getLandkode()).isEqualTo("PL");
		assertThat(actualAdresse.getLand()).isEqualTo("POLEN");
	}

	@Test
	void shouldGetUtenlandskAdresseMedUsaLandkode() {
		postPdlGraphql(OK.value(), "pdl/utenlandskadresse_med_usa_landkode.json");
		PostadresseResponse reponse = hentPostadresse();

		assertThat(reponse.getNavn()).isEqualTo("BJARNE BETJENT");

		Adresse actualAdresse = reponse.getAdresse();
		assertThat(actualAdresse.getType()).isEqualTo(UTENLANDSKPOSTADRESSE);
		assertThat(actualAdresse.getAdresseKilde()).isEqualTo(KONTAKTADRESSE);
		assertThat(actualAdresse.getAdresselinje1()).isEqualTo("799 E Dragram Suite 5A");
		assertThat(actualAdresse.getAdresselinje2()).isEqualTo("Tucson AZ 85705");
		assertThat(actualAdresse.getAdresselinje3()).isNull();
		assertThat(actualAdresse.getPostnummer()).isNull();
		assertThat(actualAdresse.getPoststed()).isNull();
		assertThat(actualAdresse.getLandkode()).isEqualTo("US");
		assertThat(actualAdresse.getLand()).isEqualTo("USA");
	}

	@Test
	void shouldUtenlandskAdresseMedPostkodeOgDistrikt() {
		postPdlGraphql(OK.value(), "pdl/utenlandskadresse_med_postkode_distrikt.json");
		PostadresseResponse reponse = hentPostadresse();

		assertThat(reponse.getNavn()).isEqualTo("BJARNE BETJENT");

		Adresse actualAdresse = reponse.getAdresse();
		assertThat(actualAdresse.getType()).isEqualTo(UTENLANDSKPOSTADRESSE);
		assertThat(actualAdresse.getAdresseKilde()).isEqualTo(KONTAKTADRESSE);
		assertThat(actualAdresse.getAdresselinje1()).isEqualTo("799 E Dragram Suite 5A");
		assertThat(actualAdresse.getAdresselinje2()).isEqualTo("85705, SO53 5PD");
		assertThat(actualAdresse.getAdresselinje3()).isNull();
		assertThat(actualAdresse.getPostnummer()).isNull();
		assertThat(actualAdresse.getPoststed()).isNull();
		assertThat(actualAdresse.getLandkode()).isEqualTo("GB");
		assertThat(actualAdresse.getLand()).isEqualTo("STORBRITANNIA");
	}

	@Test
	public void shouldGetPersonMedUtenlandskPostadresse() {
		postPdlGraphql(OK.value(), "pdl/utenlandsk_kontaktadresse.json");

		PostadresseResponse response = hentPostadresse();

		assertThat(response.getNavn()).isEqualTo("TORIA AB Pdl");

		Adresse actualAdresse = response.getAdresse();
		assertThat(actualAdresse.getAdresseKilde()).isEqualTo(KONTAKTADRESSE);
		assertThat(actualAdresse.getAdresselinje1()).isEqualTo("Trousis 1");
		assertThat(actualAdresse.getAdresselinje2()).isEqualTo("11111 Kalamaka");
		assertThat(actualAdresse.getAdresselinje3()).isNull();
		assertThat(actualAdresse.getPostnummer()).isNull();
		assertThat(actualAdresse.getPoststed()).isNull();
		assertThat(actualAdresse.getLand()).isEqualTo("HELLAS");
		assertThat(actualAdresse.getLandkode()).isEqualTo("GR");
	}

	@Test
	public void shouldGetUtenlandskPostadresseForDoedsbo() {
		postPdlGraphql(OK.value(), "pdl/pdl_utenlandsk_doedsbo_adresse.json");

		PostadresseResponse response = hentPostadresse();

		assertThat(response.getNavn()).isEqualTo("TORIA AB Pdl DØDSBO");

		Adresse actualAdresse = response.getAdresse();
		assertThat(actualAdresse.getAdresseKilde()).isEqualTo(KONTAKTINFORMASJONFORDØDSBO);
		assertThat(actualAdresse.getAdresselinje1()).isEqualTo("C/O TORIA AB Pdl");
		assertThat(actualAdresse.getAdresselinje2()).isEqualTo("Tysklandsveien 89");
		assertThat(actualAdresse.getAdresselinje3()).isEqualTo("12345 Berlin");
		assertThat(actualAdresse.getPostnummer()).isNull();
		assertThat(actualAdresse.getPoststed()).isNull();
		assertThat(actualAdresse.getLand()).isEqualTo("TYSKLAND");
		assertThat(actualAdresse.getLandkode()).isEqualTo("DE");
	}

	@Test
	public void shouldGetNorskPostadresseForDoedsbo() {
		postPdlGraphql(OK.value(), "pdl/doedperson.json");

		PostadresseResponse response = hentPostadresse();

		assertThat(response.getNavn()).isEqualTo("GUL MÅPENDE KAKE DØDSBO");

		Adresse actualAdresse = response.getAdresse();
		assertThat(actualAdresse.getAdresseKilde()).isEqualTo(KONTAKTINFORMASJONFORDØDSBO);
		assertThat(actualAdresse.getAdresselinje1()).isEqualTo("C/O Herr Andersen");
		assertThat(actualAdresse.getAdresselinje2()).isEqualTo("Postboks 15");
		assertThat(actualAdresse.getAdresselinje3()).isNull();
		assertThat(actualAdresse.getPostnummer()).isEqualTo("7320");
		assertThat(actualAdresse.getPoststed()).isEqualTo("FANNREM");
		assertThat(actualAdresse.getLand()).isEqualTo("NORGE");
		assertThat(actualAdresse.getLandkode()).isEqualTo("NO");
	}

	@Test
	public void shouldThrowWhenDoedPersonUtenKontaktinformasjon() {
		postPdlGraphql(OK.value(), "pdl/doedpersonutenadresse.json");

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequest(VALID_IDENT), PostadresseResponse.class));

		assertEquals(GONE, e.getStatusCode());
	}

	@Test
	public void shouldThrowWhenPersonFinnesIkke() {
		postPdlGraphql(OK.value(), "pdl/ukjentbosted.json");

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequest(VALID_IDENT), PostadresseResponse.class));

		assertEquals(NOT_FOUND, e.getStatusCode());
	}

	@Test
	public void shouldGetOrganisasjonWithNorskPostadresse() {
		stubFor(get("/v1/organisasjon/" + ORG_IDENT).willReturn(aResponse()
				.withStatus(OK.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("treg002/ereg/ereg-happy.json")));

		ResponseEntity<PostadresseResponse> response = restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequest(ORG_IDENT), PostadresseResponse.class);

		var postadresse = response.getBody();
		assertEquals("YARA INTERNATIONAL ASA", postadresse.getNavn());
		assertEquals("Postboks 343  Skøyen", postadresse.getAdresse().getAdresselinje1());
		assertNull(postadresse.getAdresse().getAdresselinje2());
		assertNull(postadresse.getAdresse().getAdresselinje3());
		assertEquals("0213", postadresse.getAdresse().getPostnummer());
		assertEquals("OSLO", postadresse.getAdresse().getPoststed());
		assertEquals("NO", postadresse.getAdresse().getLandkode());
		assertEquals("Norge", postadresse.getAdresse().getLand());
	}

	@Test
	public void shouldGetOrganisasjonWithUtenlandskPostadresse() {
		stubFor(get("/v1/organisasjon/" + ORG_IDENT).willReturn(aResponse()
				.withStatus(OK.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("treg002/ereg/ereg-happy-utenlandsk-gb.json")));

		ResponseEntity<PostadresseResponse> response = restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequest(ORG_IDENT), PostadresseResponse.class);

		var postadresse = response.getBody();
		assertEquals("SUBSEA 7 (UK SERVICE COMPANY) LIMITED", postadresse.getNavn());
		assertEquals("Prospect Road", postadresse.getAdresse().getAdresselinje1());
		assertEquals("Arnhall Business Park, Westhill", postadresse.getAdresse().getAdresselinje2());
		assertEquals("ABERDEEN AB32 6FE", postadresse.getAdresse().getAdresselinje3());
		assertNull(postadresse.getAdresse().getPoststed());
		assertNull(postadresse.getAdresse().getPostnummer());
		assertEquals("GB", postadresse.getAdresse().getLandkode());
		assertEquals("STORBRITANNIA", postadresse.getAdresse().getLand());
	}

	@Test
	public void shouldThrowExceptionWhenFilterAdressebeskyttelseAndPdlResponseHaveInCommonFortroligGradering() {
		postPdlGraphql(OK.value(), "pdl/adresse_with_adressebeskyttelse_fortrolig.json");

		Set<String> gradering = of(FORTROLIG, STRENGT_FORTROLIG, STRENGT_FORTROLIG_UTLAND);

		ResponseEntity<PostadresseResponse> response = restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequestMedFilterAdressebeskyttelse(VALID_IDENT, gradering), PostadresseResponse.class);
		assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
	}

	@Test
	public void shouldReturnPostadresseResponseWhenFilterAdressebeskyttelseAndPdlResponseDoNotHaveInCommon() {
		postPdlGraphql(OK.value(), "pdl/adresse_with_adressebeskyttelse_fortrolig.json");

		Set<String> gradering = Set.of(STRENGT_FORTROLIG, STRENGT_FORTROLIG_UTLAND);

		ResponseEntity<PostadresseResponse> response = restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequestMedFilterAdressebeskyttelse(VALID_IDENT, gradering), PostadresseResponse.class);
		assertThat(response.getStatusCode()).isEqualTo(OK);

		Adresse actualAdresse = response.getBody().getAdresse();
		assertThat(actualAdresse.getAdresselinje1()).isEqualTo("C/O Finnesveien 27");
		assertThat(actualAdresse.getAdresselinje2()).isEqualTo("Postboks 7320");
		assertThat(actualAdresse.getAdresselinje3()).isNull();
		assertThat(actualAdresse.getPostnummer()).isEqualTo("7320");
		assertThat(actualAdresse.getPoststed()).isEqualTo("FANNREM");
		assertThat(actualAdresse.getLand()).isEqualTo("NORGE");
		assertThat(actualAdresse.getLandkode()).isEqualTo("NO");
	}

	@Test
	public void shouldReturnPostadresseWithNoAdressebeskyttelseFilterAndPdlResponseIsUgradert() {
		postPdlGraphql(OK.value(), "pdl/adresse_with_adressebeskyttelse_ugradert.json");

		Set<String> gradering = Set.of();

		ResponseEntity<PostadresseResponse> response = restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequestMedFilterAdressebeskyttelse(VALID_IDENT, gradering), PostadresseResponse.class);
		assertThat(response.getStatusCode()).isEqualTo(OK);

		Adresse actualAdresse = response.getBody().getAdresse();
		assertThat(actualAdresse.getAdresselinje1()).isEqualTo("C/O Finnesveien 27");
		assertThat(actualAdresse.getAdresselinje2()).isEqualTo("Postboks 7320");
		assertThat(actualAdresse.getAdresselinje3()).isNull();
		assertThat(actualAdresse.getPostnummer()).isEqualTo("7320");
		assertThat(actualAdresse.getPoststed()).isEqualTo("FANNREM");
		assertThat(actualAdresse.getLand()).isEqualTo("NORGE");
		assertThat(actualAdresse.getLandkode()).isEqualTo("NO");
	}

	@Test
	public void shouldReturnNoContentWhenFilterAdressebeskyttelseAndPdlResponseHaveInCommonStrengtFortroligGradering() {
		postPdlGraphql(OK.value(), "pdl/adresse_with_adressebeskyttelse_strengt_fortrolig.json");

		ResponseEntity<PostadresseResponse> response = restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequestMedFilterAdressebeskyttelse(VALID_IDENT, ADRESSEBESKYTTELSE_TYPE), PostadresseResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
	}

	@Test
	public void shouldReturnNoContentWhenFilterAdressebeskyttelseAndPdlResponseHaveInCommonStrengtFortroligUtlandGradering() {
		postPdlGraphql(OK.value(), "pdl/utenlandskadresse_med_gradering.json");

		ResponseEntity<PostadresseResponse> response = restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequestMedFilterAdressebeskyttelse(VALID_IDENT, ADRESSEBESKYTTELSE_TYPE), PostadresseResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
	}

	@Test
	public void shouldThrowExceptionWhenFilterAdressebeskyttelseInputErInvalid() {
		postPdlGraphql(OK.value(), "pdl/adresse_with_adressebeskyttelse_fortrolig.json");

		Set<String> gradering = of("Strengt", STRENGT_FORTROLIG, STRENGT_FORTROLIG_UTLAND);

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () ->
				restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequestMedFilterAdressebeskyttelse(VALID_IDENT, gradering), Object.class));

		assertThat(e.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(e.getMessage()).contains("Fikk ugyldig filtrerAdressebeskyttelse=[Strengt]");
	}

	@Test
	public void shouldThrowExceptionWhenFilterAdressebeskyttelseErOverThreeInput() {
		postPdlGraphql(OK.value(), "pdl/adresse_with_adressebeskyttelse_fortrolig.json");

		Set<String> gradering = new HashSet<>(ADRESSEBESKYTTELSE_TYPE);
		gradering.add("ugradert");

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () ->
				restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequestMedFilterAdressebeskyttelse(VALID_IDENT, gradering), Object.class));

		assertThat(e.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(e.getMessage()).contains("Fikk ugyldig filtrerAdressebeskyttelse=[ugradert]");
	}

	@Test
	public void shouldThrowWhenOrganisasjonIkkeFinnes() {
		stubFor(get("/v1/organisasjon/" + ORG_IDENT)
				.willReturn(aResponse()
						.withStatus(NOT_FOUND.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("treg002/ereg/ereg-ikkefunnet.json")));

		NotFound e = assertThrows(NotFound.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, createRequest(ORG_IDENT), PostadresseResponse.class));

		assertEquals(NOT_FOUND, e.getStatusCode());
	}

	@Test
	public void shouldThrowBadRequestWhenOrganisasjonIkke() {
		assertThrows(HttpClientErrorException.BadRequest.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, createRequest("999999999a"), PostadresseResponse.class));
	}

	@Test
	public void shouldThrowWhenTekniskFeilFraEreg() {
		stubFor(get("/v1/organisasjon/" + ORG_IDENT)
				.willReturn(aResponse()
						.withStatus(INTERNAL_SERVER_ERROR.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("treg002/ereg/ereg-tekniskfeil.json")));

		HttpServerErrorException e = assertThrows(HttpServerErrorException.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, createRequest(ORG_IDENT), PostadresseResponse.class));

		assertEquals(INTERNAL_SERVER_ERROR, e.getStatusCode());
	}

	public HttpEntity<PostadresseRequest> createRequest(String ident) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(token("Rreg003IT"));
		PostadresseRequest postadresseRequest = createPostadresseRequest(ident);

		return new HttpEntity<>(postadresseRequest, headers);
	}

	public HttpEntity<PostadresseRequest> createRequestMedFilterAdressebeskyttelse(String ident, Set<String> gradering) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(token("Rreg003IT"));
		PostadresseRequest postadresseRequest = createPostadresseRequestMedGradering(ident, gradering);

		return new HttpEntity<>(postadresseRequest, headers);
	}

	public HttpEntity<PostadresseRequest> createRequestWithBehandlingsnummer(String ident, String behandlingsnummer) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(token("Rreg003IT"));
		headers.set(BEHANDLINGSNUMMER_HEADER, behandlingsnummer);
		PostadresseRequest postadresseRequest = createPostadresseRequest(ident);

		return new HttpEntity<>(postadresseRequest, headers);
	}

	private PostadresseRequest createPostadresseRequest(String ident) {
		return PostadresseRequest.builder()
				.ident(ident)
				.build();
	}

	private PostadresseRequest createPostadresseRequestMedGradering(String ident, Set<String> gradering) {
		return PostadresseRequest.builder()
				.ident(ident)
				.filtrerAdressebeskyttelse(gradering)
				.build();
	}
}