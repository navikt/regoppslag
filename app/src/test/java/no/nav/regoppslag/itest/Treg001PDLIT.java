package no.nav.regoppslag.itest;


import com.github.tomakehurst.wiremock.client.CountMatchingStrategy;
import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.regoppslag.treg001.KompletterBrevdataRequest;
import no.nav.regoppslag.treg001.KompletterBrevdataResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.nav.regoppslag.rest.RegisteroppslagRestController.KOMPLETTER_BREVDATA_URI_PATH;
import static no.nav.regoppslag.rest.RegisteroppslagRestController.REST;
import static no.nav.regoppslag.util.PDLResponseUtil.getStsToken;
import static no.nav.regoppslag.util.PDLResponseUtil.postPdlDigdir;
import static no.nav.regoppslag.util.PDLResponseUtil.postPdlGraphql;
import static no.nav.regoppslag.util.PDLResponseUtil.postPdlGraphqlWithErrorResponse;
import static no.nav.regoppslag.util.TestUtil.classpathToString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.GONE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class Treg001PDLIT extends AbstractIT {

	private static final String DOKUMENTTYPEID = "123";

	private String token;

	@BeforeEach
	public void runBefore() {
		WireMock.removeAllMappings();
		WireMock.resetAllRequests();
		WireMock.reset();

		stubFor(get(urlPathMatching("/DOKUMENTTYPEINFO_V4(.*)"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader("Content-Type", "application/json")
						.withBodyFile("treg001/dokkat/dokkat_happy-response.json"))); //Brukes til hentDokumenttypeinfo for Spraak

		//Stub web services:
		stubFor(post("/ORGANISASJONENHETKONTAKTINFORMASJON_V1")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/norg/happy-response.xml")));

		stubFor(post("/STS")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("felles/sts/sts_signature-responsebody.xml"))); //mottakerPlugin

		stubAzureToken();
		this.token = token("subject1");
	}

	/**
	 * Kompletterer fullt brevdatasett der mottaker er person
	 */
	@Test
	public void shouldGetKomplettBrevdataPerson() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/BosattVegadresse.json");
		postPdlDigdir(HttpStatus.OK.value(), "dkif/dkif-happy.json");
		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class);
		assertThat(actualResponse.getBrevdata()).isEqualTo(classpathToString("__files/treg001pdl/treg001pdl_full_response.xml"));
	}

	@Test
	public void shouldGetKomplettBrevdataForPersonWithPostboksAdresse() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/postbokskontaktadresse.json");
		postPdlDigdir(HttpStatus.OK.value(), "dkif/dkif-happy.json");
		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class);
		assertThat(actualResponse.getBrevdata()).isEqualTo(classpathToString("__files/treg001pdl/treg001_postboks_response.xml"));
	}


	@Test
	public void shouldMapAndGetKomplettBrevdataForPersonWithNullForkortetnavn() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/bosattadresse_with_null_forkortetnavn.json");
		postPdlDigdir(HttpStatus.OK.value(), "dkif/dkif-happy.json");
		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class);
		assertThat(actualResponse.getBrevdata()).isEqualTo(classpathToString("__files/treg001pdl/treg001pdl_forkortetnavn_response.xml"));
	}

	@Test
	public void shouldMapAndGetKomplettBrevdataForPersonWithBosattadresseMedMatrikkeladresse() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/bosattadressemedmatrikkeladresse.json");
		postPdlDigdir(HttpStatus.OK.value(), "dkif/dkif-happy.json");
		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class);
		assertThat(actualResponse.getBrevdata()).isEqualTo(classpathToString("__files/treg001pdl/treg001_response_matrikkeladresse.xml"));
	}


	@Test
	public void shouldGetKomplettBrevdataPersonMaalFormEN() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/doedperson.json");
		postPdlDigdir(HttpStatus.OK.value(), "dkif/dkif-happy.json");

		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class);
		assertThat(actualResponse
				.getBrevdata()).isEqualTo(classpathToString("__files/treg001pdl/treg001_response_maalform_en.xml"));
	}

	@Test
	public void shouldGetKomplettBrevdataPersonMaalFormIkkeSkandinavisk() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/bosattadressemedconavn.json");
		postPdlDigdir(HttpStatus.OK.value(), "dkif/dkif-happy.json");

		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class);
		assertThat(actualResponse.getBrevdata()).isEqualTo(classpathToString("__files/treg001pdl/treg001_response_maalform_sv.xml"));
	}

	@Test
	public void shouldGetKomplettBrevdataPersonMaalFormDansk() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/kontaktadresse.json");
		postPdlDigdir(HttpStatus.OK.value(), "dkif/dkif-happy.json");
		//mottakerPlugin
		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class);
		assertThat(actualResponse.getBrevdata()).isEqualTo(classpathToString("__files/treg001pdl/treg001_kontaktadresse_response.xml"));
	}

	/**
	 * Komplertterer fullt brevdatasett der mottaker er organisasjon
	 */
	@Test
	public void shouldGetKomplettBrevdataOrg() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		stubFor(post("/ORGANISASJONENHETKONTAKTINFORMASJON_V1")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/norg/happy-response.xml")));
		stubFor(get("/v1/organisasjon/" + "111111111")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("treg001/ereg/ereg-happy.json")));
		postPdlDigdir(HttpStatus.OK.value(), "dkif/dkif-happy.json");
		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request_orgv4.xml"), KompletterBrevdataResponse.class);
		assertThat(actualResponse.getBrevdata()).isEqualTo(classpathToString("__files/treg001pdl/treg001_full_response_orgv4.xml"));
	}

	@Test
	public void shouldGetKomplettBrevdataOrgIkkeSkandinavisk() {
		stubFor(get("/v1/organisasjon/" + "111111111")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("treg001/ereg/ereg-happy_ikke_skandinavisk.json")));
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request_orgv4.xml"), KompletterBrevdataResponse.class);

		assertThat(actualResponse.getBrevdata()).isEqualTo(classpathToString("__files/treg001pdl/treg001_full_response_orgv4_en.xml"));
	}

	@Test
	public void shouldNotMapBehandlendeEnhetWhenBerikIsFalse() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/bosattadressemedconavn.json");
		postPdlDigdir(HttpStatus.OK.value(), "dkif/dkif-happy.json");
		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request_behandlende_enhet_8020.xml"), KompletterBrevdataResponse.class);
		assertThat(actualResponse.getBrevdata()).isEqualTo(classpathToString("__files/treg001pdl/treg001_full_response_behandlendeEnhet_8020.xml"));
	}

	@Test
	public void shouldGetKomplettBrevdataWhenDoedPerson() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/doedperson.json");
		postPdlDigdir(HttpStatus.OK.value(), "dkif/dkif-happy.json");
		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class);
		assertThat(actualResponse
				.getBrevdata()).isEqualTo(classpathToString("__files/treg001pdl/treg001_full_response_dodperson.xml"));
	}

	/**
	 * Testbetingelser:
	 * -HVIS det oppstår en funksjonell feil for   et brevdataelement i en berikerplugin SÅ oppdater feillogg funksjonelle feil   OG fortsett til neste brevdataelement
	 * - HVIS det er opprettet en feillogg funksjonelle feil SÅ SKAL loggen returneres
	 */
	@Test
	public void shouldThrowFunctionalExceptionFromOrgPlugin() {
		//Stub web services:
		stubFor(get("/v1/organisasjon/" + "111111111")
				.willReturn(aResponse().withStatus(NOT_FOUND.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("treg001/ereg/ereg-ikkefunnet.json")));

		HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_request_orgv4.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");
		verify(getRequestedFor(urlEqualTo("/v1/organisasjon/111111111")));
		assertThat(e.getStatusCode()).isEqualTo(NOT_FOUND);
		assertThat(e.getResponseBodyAsString()).contains("Fant ikke Organisasjon med organisasjonsnummer=111111111");
	}

	@Test
	public void shouldThrowTechnicalIfPersonIsMissingAdresse() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/ukjentbosted.json");
		postPdlDigdir(HttpStatus.OK.value(), "dkif/dkif-happy.json");
		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class),
				"Should throw techical Exception");

		verify(postRequestedFor(urlEqualTo("/graphql")));
		assertThat(e.getStatusCode()).isEqualTo(NOT_FOUND);
		assertThat(e.getResponseBodyAsString()).contains("Funksjonell feil: dokumenttypeId=123 feilmelding=TREG001: Kunne ikke mappe postadresse for UkjentBosted mottaker");
	}

	@Test
	public void shouldThrowWhenPDLFailsFunctionalInvalidSecurityToken() {
		getStsToken(HttpStatus.BAD_REQUEST.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/doedperson.json");
		postPdlDigdir(HttpStatus.OK.value(), "dkif/dkif-happy.json");
		HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");

		assertThat(e.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
		verify(15, getRequestedFor(urlEqualTo("/stsRest/token?grant_type=client_credentials&scope=openid")));
	}

	@Test
	public void shouldThrowWhenPersonHasUkjentAdresse() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/kontaktinformasjonfordoedsbo.json"); //mottakerPlugin
		postPdlDigdir(BAD_REQUEST.value(), "dkif/dkif-happy.json");
		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");
		assertThat(e.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(e.getResponseBodyAsString()).contains("Funksjonell feil: dokumenttypeId=123 feilmelding=Funksjonell feil ved kall mot Digdir KRR. Feilmelding=400 Bad Request");
	}

	@Test
	public void shouldThrowWhenPDLFailsSecurityErrorNoAccess() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/unauthenticated-error-response.json");

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");
		assertThat(e.getStatusCode()).isEqualTo(BAD_REQUEST);
	}

	@Test
	public void shouldThrowFunctionalExceptionFromPersonPlugin() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/bosattutenpostdresse.json");
		postPdlDigdir(HttpStatus.OK.value(), "dkif/dkif-happy.json");
		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");

		verify(postRequestedFor(urlEqualTo("/graphql")));
		assertThat(e.getStatusCode()).isEqualTo(NOT_FOUND);
	}

	@Test
	public void shouldThrowFunctionalExceptionFromNorgPlugins() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		stubFor(post("/ORGANISASJONENHETKONTAKTINFORMASJON_V1")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/norg/hentEnhet-FunksjonellFeil-EnhetIkkeFunnet.xml"))); //mottakerPlugin
		postPdlDigdir(HttpStatus.OK.value(), "dkif/dkif-happy.json");
		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_norg2_request.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");

		verify(postRequestedFor(urlEqualTo("/ORGANISASJONENHETKONTAKTINFORMASJON_V1")));
		assertThat(e.getStatusCode()).isEqualTo(NOT_FOUND);
	}

	@Test
	public void shouldThrowNotFoundExceptionFromPDL() {
		//Stub web services:
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphqlWithErrorResponse(NOT_FOUND.value());
		postPdlDigdir(HttpStatus.OK.value(), "dkif/dkif-happy.json");
		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");

		verify(1, postRequestedFor(urlEqualTo("/graphql")));
		assertThat(e.getStatusCode()).isEqualTo(NOT_FOUND);
	}

	@Test
	public void shouldThrowTechnicalExceptionFromOrgPlugin() {
		//Stub web services:
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/bosattutenpostdresse.json");
		postPdlDigdir(HttpStatus.OK.value(), "dkif/dkif-happy.json");
		stubFor(get("/v1/organisasjon/111111111")
				.willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.value())));
		HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_request_orgv4.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");

		verify(new CountMatchingStrategy(CountMatchingStrategy.GREATER_THAN_OR_EQUAL, 5), getRequestedFor(urlEqualTo("/v1/organisasjon/111111111")));
		assertThat(e.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
		assertThat(e.getResponseBodyAsString()).contains("Teknisk feil mot hentOrganisasjon for organisasjon med organisasjonsnummer=111111111");
	}

	@Test
	public void shouldThrowTechnicalExceptionFromNorgPlugin() {
		//Stub web services:
		stubFor(post("/ORGANISASJONENHETKONTAKTINFORMASJON_V1")
				.willReturn(notFound().withStatus(NOT_FOUND.value())));
		HttpServerErrorException e = assertThrows(HttpServerErrorException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_norg2_request.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");
		verify(new CountMatchingStrategy(CountMatchingStrategy.GREATER_THAN_OR_EQUAL, 5), postRequestedFor(urlEqualTo("/ORGANISASJONENHETKONTAKTINFORMASJON_V1")));
		assertThat(e.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
	}

	@Test
	public void shouldThrowTechnicalExceptionWhenReceivedNotFoundFromDokkat() {
		//Stub web services:
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/BosattVegadresse.json");
		postPdlDigdir(HttpStatus.OK.value(), "dkif/dkif-happy.json");
		stubFor(get(urlPathMatching("/DOKUMENTTYPEINFO_V4(.*)"))
				.willReturn(aResponse().withStatus(NOT_FOUND.value())
						.withHeader("Content-Type", "application/json")
						.withBodyFile("treg001/dokkat/dokkat_happy-response.json")));
		HttpServerErrorException e = assertThrows(HttpServerErrorException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");
		verify(getRequestedFor(urlEqualTo("/DOKUMENTTYPEINFO_V4/123")));
		assertThat(e.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
	}

	@Test
	public void shouldThrowTechnicalExceptionFromDokkat() {
		//Stub web services:
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/BosattVegadresse.json");
		postPdlDigdir(HttpStatus.OK.value(), "dkif/dkif-happy.json");
		stubFor(get(urlPathMatching("/DOKUMENTTYPEINFO_V4(.*)"))
				.willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.value())
						.withHeader("Content-Type", "application/json")
						.withBodyFile("treg001/dokkat/dokkat_happy-response.json")));
		HttpServerErrorException e = assertThrows(HttpServerErrorException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");
		verify(new CountMatchingStrategy(CountMatchingStrategy.GREATER_THAN_OR_EQUAL, 5), getRequestedFor(urlEqualTo("/DOKUMENTTYPEINFO_V4/123")));
		assertThat(e.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
	}

	@Test
	public void shouldLogWithStatusCodeGoneIfPersonErDoedOgUtenKontaktAdresse() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/doedpersonutenadresse.json");
		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class));
		verify(postRequestedFor(urlEqualTo("/graphql")));
		assertThat(e.getStatusCode()).isEqualTo(GONE);
	}

	private HttpEntity<KompletterBrevdataRequest> createRequest(String path) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + this.token);

		KompletterBrevdataRequest kompletterBrevdataRequest = KompletterBrevdataRequest.builder()
				.dokumentTypeId(DOKUMENTTYPEID)
				.brevdata(classpathToString(path))
				.tema("FRI")
				.build();

		return new HttpEntity<>(kompletterBrevdataRequest, headers);
	}
}
