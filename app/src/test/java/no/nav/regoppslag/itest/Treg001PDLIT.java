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
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.nav.regoppslag.pdl.MapPDLResponse.ERROR_REASON_CODE;
import static no.nav.regoppslag.rest.RegisteroppslagRestController.KOMPLETTER_BREVDATA_URI_PATH;
import static no.nav.regoppslag.rest.RegisteroppslagRestController.REST;
import static no.nav.regoppslag.util.NavHeaders.NAV_REASON_CODE;
import static no.nav.regoppslag.util.PDLResponseUtil.postPdlDigdir;
import static no.nav.regoppslag.util.PDLResponseUtil.postPdlGraphql;
import static no.nav.regoppslag.util.PDLResponseUtil.postPdlGraphqlWithErrorResponse;
import static no.nav.regoppslag.util.PDLResponseUtil.stubGetEnhetKontaktInfo;
import static no.nav.regoppslag.util.PDLResponseUtil.stubGetEnhetNavn;
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
		stubMsGraphGetUser("Z991006");
		stubDokmetResponse();
		stubAzureToken();
		this.token = token("subject1");
	}

	/**
	 * Kompletterer fullt brevdatasett der mottaker er person
	 */
	@Test
	public void shouldGetKomplettBrevdataPerson() {
		stubGetEnhetNavn(OK.value(), "norg2/hentEnhet_happy.json");
		stubGetEnhetKontaktInfo(OK.value(), "norg2/hentEnhetKontaktInfo_happy.json");
		postPdlGraphql(OK.value(), "pdl/BosattVegadresse.json");
		postPdlDigdir(OK.value(), "dkif/dkif-happy.json");
		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class);

		assertThat(actualResponse.getBrevdata()).isEqualTo(classpathToString("__files/treg001pdl/treg001pdl_full_response.xml"));
	}

	@Test
	public void shouldGetKomplettBrevdataForPersonWithPostboksAdresse() {
		postPdlGraphql(OK.value(), "pdl/postbokskontaktadresse.json");
		postPdlDigdir(OK.value(), "dkif/dkif-happy.json");
		stubGetEnhetNavn(OK.value(), "norg2/hentEnhet_happy.json");
		stubGetEnhetKontaktInfo(OK.value(), "norg2/hentEnhetKontaktInfo_happy.json");

		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class);

		assertThat(actualResponse.getBrevdata()).isEqualTo(classpathToString("__files/treg001pdl/treg001_postboks_response.xml"));
	}

	@Test
	public void shouldMapAndGetKomplettBrevdataForPersonWithNullForkortetnavn() {
		postPdlGraphql(OK.value(), "pdl/bosattadresse_with_null_forkortetnavn.json");
		postPdlDigdir(OK.value(), "dkif/dkif-happy.json");
		stubGetEnhetNavn(OK.value(), "norg2/hentEnhet_happy.json");
		stubGetEnhetKontaktInfo(OK.value(), "norg2/hentEnhetKontaktInfo_happy.json");

		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class);

		assertThat(actualResponse.getBrevdata()).isEqualTo(classpathToString("__files/treg001pdl/treg001pdl_forkortetnavn_response.xml"));
	}

	@Test
	public void shouldMapAndGetKomplettBrevdataForPersonWithBosattadresseMedMatrikkeladresse() {
		postPdlGraphql(OK.value(), "pdl/bosattadressemedmatrikkeladresse.json");
		postPdlDigdir(OK.value(), "dkif/dkif-happy.json");
		stubGetEnhetNavn(OK.value(), "norg2/hentEnhet_happy.json");
		stubGetEnhetKontaktInfo(OK.value(), "norg2/hentEnhetKontaktInfo_happy.json");

		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class);

		assertThat(actualResponse.getBrevdata()).isEqualTo(classpathToString("__files/treg001pdl/treg001_response_matrikkeladresse.xml"));
	}

	@Test
	public void shouldGetKomplettBrevdataPersonMaalFormEN() {
		postPdlGraphql(OK.value(), "pdl/doedperson.json");
		postPdlDigdir(OK.value(), "dkif/dkif-happy.json");
		stubGetEnhetNavn(OK.value(), "norg2/hentEnhet_happy.json");
		stubGetEnhetKontaktInfo(OK.value(), "norg2/hentEnhetKontaktInfo_happy.json");

		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class);

		assertThat(actualResponse.getBrevdata()).isEqualTo(classpathToString("__files/treg001pdl/treg001_response_maalform_en.xml"));
	}

	@Test
	public void shouldGetKomplettBrevdataPersonMaalFormIkkeSkandinavisk() {
		postPdlGraphql(OK.value(), "pdl/bosattadressemedconavn.json");
		postPdlDigdir(OK.value(), "dkif/dkif-happy.json");
		stubGetEnhetNavn(OK.value(), "norg2/hentEnhet_happy.json");
		stubGetEnhetKontaktInfo(OK.value(), "norg2/hentEnhetKontaktInfo_happy.json");

		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class);

		assertThat(actualResponse.getBrevdata()).isEqualTo(classpathToString("__files/treg001pdl/treg001_response_maalform_sv.xml"));
	}

	@Test
	public void shouldGetKomplettBrevdataPersonMaalFormDansk() {
		postPdlGraphql(OK.value(), "pdl/kontaktadresse.json");
		postPdlDigdir(OK.value(), "dkif/dkif-happy.json");
		stubGetEnhetNavn(OK.value(), "norg2/hentEnhet_happy.json");
		stubGetEnhetKontaktInfo(OK.value(), "norg2/hentEnhetKontaktInfo_happy.json");

		//mottakerPlugin
		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class);

		assertThat(actualResponse.getBrevdata()).isEqualTo(classpathToString("__files/treg001pdl/treg001_kontaktadresse_response.xml"));
	}

	/**
	 * Kompletterer fullt brevdatasett der mottaker er organisasjon
	 */
	@Test
	public void shouldGetKomplettBrevdataOrg() {
		stubGetEnhetNavn(OK.value(), "");
		postPdlDigdir(OK.value(), "dkif/dkif-happy.json");
		stubGetEnhetNavn(OK.value(), "norg2/hentEnhet_happy.json");
		stubGetEnhetKontaktInfo(OK.value(), "norg2/hentEnhetKontaktInfo_happy.json");
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
		stubFor(get("/v1/organisasjon/" + "111111111")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("treg001/ereg/ereg-happy_ikke_skandinavisk.json")));
		stubGetEnhetNavn(OK.value(), "norg2/hentEnhet_happy.json");
		stubGetEnhetKontaktInfo(OK.value(), "norg2/hentEnhetKontaktInfo_happy.json");

		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request_orgv4.xml"), KompletterBrevdataResponse.class);

		assertThat(actualResponse.getBrevdata()).isEqualTo(classpathToString("__files/treg001pdl/treg001_full_response_orgv4_en.xml"));
	}

	@Test
	public void shouldNotMapBehandlendeEnhetWhenBerikIsFalse() {
		postPdlGraphql(OK.value(), "pdl/bosattadressemedconavn.json");
		postPdlDigdir(OK.value(), "dkif/dkif-happy.json");
		stubGetEnhetNavn(OK.value(), "norg2/hentEnhet_happy.json");
		stubGetEnhetKontaktInfo(OK.value(), "norg2/hentEnhetKontaktInfo_happy.json");

		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request_behandlende_enhet_8020.xml"), KompletterBrevdataResponse.class);

		assertThat(actualResponse.getBrevdata()).isEqualTo(classpathToString("__files/treg001pdl/treg001_full_response_behandlendeEnhet_8020.xml"));
	}

	@Test
	public void shouldGetKomplettBrevdataWhenDoedPerson() {
		postPdlGraphql(OK.value(), "pdl/doedperson.json");
		postPdlDigdir(OK.value(), "dkif/dkif-happy.json");
		stubGetEnhetNavn(OK.value(), "norg2/hentEnhet_happy.json");
		stubGetEnhetKontaktInfo(OK.value(), "norg2/hentEnhetKontaktInfo_happy.json");

		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class);

		assertThat(actualResponse.getBrevdata()).isEqualTo(classpathToString("__files/treg001pdl/treg001_full_response_dodperson.xml"));
	}

	@Test
	public void shouldGetKomplettBrevdataWhenDkifPersonIkkeFunnet() {
		postPdlGraphql(OK.value(), "pdl/BosattVegadresse.json");
		postPdlDigdir(OK.value(), "dkif/ikke-funnet.json");
		stubGetEnhetNavn(OK.value(), "norg2/hentEnhet_happy.json");
		stubGetEnhetKontaktInfo(OK.value(), "norg2/hentEnhetKontaktInfo_happy.json");

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
		stubGetEnhetNavn(OK.value(), "norg2/hentEnhet_happy.json");
		stubGetEnhetKontaktInfo(OK.value(), "norg2/hentEnhetKontaktInfo_happy.json");

		HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_request_orgv4.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");

		verify(getRequestedFor(urlEqualTo("/v1/organisasjon/111111111")));
		assertThat(e.getStatusCode()).isEqualTo(NOT_FOUND);
		assertThat(e.getResponseBodyAsString()).contains("Fant ikke Organisasjon med organisasjonsnummer=111111111");
	}

	@Test
	public void shouldReturnNotFoundIfPersonIsMissingAdresseInPdl() {
		postPdlDigdir(OK.value(), "dkif/dkif-happy.json");
		postPdlGraphql(OK.value(), "pdl/ukjentbosted.json");
		stubGetEnhetNavn(OK.value(), "norg2/hentEnhet_happy.json");
		stubGetEnhetKontaktInfo(OK.value(), "norg2/hentEnhetKontaktInfo_happy.json");

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class),
				"Should throw techical Exception");

		verify(postRequestedFor(urlEqualTo("/graphql")));
		assertThat(e.getStatusCode()).isEqualTo(NOT_FOUND);
		assertThat(e.getResponseBodyAsString()).contains("Fant ikke bostedsadresse for personen i PDL");
		assertThat(e.getResponseHeaders().get(NAV_REASON_CODE).get(0)).isEqualTo(ERROR_REASON_CODE);
	}

	@Test
	public void shouldReturnBadRequestWhenPersonHasUkjentAdresse() {
		postPdlGraphql(OK.value(), "pdl/kontaktinformasjonfordoedsbo.json"); //mottakerPlugin
		postPdlDigdir(BAD_REQUEST.value(), "dkif/dkif-happy.json");
		stubGetEnhetNavn(OK.value(), "norg2/hentEnhet_happy.json");
		stubGetEnhetKontaktInfo(OK.value(), "norg2/hentEnhetKontaktInfo_happy.json");

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");

		assertThat(e.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(e.getResponseBodyAsString()).contains("Funksjonell feil: dokumenttypeId=123 feilmelding=Funksjonell feil ved kall mot Digdir KRR. Feilmelding=400 Bad Request");
	}

	@Test
	public void shouldReturnBadRequestWhenPDLFailsSecurityErrorNoAccess() {
		postPdlGraphql(OK.value(), "pdl/unauthenticated-error-response.json");
		stubGetEnhetNavn(OK.value(), "norg2/hentEnhet_happy.json");
		stubGetEnhetKontaktInfo(OK.value(), "norg2/hentEnhetKontaktInfo_happy.json");

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");

		assertThat(e.getStatusCode()).isEqualTo(BAD_REQUEST);
	}

	@Test
	public void shouldReturnNotFoundIfFunctionalExceptionFromPersonPlugin() {
		postPdlDigdir(OK.value(), "dkif/dkif-happy.json");
		postPdlGraphql(OK.value(), "pdl/bosattutenpostdresse.json");
		stubGetEnhetNavn(OK.value(), "norg2/hentEnhet_happy.json");
		stubGetEnhetKontaktInfo(OK.value(), "norg2/hentEnhetKontaktInfo_happy.json");

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");

		verify(postRequestedFor(urlEqualTo("/graphql")));
		assertThat(e.getStatusCode()).isEqualTo(NOT_FOUND);
	}

	@Test
	public void shouldReturnNotFoundIfFunctionalExceptionFromNorgPlugins() {
		postPdlDigdir(OK.value(), "dkif/dkif-happy.json");
		stubGetEnhetNavn(NOT_FOUND.value(), "norg2/hentEnhet_happy.json");
		stubGetEnhetKontaktInfo(NOT_FOUND.value(), "norg2/hentEnhetKontaktInfo_happy.json");

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_norg2_request.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");

		verify(getRequestedFor(urlEqualTo("/norg2/enhet/0136")));
		assertThat(e.getStatusCode()).isEqualTo(NOT_FOUND);
	}

	@Test
	public void shouldReturnNotFoundIfNotFoundFromPDL() {
		postPdlDigdir(OK.value(), "dkif/dkif-happy.json");
		postPdlGraphqlWithErrorResponse(NOT_FOUND.value());
		stubGetEnhetNavn(OK.value(), "norg2/hentEnhet_happy.json");
		stubGetEnhetKontaktInfo(OK.value(), "norg2/hentEnhetKontaktInfo_happy.json");

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");

		assertThat(e.getStatusCode()).isEqualTo(NOT_FOUND);
	}

	@Test
	public void shouldReturnInternalServerErrorIfInternalServerErrorFromOrgPlugin() {
		postPdlGraphql(OK.value(), "pdl/bosattutenpostdresse.json");
		postPdlDigdir(OK.value(), "dkif/dkif-happy.json");
		stubFor(get("/v1/organisasjon/111111111").willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.value())));
		stubGetEnhetNavn(OK.value(), "norg2/hentEnhet_happy.json");
		stubGetEnhetKontaktInfo(OK.value(), "norg2/hentEnhetKontaktInfo_happy.json");

		HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_request_orgv4.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");

		verify(new CountMatchingStrategy(GREATER_THAN_OR_EQUAL, 5), getRequestedFor(urlEqualTo("/v1/organisasjon/111111111")));
		assertThat(e.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
		assertThat(e.getResponseBodyAsString()).contains("Teknisk feil mot hentOrganisasjon for organisasjon med organisasjonsnummer=111111111");
	}

	@Test
	public void shouldReturnInternalServerErrorIfNotFoundFromNorgPlugin() {
		stubGetEnhetNavn(INTERNAL_SERVER_ERROR.value(), "norg2/hentEnhet_happy.json");
		stubGetEnhetKontaktInfo(OK.value(), "norg2/hentEnhetKontaktInfo_happy.json");

		HttpServerErrorException e = assertThrows(HttpServerErrorException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_norg2_request.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");

		verify(new CountMatchingStrategy(GREATER_THAN_OR_EQUAL, 5), getRequestedFor(urlEqualTo("/norg2/enhet/0136")));
		assertThat(e.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
	}

	@Test
	public void shouldReturnInternalServerErrorWhenNotFoundFromDokmet() {
		postPdlGraphql(OK.value(), "pdl/BosattVegadresse.json");
		postPdlDigdir(OK.value(), "dkif/dkif-happy.json");
		stubFor(get(urlPathMatching("/DOKUMENTTYPEINFO_V4(.*)")).willReturn(aResponse().withStatus(NOT_FOUND.value())));
		stubGetEnhetNavn(OK.value(), "norg2/hentEnhet_happy.json");
		stubGetEnhetKontaktInfo(OK.value(), "norg2/hentEnhetKontaktInfo_happy.json");

		HttpServerErrorException e = assertThrows(HttpServerErrorException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");

		verify(getRequestedFor(urlEqualTo("/DOKUMENTTYPEINFO_V4/123")));
		assertThat(e.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
	}

	@Test
	public void shouldReturnInternalServerErrorIfTechnicalExceptionFromDokmet() {
		postPdlGraphql(OK.value(), "pdl/BosattVegadresse.json");
		postPdlDigdir(OK.value(), "dkif/dkif-happy.json");
		stubFor(get(urlPathMatching("/DOKUMENTTYPEINFO_V4(.*)")).willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.value())));
		stubGetEnhetNavn(OK.value(), "norg2/hentEnhet_happy.json");
		stubGetEnhetKontaktInfo(OK.value(), "norg2/hentEnhetKontaktInfo_happy.json");

		HttpServerErrorException e = assertThrows(HttpServerErrorException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001pdl/treg001_full_request.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");

		verify(new CountMatchingStrategy(GREATER_THAN_OR_EQUAL, 3), getRequestedFor(urlEqualTo("/DOKUMENTTYPEINFO_V4/123")));
		assertThat(e.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
	}

	@Test
	public void shouldReturnGoneIfPersonErDoedOgUtenKontaktAdresse() {
		postPdlGraphql(OK.value(), "pdl/doedpersonutenadresse.json");
		stubGetEnhetNavn(OK.value(), "norg2/hentEnhet_happy.json");
		stubGetEnhetKontaktInfo(OK.value(), "norg2/hentEnhetKontaktInfo_happy.json");

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

	protected void stubDokmetResponse() {
		stubFor(get(urlPathMatching("/DOKUMENTTYPEINFO_V4(.*)")).willReturn(aResponse()
				.withStatus(OK.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("treg001/dokmet/dokmet_happy-response.json"))); // Brukes til hentDokumenttypeinfo for Spraak
	}
}
