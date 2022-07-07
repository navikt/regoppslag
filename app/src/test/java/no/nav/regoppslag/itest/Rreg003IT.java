package no.nav.regoppslag.itest;

import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.regoppslag.rreg003.Adresse;
import no.nav.regoppslag.rreg003.PostadresseRequest;
import no.nav.regoppslag.rreg003.PostadresseResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

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
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class Rreg003IT extends AbstractIT {

	String VALID_IDENT = "01020304051";
	String INVALID_IDENT = "123";
	String VALID_TEMA = "PEN";
	String INVALID_TEMA = "testetest";
	String ORG_IDENT = "889640782";

	private String token;

	@BeforeEach
	public void setUpStubs() {
		WireMock.reset();
		WireMock.resetAllRequests();
		WireMock.removeAllMappings();

		this.token = token("subject1");
	}

	@Test
	public void shouldThrowUnauthorizedWithoutValidToken() {
		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequestInvalidToken(VALID_IDENT, VALID_TEMA), PostadresseResponse.class),
				"Test did not throw exception");

		assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
	}

	@Test
	public void shouldThrowWhenBadRequestWithInvalidInput() {
		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequest(VALID_IDENT, INVALID_TEMA), PostadresseResponse.class),
				"Test did not throw exception");
		assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());

		HttpClientErrorException e2 = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequest(INVALID_IDENT, VALID_TEMA), PostadresseResponse.class),
				"Test did not throw exception");
		assertEquals(HttpStatus.BAD_REQUEST, e2.getStatusCode());
	}

	@Test
	public void shouldGetPersonMedNorskPostadresse() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/postbokskontaktadresse.json");

		PostadresseResponse postadresseResponse = hentPostadresse();

		assertThat(postadresseResponse.getNavn()).isEqualTo("GYNGEHEST A. ÅPENHJERTIG");
		Adresse actualAdresse = postadresseResponse.getAdresse();
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
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/kontaktadresse_flere_gyldige.json");

		PostadresseResponse postadresseResponse = hentPostadresse();

		assertThat(postadresseResponse.getNavn()).isEqualTo("FLERE GYLDIGE KONTAKTADRESSER");
		Adresse actualAdresse = postadresseResponse.getAdresse();
		assertThat(actualAdresse.getAdresselinje1()).isEqualTo("Postboks 9000 Grønland");
		assertThat(actualAdresse.getAdresselinje2()).isNull();
		assertThat(actualAdresse.getAdresselinje3()).isNull();
		assertThat(actualAdresse.getPostnummer()).isEqualTo("0134");
		assertThat(actualAdresse.getPoststed()).isEqualTo("OSLO");
		assertThat(actualAdresse.getLand()).isEqualTo("NORGE");
		assertThat(actualAdresse.getLandkode()).isEqualTo("NO");
	}

	private PostadresseResponse hentPostadresse() {
		ResponseEntity<PostadresseResponse> actualResponse = restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequest(VALID_IDENT, VALID_TEMA), PostadresseResponse.class);
		assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
		PostadresseResponse postadresseResponse = actualResponse.getBody();
		assertNotNull(postadresseResponse);
		return postadresseResponse;
	}

	@Test
	public void shouldGetPersonMedUtenlandskPostadresse() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/utenlandsk_kontaktadresse.json");

		ResponseEntity<PostadresseResponse> actualResponse = restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequest(VALID_IDENT, VALID_TEMA), PostadresseResponse.class);
		assertNotNull(actualResponse);
		assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
		assertNotNull(actualResponse.getBody().getNavn());
		assertNotNull(actualResponse.getBody().getAdresse());
	}

	@Test
	public void shouldGetDodPerson() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/doedperson.json");

		ResponseEntity<PostadresseResponse> actualResponse = restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequest(VALID_IDENT, VALID_TEMA), PostadresseResponse.class);
		assertNotNull(actualResponse);
		assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
		assertNotNull(actualResponse.getBody().getNavn());
		assertNotNull(actualResponse.getBody().getAdresse());
	}

	@Test
	public void shouldThrowWhenDodPersonUtenKontaktinformasjon() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/doedpersonutenadresse.json");

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequest(VALID_IDENT, VALID_TEMA), PostadresseResponse.class),
				"Test did not throw exception");
		assertEquals(HttpStatus.GONE, e.getStatusCode());
	}

	@Test
	public void shouldThrowWhenPersonFinnesIkke() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/ukjentbosted.json");

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequest(VALID_IDENT, VALID_TEMA), PostadresseResponse.class),
				"Test did not throw exception");
		assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
	}

	@Test
	public void shouldGetOrganisasjonWithNorskPostadresse() {
		stubFor(get("/v1/organisasjon/" + ORG_IDENT)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("treg002/ereg/ereg-happy.json")));
		ResponseEntity<PostadresseResponse> response = restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequest(ORG_IDENT, VALID_TEMA), PostadresseResponse.class);

		assertEquals("NAV IKT", response.getBody().getNavn());
	}

	@Test
	public void shouldGetOrganisasjonWithUtenlandskPostadresse() {
		stubFor(get("/v1/organisasjon/" + ORG_IDENT)
				.willReturn(aResponse().withStatus(OK.value())
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
	public void shouldThrowWhenOrganisasjonFinnesIkke() {
		stubFor(get("/v1/organisasjon/" + ORG_IDENT)
				.willReturn(aResponse().withStatus(NOT_FOUND.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("treg002/ereg/ereg-ikkefunnet.json")));
		HttpClientErrorException.NotFound e = assertThrows(HttpClientErrorException.NotFound.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, createRequest(ORG_IDENT, VALID_TEMA), PostadresseResponse.class));

		assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
	}

	@Test
	public void shouldThrowWhenOrganisasjonTekniskFeil() {
		stubFor(get("/v1/organisasjon/" + ORG_IDENT)
				.willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("treg002/ereg/ereg-tekniskfeil.json")));
		HttpServerErrorException e = assertThrows(HttpServerErrorException.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, createRequest(ORG_IDENT, VALID_TEMA), PostadresseResponse.class));

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatusCode());
	}

	private HttpEntity<PostadresseRequest> createRequest(String ident, String tema) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + this.token);
		PostadresseRequest postadresseRequest = PostadresseRequest.builder()
				.ident(ident)
				.tema(tema)
				.build();
		return new HttpEntity<>(postadresseRequest, headers);
	}

	private HttpEntity<PostadresseRequest> createRequestInvalidToken(String ident, String tema) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer combustible potato");
		PostadresseRequest postadresseRequest = PostadresseRequest.builder()
				.ident(ident)
				.tema(tema)
				.build();
		return new HttpEntity<>(postadresseRequest, headers);
	}
}
