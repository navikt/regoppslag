package no.nav.regoppslag.itest;

import no.nav.regoppslag.rreg003.Adresse;
import no.nav.regoppslag.rreg003.PostadresseRequest;
import no.nav.regoppslag.rreg003.PostadresseResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpClientErrorException.NotFound;
import org.springframework.web.client.HttpServerErrorException;

import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static no.nav.regoppslag.rest.PostAdresseController.POSTADRESSE_URI_PATH;
import static no.nav.regoppslag.rest.RegisteroppslagRestController.REST;
import static no.nav.regoppslag.util.PDLResponseUtil.getStsToken;
import static no.nav.regoppslag.util.PDLResponseUtil.postPdlGraphql;
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
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class Rreg003IT extends AbstractIT {

	private static final String VALID_IDENT = "01020304051";
	private static final String VALID_TEMA = "PEN";
	private static final String INVALID_IDENT = "123";
	private static final String INVALID_TEMA = "testetest";
	private final String ORG_IDENT = "889640782";

	@Test
	public void shouldThrowUnauthorizedWithoutValidToken() {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth("Bearer combustible potato");
		PostadresseRequest postadresseRequest = createPostadresseRequest(VALID_IDENT, VALID_TEMA);

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, new HttpEntity<>(postadresseRequest, headers), PostadresseResponse.class));

		assertEquals(UNAUTHORIZED, e.getStatusCode());
	}

	@ParameterizedTest
	@MethodSource
	public void shouldThrowWhenBadRequestWithInvalidInput(String ident, String tema) {
		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequest(ident, tema), PostadresseResponse.class),
				"Test did not throw exception");

		assertEquals(BAD_REQUEST, e.getStatusCode());
	}

	private static Stream<Arguments> shouldThrowWhenBadRequestWithInvalidInput() {
		return Stream.of(
				Arguments.of(VALID_IDENT, INVALID_TEMA),
				Arguments.of(INVALID_IDENT, VALID_TEMA)
		);
	}

	@Test
	public void shouldGetPersonMedNorskPostadresse() {
		getStsToken(OK.value(), "sts/stsResponse_happy.json");
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
	void shouldGetSisteGyldigeKontaktAdresseWhenFlereGyldigeKontaktadresser() {
		getStsToken(OK.value(), "sts/stsResponse_happy.json");
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

	private PostadresseResponse hentPostadresse() {
		ResponseEntity<PostadresseResponse> response = restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequest(VALID_IDENT, VALID_TEMA), PostadresseResponse.class);

		assertEquals(OK, response.getStatusCode());
		PostadresseResponse postadresseResponse = response.getBody();
		assertNotNull(postadresseResponse);

		return postadresseResponse;
	}

	@Test
	public void shouldGetPersonMedUtenlandskPostadresse() {
		getStsToken(OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(OK.value(), "pdl/utenlandsk_kontaktadresse.json");

		ResponseEntity<PostadresseResponse> response = restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequest(VALID_IDENT, VALID_TEMA), PostadresseResponse.class);

		assertNotNull(response);
		assertEquals(OK, response.getStatusCode());
		assertNotNull(response.getBody().getNavn());
		assertNotNull(response.getBody().getAdresse());
	}

	@Test
	public void shouldGetUtenlandskPostadresseForDoedsbo() {
		getStsToken(OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(OK.value(), "pdl/pdl_utenlandsk_doedsbo_adresse.json");

		ResponseEntity<PostadresseResponse> response = restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequest(VALID_IDENT, VALID_TEMA), PostadresseResponse.class);

		assertNotNull(response);
		assertEquals(OK, response.getStatusCode());
		assertNotNull(response.getBody().getNavn());
		assertNotNull(response.getBody().getAdresse());
	}

	@Test
	public void shouldGetDoedPerson() {
		getStsToken(OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(OK.value(), "pdl/doedperson.json");

		ResponseEntity<PostadresseResponse> actualResponse = restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequest(VALID_IDENT, VALID_TEMA), PostadresseResponse.class);

		assertNotNull(actualResponse);
		assertEquals(OK, actualResponse.getStatusCode());
		assertNotNull(actualResponse.getBody().getNavn());
		assertNotNull(actualResponse.getBody().getAdresse());
	}

	@Test
	public void shouldThrowWhenDoedPersonUtenKontaktinformasjon() {
		getStsToken(OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(OK.value(), "pdl/doedpersonutenadresse.json");

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequest(VALID_IDENT, VALID_TEMA), PostadresseResponse.class),
				"Test did not throw exception");

		assertEquals(GONE, e.getStatusCode());
	}

	@Test
	public void shouldThrowWhenPersonFinnesIkke() {
		getStsToken(OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(OK.value(), "pdl/ukjentbosted.json");

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequest(VALID_IDENT, VALID_TEMA), PostadresseResponse.class),
				"Test did not throw exception");

		assertEquals(NOT_FOUND, e.getStatusCode());
	}

	@Test
	public void shouldGetOrganisasjonWithNorskPostadresse() {
		stubFor(get("/v1/organisasjon/" + ORG_IDENT).willReturn(aResponse()
				.withStatus(OK.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("treg002/ereg/ereg-happy.json")));

		ResponseEntity<PostadresseResponse> response = restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequest(ORG_IDENT, VALID_TEMA), PostadresseResponse.class);

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

		ResponseEntity<PostadresseResponse> response = restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequest(ORG_IDENT, VALID_TEMA), PostadresseResponse.class);

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
	public void shouldThrowWhenOrganisasjonIkkeFinnes() {
		stubFor(get("/v1/organisasjon/" + ORG_IDENT)
				.willReturn(aResponse()
						.withStatus(NOT_FOUND.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("treg002/ereg/ereg-ikkefunnet.json")));

		NotFound e = assertThrows(NotFound.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, createRequest(ORG_IDENT, VALID_TEMA), PostadresseResponse.class));

		assertEquals(NOT_FOUND, e.getStatusCode());
	}

	@Test
	public void shouldThrowWhenTekniskFeilFraEreg() {
		stubFor(get("/v1/organisasjon/" + ORG_IDENT)
				.willReturn(aResponse()
						.withStatus(INTERNAL_SERVER_ERROR.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("treg002/ereg/ereg-tekniskfeil.json")));

		HttpServerErrorException e = assertThrows(HttpServerErrorException.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, createRequest(ORG_IDENT, VALID_TEMA), PostadresseResponse.class));

		assertEquals(INTERNAL_SERVER_ERROR, e.getStatusCode());
	}

	public HttpEntity<PostadresseRequest> createRequest(String ident, String tema) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(token("Rreg003IT"));
		PostadresseRequest postadresseRequest = createPostadresseRequest(ident, tema);

		return new HttpEntity<>(postadresseRequest, headers);
	}

	private PostadresseRequest createPostadresseRequest(String ident, String tema) {
		return PostadresseRequest.builder()
				.ident(ident)
				.tema(tema)
				.build();
	}

}
