package no.nav.regoppslag.itest;


import com.github.tomakehurst.wiremock.client.CountMatchingStrategy;
import no.nav.regoppslag.treg001.KompletterBrevdataRequest;
import no.nav.regoppslag.treg001.KompletterBrevdataResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import static com.github.tomakehurst.wiremock.client.CountMatchingStrategy.GREATER_THAN_OR_EQUAL;
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
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
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
		stubDokkatResponse();
		stubNorg();
		stubSts();

		stubAzureToken();
		this.token = token("subject1");
	}

	/**
	 * Kompletterer fullt brevdatasett der mottaker er person
	 */
	@Test
	public void shouldGetKomplettBrevdataPerson() {
		getStsToken(OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(OK.value(), "pdl/BosattVegadresse.json");
		postPdlDigdir(OK.value(), "dkif/dkif-happy.json");

		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class);

		assertThat(actualResponse.getBrevdata()).isEqualTo(classpathToString("__files/treg001pdl/treg001pdl_full_response.xml"));
	}

	@Test
	public void shouldGetKomplettBrevdataForPersonWithPostboksAdresse() {
		getStsToken(OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(OK.value(), "pdl/postbokskontaktadresse.json");
		postPdlDigdir(OK.value(), "dkif/dkif-happy.json");

		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class);

		assertThat(actualResponse.getBrevdata()).isEqualTo(classpathToString("__files/treg001pdl/treg001_postboks_response.xml"));
	}


	@Test
	public void shouldMapAndGetKomplettBrevdataForPersonWithNullForkortetnavn() {
		getStsToken(OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(OK.value(), "pdl/bosattadresse_with_null_forkortetnavn.json");
		postPdlDigdir(OK.value(), "dkif/dkif-happy.json");

		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class);

		assertThat(actualResponse.getBrevdata()).isEqualTo(classpathToString("__files/treg001pdl/treg001pdl_forkortetnavn_response.xml"));
	}

	@Test
	public void shouldMapAndGetKomplettBrevdataForPersonWithBosattadresseMedMatrikkeladresse() {
		getStsToken(OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(OK.value(), "pdl/bosattadressemedmatrikkeladresse.json");
		postPdlDigdir(OK.value(), "dkif/dkif-happy.json");

		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class);

		assertThat(actualResponse.getBrevdata()).isEqualTo(classpathToString("__files/treg001pdl/treg001_response_matrikkeladresse.xml"));
	}

	@Test
	public void shouldGetKomplettBrevdataPersonMaalFormEN() {
		getStsToken(OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(OK.value(), "pdl/doedperson.json");
		postPdlDigdir(OK.value(), "dkif/dkif-happy.json");

		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class);

		assertThat(actualResponse.getBrevdata()).isEqualTo(classpathToString("__files/treg001pdl/treg001_response_maalform_en.xml"));
	}

	@Test
	public void shouldGetKomplettBrevdataPersonMaalFormIkkeSkandinavisk() {
		getStsToken(OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(OK.value(), "pdl/bosattadressemedconavn.json");
		postPdlDigdir(OK.value(), "dkif/dkif-happy.json");

		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class);

		assertThat(actualResponse.getBrevdata()).isEqualTo(classpathToString("__files/treg001pdl/treg001_response_maalform_sv.xml"));
	}

	@Test
	public void shouldGetKomplettBrevdataPersonMaalFormDansk() {
		getStsToken(OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(OK.value(), "pdl/kontaktadresse.json");
		postPdlDigdir(OK.value(), "dkif/dkif-happy.json");

		//mottakerPlugin
		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class);

		assertThat(actualResponse.getBrevdata()).isEqualTo(classpathToString("__files/treg001pdl/treg001_kontaktadresse_response.xml"));
	}

	/**
	 * Kompletterer fullt brevdatasett der mottaker er organisasjon
	 */
	@Test
	public void shouldGetKomplettBrevdataOrg() {
		getStsToken(OK.value(), "sts/stsResponse_happy.json");
		stubNorg();
		postPdlDigdir(OK.value(), "dkif/dkif-happy.json");
		stubFor(get("/v1/organisasjon/" + "111111111")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("treg001/ereg/ereg-happy.json")));

		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request_orgv4.xml"), KompletterBrevdataResponse.class);

		assertThat(actualResponse.getBrevdata()).isEqualTo(classpathToString("__files/treg001pdl/treg001_full_response_orgv4.xml"));
	}

	@Test
	public void shouldGetKomplettBrevdataOrgIkkeSkandinavisk() {
		getStsToken(OK.value(), "sts/stsResponse_happy.json");
		stubFor(get("/v1/organisasjon/" + "111111111")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("treg001/ereg/ereg-happy_ikke_skandinavisk.json")));

		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request_orgv4.xml"), KompletterBrevdataResponse.class);

		assertThat(actualResponse.getBrevdata()).isEqualTo(classpathToString("__files/treg001pdl/treg001_full_response_orgv4_en.xml"));
	}

	@Test
	public void shouldNotMapBehandlendeEnhetWhenBerikIsFalse() {
		getStsToken(OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(OK.value(), "pdl/bosattadressemedconavn.json");
		postPdlDigdir(OK.value(), "dkif/dkif-happy.json");

		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request_behandlende_enhet_8020.xml"), KompletterBrevdataResponse.class);

		assertThat(actualResponse.getBrevdata()).isEqualTo(classpathToString("__files/treg001pdl/treg001_full_response_behandlendeEnhet_8020.xml"));
	}

	@Test
	public void shouldGetKomplettBrevdataWhenDoedPerson() {
		getStsToken(OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(OK.value(), "pdl/doedperson.json");
		postPdlDigdir(OK.value(), "dkif/dkif-happy.json");

		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class);

		assertThat(actualResponse.getBrevdata()).isEqualTo(classpathToString("__files/treg001pdl/treg001_full_response_dodperson.xml"));
	}

	@Test
	public void shouldGetKomplettBrevdataWhenDkifPersonIkkeFunnet() {
		getStsToken(OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(OK.value(), "pdl/BosattVegadresse.json");
		postPdlDigdir(OK.value(), "dkif/ikke-funnet.json");

		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class);

		assertThat(actualResponse.getBrevdata()).isEqualTo(classpathToString("__files/treg001pdl/treg001pdl_full_response.xml"));
	}

	/**
	 * Testbetingelser:
	 * - HVIS det oppstår en funksjonell feil for et brevdataelement i en berikerplugin SÅ oppdater feillogg funksjonelle feil OG fortsett til neste brevdataelement
	 * - HVIS det er opprettet en feillogg funksjonelle feil SÅ SKAL loggen returneres
	 */
	@Test
	public void shouldReturnNotFoundFromOrgPluginWhenOrgIsNotFoundInEreg() {
		stubFor(get("/v1/organisasjon/" + "111111111")
				.willReturn(aResponse()
						.withStatus(NOT_FOUND.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("treg001/ereg/ereg-ikkefunnet.json")));

		HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_request_orgv4.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");

		verify(getRequestedFor(urlEqualTo("/v1/organisasjon/111111111")));
		assertThat(e.getStatusCode()).isEqualTo(NOT_FOUND);
		assertThat(e.getResponseBodyAsString()).contains("Fant ikke Organisasjon med organisasjonsnummer=111111111");
	}

	@Test
	public void shouldReturnNotFoundIfPersonIsMissingAdresseInPdl() {
		getStsToken(OK.value(), "sts/stsResponse_happy.json");
		postPdlDigdir(OK.value(), "dkif/dkif-happy.json");
		postPdlGraphql(OK.value(), "pdl/ukjentbosted.json");

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class),
				"Should throw techical Exception");

		verify(postRequestedFor(urlEqualTo("/graphql")));
		assertThat(e.getStatusCode()).isEqualTo(NOT_FOUND);
		assertThat(e.getResponseBodyAsString()).contains("Funksjonell feil: dokumenttypeId=123 feilmelding=TREG001: Kunne ikke mappe postadresse for UkjentBosted mottaker");
	}

	@Test
	public void shouldReturnInternalServerErrorWhenPDLFailsGetsInvalidSecurityToken() {
		postPdlDigdir(OK.value(), "dkif/dkif-happy.json");
		postPdlGraphql(OK.value(), "pdl/doedperson.json");
		getStsToken(BAD_REQUEST.value(), "sts/stsResponse_happy.json");

		HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");

		assertThat(e.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
		verify(15, getRequestedFor(urlEqualTo("/stsRest/token?grant_type=client_credentials&scope=openid")));
	}

	@Test
	public void shouldReturnBadRequestWhenPersonHasUkjentAdresse() {
		getStsToken(OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(OK.value(), "pdl/kontaktinformasjonfordoedsbo.json"); //mottakerPlugin
		postPdlDigdir(BAD_REQUEST.value(), "dkif/dkif-happy.json");

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");

		assertThat(e.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(e.getResponseBodyAsString()).contains("Funksjonell feil: dokumenttypeId=123 feilmelding=Funksjonell feil ved kall mot Digdir KRR. Feilmelding=400 Bad Request");
	}

	@Test
	public void shouldReturnBadRequestWhenPDLFailsSecurityErrorNoAccess() {
		getStsToken(OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(OK.value(), "pdl/unauthenticated-error-response.json");

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");

		assertThat(e.getStatusCode()).isEqualTo(BAD_REQUEST);
	}

	@Test
	public void shouldReturnNotFoundIfFunctionalExceptionFromPersonPlugin() {
		getStsToken(OK.value(), "sts/stsResponse_happy.json");
		postPdlDigdir(OK.value(), "dkif/dkif-happy.json");
		postPdlGraphql(OK.value(), "pdl/bosattutenpostdresse.json");

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");

		verify(postRequestedFor(urlEqualTo("/graphql")));
		assertThat(e.getStatusCode()).isEqualTo(NOT_FOUND);
	}

	@Test
	public void shouldReturnNotFoundIfFunctionalExceptionFromNorgPlugins() {
		getStsToken(OK.value(), "sts/stsResponse_happy.json");
		postPdlDigdir(OK.value(), "dkif/dkif-happy.json");
		stubFor(post("/ORGANISASJONENHETKONTAKTINFORMASJON_V1").willReturn(aResponse()
				.withStatus(OK.value())
				.withBodyFile("treg001/norg/hentEnhet-FunksjonellFeil-EnhetIkkeFunnet.xml"))); //mottakerPlugin

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_norg2_request.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");

		verify(postRequestedFor(urlEqualTo("/ORGANISASJONENHETKONTAKTINFORMASJON_V1")));
		assertThat(e.getStatusCode()).isEqualTo(NOT_FOUND);
	}

	@Test
	public void shouldReturnNotFoundIfNotFoundFromPDL() {
		getStsToken(OK.value(), "sts/stsResponse_happy.json");
		postPdlDigdir(OK.value(), "dkif/dkif-happy.json");
		postPdlGraphqlWithErrorResponse(NOT_FOUND.value());

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");


		verify(1, postRequestedFor(urlEqualTo("/graphql")));
		assertThat(e.getStatusCode()).isEqualTo(NOT_FOUND);
	}

	@Test
	public void shouldReturnInternalServerErrorIfInternalServerErrorFromOrgPlugin() {
		getStsToken(OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(OK.value(), "pdl/bosattutenpostdresse.json");
		postPdlDigdir(OK.value(), "dkif/dkif-happy.json");
		stubFor(get("/v1/organisasjon/111111111").willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.value())));

		HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_request_orgv4.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");

		verify(new CountMatchingStrategy(GREATER_THAN_OR_EQUAL, 5), getRequestedFor(urlEqualTo("/v1/organisasjon/111111111")));
		assertThat(e.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
		assertThat(e.getResponseBodyAsString()).contains("Teknisk feil mot hentOrganisasjon for organisasjon med organisasjonsnummer=111111111");
	}

	@Test
	public void shouldReturnInternalServerErrorIfNotFoundFromNorgPlugin() {
		stubFor(post("/ORGANISASJONENHETKONTAKTINFORMASJON_V1").willReturn(notFound().withStatus(NOT_FOUND.value())));

		HttpServerErrorException e = assertThrows(HttpServerErrorException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_norg2_request.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");

		verify(new CountMatchingStrategy(GREATER_THAN_OR_EQUAL, 5), postRequestedFor(urlEqualTo("/ORGANISASJONENHETKONTAKTINFORMASJON_V1")));
		assertThat(e.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
	}

	@Test
	public void shouldReturnInternalServerErrorWhenNotFoundFromDokkat() {
		getStsToken(OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(OK.value(), "pdl/BosattVegadresse.json");
		postPdlDigdir(OK.value(), "dkif/dkif-happy.json");
		stubFor(get(urlPathMatching("/DOKUMENTTYPEINFO_V4(.*)")).willReturn(aResponse().withStatus(NOT_FOUND.value())));

		HttpServerErrorException e = assertThrows(HttpServerErrorException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");

		verify(getRequestedFor(urlEqualTo("/DOKUMENTTYPEINFO_V4/123")));
		assertThat(e.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
	}

	@Test
	public void shouldReturnInternalServerErrorIfTechnicalExceptionFromDokkat() {
		getStsToken(OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(OK.value(), "pdl/BosattVegadresse.json");
		postPdlDigdir(OK.value(), "dkif/dkif-happy.json");
		stubFor(get(urlPathMatching("/DOKUMENTTYPEINFO_V4(.*)")).willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.value())));

		HttpServerErrorException e = assertThrows(HttpServerErrorException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");

		verify(new CountMatchingStrategy(GREATER_THAN_OR_EQUAL, 5), getRequestedFor(urlEqualTo("/DOKUMENTTYPEINFO_V4/123")));
		assertThat(e.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
	}

	@Test
	public void shouldReturnGoneIfPersonErDoedOgUtenKontaktAdresse() {
		getStsToken(OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(OK.value(), "pdl/doedpersonutenadresse.json");

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class));

		verify(postRequestedFor(urlEqualTo("/graphql")));
		assertThat(e.getStatusCode()).isEqualTo(GONE);
	}

	private HttpEntity<KompletterBrevdataRequest> createRequest(String path) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(token);

		KompletterBrevdataRequest kompletterBrevdataRequest = KompletterBrevdataRequest.builder()
				.dokumentTypeId(DOKUMENTTYPEID)
				.brevdata(classpathToString(path))
				.tema("FRI")
				.build();

		return new HttpEntity<>(kompletterBrevdataRequest, headers);
	}


	protected void stubSts() {
		stubFor(post("/STS").willReturn(aResponse()
				.withStatus(OK.value())
				.withBodyFile("felles/sts/sts_signature-responsebody.xml"))); //mottakerPlugin
	}

	protected void stubNorg() {
		stubFor(post("/ORGANISASJONENHETKONTAKTINFORMASJON_V1").willReturn(aResponse()
				.withStatus(OK.value())
				.withBodyFile("treg001/norg/happy-response.xml")));
	}

	protected void stubDokkatResponse() {
		stubFor(get(urlPathMatching("/DOKUMENTTYPEINFO_V4(.*)")).willReturn(aResponse()
				.withStatus(OK.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("treg001/dokkat/dokkat_happy-response.json"))); //Brukes til hentDokumenttypeinfo for Spraak
	}
}
