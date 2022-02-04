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
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.nav.regoppslag.rest.PostAdresseController.POSTADRESSE_URI_PATH;
import static no.nav.regoppslag.rest.RegisteroppslagRestController.REST;
import static no.nav.regoppslag.util.PDLResponseUtil.getStsToken;
import static no.nav.regoppslag.util.PDLResponseUtil.postPdlGraphql;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpMethod.POST;

public class Rreg003IT extends AbstractIT {

	String VALID_IDENT = "01020304051";
	String INVALID_IDENT = "123";
	String VALID_TEMA = "PEN";
	String INVALID_TEMA = "testetest";
	String ORG_IDENT = "889640782";

	@BeforeEach
	public void setUpStubs() {
		WireMock.reset();
		WireMock.resetAllRequests();
		WireMock.removeAllMappings();
	}

	@Test
	public void shouldThrowUnauthorizedWithoutValidToken() {
		String token = "invalid_token";

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequest(token, VALID_IDENT, VALID_TEMA), PostadresseResponse.class),
				"Test did not throw exception");

		assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
	}

	@Test
	public void shouldThrowWhenBadRequestWithInvalidInput() {
		String token = token("subject1");

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequest(token, VALID_IDENT, INVALID_TEMA), PostadresseResponse.class),
				"Test did not throw exception");
		assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());

		HttpClientErrorException e2 = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequest(token, INVALID_IDENT, VALID_TEMA), PostadresseResponse.class),
				"Test did not throw exception");
		assertEquals(HttpStatus.BAD_REQUEST, e2.getStatusCode());
	}

	@Test
	public void shouldGetPersonMedNorskPostadresse() {
		String token = token("subject1");

		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/postbokskontaktadresse.json");

		ResponseEntity<PostadresseResponse> actualResponse = restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequest(token, VALID_IDENT, VALID_TEMA), PostadresseResponse.class);
		assertNotNull(actualResponse);
		assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
		assertNotNull(actualResponse.getBody().getNavn());
		assertNotNull(actualResponse.getBody().getAdresse());

		verify(1, postRequestedFor(urlMatching("/graphql")));
		verify(1, getRequestedFor(urlEqualTo("/stsRest/token?grant_type=client_credentials&scope=openid")));
	}

	@Test
	public void shouldGetPersonMedUtenlandskPostadresse() {
		String token = token("subject1");

		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/utenlandsk_kontaktadresse.json");

		ResponseEntity<PostadresseResponse> actualResponse = restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequest(token, VALID_IDENT, VALID_TEMA), PostadresseResponse.class);
		assertNotNull(actualResponse);
		assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
		assertNotNull(actualResponse.getBody().getNavn());
		assertNotNull(actualResponse.getBody().getAdresse());

		verify(1, postRequestedFor(urlMatching("/graphql")));
		verify(1, getRequestedFor(urlEqualTo("/stsRest/token?grant_type=client_credentials&scope=openid")));
	}

	@Test
	public void shouldGetDodPerson() {
		String token = token("subject1");

		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/doedperson.json");

		ResponseEntity<PostadresseResponse> actualResponse = restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequest(token, VALID_IDENT, VALID_TEMA), PostadresseResponse.class);
		assertNotNull(actualResponse);
		assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
		assertNotNull(actualResponse.getBody().getNavn());
		assertNotNull(actualResponse.getBody().getAdresse());

		verify(1, postRequestedFor(urlMatching("/graphql")));
		verify(1, getRequestedFor(urlEqualTo("/stsRest/token?grant_type=client_credentials&scope=openid")));
	}

	@Test
	public void shouldThrowWhenDodPersonUtenKontaktinformasjon() {
		String token = token("subject1");

		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/doedpersonutenadresse.json");

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequest(token, VALID_IDENT, VALID_TEMA), PostadresseResponse.class),
				"Test did not throw exception");
		assertEquals(HttpStatus.GONE, e.getStatusCode());

		verify(1, postRequestedFor(urlMatching("/graphql")));
		verify(1, getRequestedFor(urlEqualTo("/stsRest/token?grant_type=client_credentials&scope=openid")));
	}

	@Test
	public void shouldThrowWhenPersonFinnesIkke() {
		String token = token("subject1");

		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/ukjentbosted.json");

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequest(token, VALID_IDENT, VALID_TEMA), PostadresseResponse.class),
				"Test did not throw exception");
		assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());

		verify(1, postRequestedFor(urlMatching("/graphql")));
		verify(1, getRequestedFor(urlEqualTo("/stsRest/token?grant_type=client_credentials&scope=openid")));
	}

	@Test
	public void shouldGetOrganisasjonWithNorskPostadresse() {
		String token = token("subject1");

		stubFor(post("/ORGANISASJON_V4")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/organisasjonv4/organisasjonv4-happy.xml")));
		ResponseEntity<PostadresseResponse> response = restTemplate.exchange(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, POST, createRequest(token, ORG_IDENT, VALID_TEMA), PostadresseResponse.class);

		assertEquals("ARBEIDS- OG VELFERDSETATEN", response.getBody().getNavn());

	}

	@Test
	public void shouldThrowWhenOrganisasjonFinnesIkke() {
		String token = token("subject1");

		stubFor(post("/ORGANISASJON_V4")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/organisasjonv4/organisasjonv4-ikkefunnet-response.xml")));
		HttpClientErrorException.NotFound e = assertThrows(HttpClientErrorException.NotFound.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, createRequest(token, ORG_IDENT, VALID_TEMA), PostadresseResponse.class));

		assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
	}

	@Test
	public void shouldThrowWhenOrganisasjonTekniskFeil() {
		String token = token("subject1");

		stubFor(post("/ORGANISASJON_V4")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/organisasjonv4/organisasjonv4-tekniskfeil-response.xml")));
		HttpServerErrorException e = assertThrows(HttpServerErrorException.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + POSTADRESSE_URI_PATH, createRequest(token, ORG_IDENT, VALID_TEMA), PostadresseResponse.class));

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatusCode());
	}

	private HttpEntity<PostadresseRequest> createRequest(String token, String ident, String tema) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + token);
		PostadresseRequest postadresseRequest = PostadresseRequest.builder()
				.ident(ident)
				.tema(tema)
				.build();
		return new HttpEntity<>(postadresseRequest, headers);
	}
}
