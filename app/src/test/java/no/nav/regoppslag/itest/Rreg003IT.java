package no.nav.regoppslag.itest;

import com.github.tomakehurst.wiremock.client.WireMock;
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
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.nav.regoppslag.rest.PostAdresseController.POSTADRESSE_URI_PATH;
import static no.nav.regoppslag.rest.RegisteroppslagRestController.REST;
import static no.nav.regoppslag.util.PDLResponseUtil.PERSON_IDENT;
import static no.nav.regoppslag.util.PDLResponseUtil.getStsToken;
import static no.nav.regoppslag.util.PDLResponseUtil.postPdlGraphql;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

		ResponseEntity<PostadresseResponse> actualResponse = restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequest(VALID_IDENT, VALID_TEMA), PostadresseResponse.class);
		assertNotNull(actualResponse);
		assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
		assertNotNull(actualResponse.getBody().getNavn());
		assertNotNull(actualResponse.getBody().getAdresse());

		verify(1, postRequestedFor(urlMatching("/graphql")));
		verify(1, getRequestedFor(urlEqualTo("/stsRest/token?grant_type=client_credentials&scope=openid")));
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

		verify(1, postRequestedFor(urlMatching("/graphql")));
		verify(1, getRequestedFor(urlEqualTo("/stsRest/token?grant_type=client_credentials&scope=openid")));
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

		verify(1, postRequestedFor(urlMatching("/graphql")));
		verify(1, getRequestedFor(urlEqualTo("/stsRest/token?grant_type=client_credentials&scope=openid")));
	}

	@Test
	public void shouldThrowWhenDodPersonUtenKontaktinformasjon() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/doedpersonutenadresse.json");

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequest(VALID_IDENT, VALID_TEMA), PostadresseResponse.class),
				"Test did not throw exception");
		assertEquals(HttpStatus.GONE, e.getStatusCode());

		verify(1, postRequestedFor(urlMatching("/graphql")));
		verify(1, getRequestedFor(urlEqualTo("/stsRest/token?grant_type=client_credentials&scope=openid")));
	}

	@Test
	public void shouldThrowWhenPersonFinnesIkke() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/ukjentbosted.json");

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequest(VALID_IDENT, VALID_TEMA), PostadresseResponse.class),
				"Test did not throw exception");
		assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());

		verify(1, postRequestedFor(urlMatching("/graphql")));
		verify(1, getRequestedFor(urlEqualTo("/stsRest/token?grant_type=client_credentials&scope=openid")));
	}

	@Test
	public void shouldGetOrganisasjonWithNorskPostadresse() {
		stubFor(get("/v1/organisasjon/" + ORG_IDENT)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("treg002/ereg/ereg-happy.json")));
		ResponseEntity<PostadresseResponse> response = restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequest(ORG_IDENT, VALID_TEMA), PostadresseResponse.class);

		assertEquals("NAV IKT", response.getBody().getNavn());

	}

	@Test
	public void shouldThrowWhenOrganisasjonFinnesIkke() {
		stubFor(get("/v1/organisasjon/" + ORG_IDENT)
				.willReturn(aResponse().withStatus(NOT_FOUND.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("treg002/ereg/ereg-ikkefunnet.json")));
		HttpClientErrorException.NotFound e = assertThrows(HttpClientErrorException.NotFound.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, createRequest(ORG_IDENT, VALID_TEMA), PostadresseResponse.class));

		assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
	}

	@Test
	public void shouldThrowWhenOrganisasjonTekniskFeil() {
		stubFor(get("/v1/organisasjon/" + ORG_IDENT)
				.willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
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
