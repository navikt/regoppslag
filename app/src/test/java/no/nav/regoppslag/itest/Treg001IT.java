package no.nav.regoppslag.itest;

import com.github.tomakehurst.wiremock.client.CountMatchingStrategy;
import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.regoppslag.api.KompletterBrevdataRequest;
import no.nav.regoppslag.api.KompletterBrevdataResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import static no.nav.regoppslag.util.TestUtil.classpathToString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 * @author Ketill Fenne, Visma Consulting
 */

public class Treg001IT extends AbstractIT {

	private static final String DOKUMENTTYPEID = "123";

	@BeforeEach
	public void runBefore() {
		WireMock.removeAllMappings();
		WireMock.resetAllRequests();
		WireMock.reset();

		stubFor(get(urlPathMatching("/DOKUMENTTYPEINFO_V3(.*)"))
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

		stubFor(post("/VIRKSOMHET_PERSONV3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/personV3/hentperson-happypath-responsebody.xml"))); //mottakerPlugin

		stubFor(post("/ORGANISASJON_V4")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/organisasjonv4/organisasjonv4-happy.xml"))); //mottakerPlugin

	}

	/**
	 * Kompletterer fullt brevdatasett der mottaker er person
	 */
	@Test
	public void shouldGetKomplettBrevdataPerson() {
		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class);
		assertEquals(classpathToString("__files/treg001/treg001_full_response.xml").replaceAll("[\n\t\r ]", ""), actualResponse.getBrevdata()
				.replaceAll("[\n\t\r ]", ""));
	}

	@Test
	public void shouldGetKomplettBrevdataPersonMaalFormEN() {
		stubFor(post("/VIRKSOMHET_PERSONV3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/personV3/hentperson-happypath-responsebody_maalform_en.xml"))); //mottakerPlugin
		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class);
		assertEquals(classpathToString("__files/treg001/treg001_response_maalform_en.xml").replaceAll("[\n\t\r ]", ""), actualResponse
				.getBrevdata()
				.replaceAll("[\n\t\r ]", ""));
	}

	@Test
	public void shouldGetKomplettBrevdataPersonMaalFormIkkeSkandinavisk() {
		stubFor(post("/VIRKSOMHET_PERSONV3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/personV3/hentperson-happypath-responsebody_maalform_ikke_skandinavisk.xml"))); //mottakerPlugin
		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class);
		assertEquals(classpathToString("__files/treg001/treg001_response_maalform_en.xml").replaceAll("[\n\t\r ]", ""), actualResponse
				.getBrevdata()
				.replaceAll("[\n\t\r ]", ""));
	}

	@Test
	public void shouldGetKomplettBrevdataPersonMaalFormDansk() {
		stubFor(post("/VIRKSOMHET_PERSONV3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/personV3/hentperson-happypath-responsebody_maalform_dansk.xml"))); //mottakerPlugin
		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class);
		assertEquals(classpathToString("__files/treg001/treg001_full_response.xml").replaceAll("[\n\t\r ]", ""), actualResponse
				.getBrevdata()
				.replaceAll("[\n\t\r ]", ""));
	}

	/**
	 * Komplertterer fullt brevdatasett der mottaker er organisasjon
	 */
	@Test
	public void shouldGetKomplettBrevdataOrg() {
		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request_orgv4.xml"), KompletterBrevdataResponse.class);
		assertEquals(classpathToString("__files/treg001/treg001_full_response_orgv4.xml").replaceAll("[\n\t\r ]", ""), actualResponse
				.getBrevdata()
				.replaceAll("[\n\t\r ]", ""));
	}

	@Test
	public void shouldGetKomplettBrevdataOrgIkkeSkandinavisk() {
		stubFor(post("/ORGANISASJON_V4")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/organisasjonv4/organisasjonv4-happy_ikke_skandinavisk.xml"))); //mottakerPlugin
		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request_orgv4.xml"), KompletterBrevdataResponse.class);

		assertEquals(classpathToString("__files/treg001/treg001_full_response_orgv4_en.xml").replaceAll("[\n\t\r ]", ""), actualResponse
				.getBrevdata()
				.replaceAll("[\n\t\r ]", ""));
	}

	@Test
	public void shouldGetKomplettBrevdataOrgDansk() {
		stubFor(post("/ORGANISASJON_V4")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/organisasjonv4/organisasjonv4-happy_dansk.xml"))); //mottakerPlugin
		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request_orgv4.xml"), KompletterBrevdataResponse.class);
		assertEquals(classpathToString("__files/treg001/treg001_full_response_orgv4.xml").replaceAll("[\n\t\r ]", ""), actualResponse
				.getBrevdata()
				.replaceAll("[\n\t\r ]", ""));
	}


	@Test
	public void shouldNotMapWhenIsBerikIsFalse() {
		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request_is_berik_false.xml"), KompletterBrevdataResponse.class);
		assertEquals(classpathToString("__files/treg001/treg001_is_berik_false_response.xml").replaceAll("[\n\t\r ]", ""), actualResponse
				.getBrevdata()
				.replaceAll("[\n\t\r ]", ""));
	}

	@Test
	public void shouldNotMapBehandlendeEnhetWhenBerikIsFalse() {
		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request_behandlende_enhet_8020.xml"), KompletterBrevdataResponse.class);
		assertEquals(classpathToString("__files/treg001/treg001_full_response_behandlendeEnhet_8020.xml").replaceAll("[\n\t\r ]", ""), actualResponse
				.getBrevdata()
				.replaceAll("[\n\t\r ]", ""));
	}

	@Test
	public void shouldGetKomplettBrevdataWhenDodPerson() {
		stubFor(post("/VIRKSOMHET_PERSONV3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/personV3/hentperson-happypath-responsebody-dodperson.xml")));

		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class);
		assertEquals(classpathToString("__files/treg001/treg001_full_response_dodperson.xml").replaceAll("[\n\t\r ]", ""), actualResponse
				.getBrevdata()
				.replaceAll("[\n\t\r ]", ""));
	}

	/**
	 * Testbetingelser:
	 * -HVIS det oppstår en funksjonell feil for   et brevdataelement i en berikerplugin SÅ oppdater feillogg funksjonelle feil   OG fortsett til neste brevdataelement
	 * - HVIS det er opprettet en feillogg funksjonelle feil SÅ SKAL loggen returneres
	 */
	@Test
	public void shouldThrowFunctionalExceptionFromOrgPlugin() {
		//Stub web services:
		stubFor(post("/ORGANISASJON_V4")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/organisasjonv4/organisasjonv4_orgIkkeFunnet.xml"))); //mottakerPlugin

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () ->
				restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_request_orgv4.xml"), KompletterBrevdataResponse.class));

		verify(postRequestedFor(urlEqualTo("/ORGANISASJON_V4")));
		assertEquals(NOT_FOUND, e.getStatusCode());

	}

	@Test
	public void shouldThrowTechnicalIfPersonIsMissingAdresse() {

		stubFor(post("/VIRKSOMHET_PERSONV3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/personV3/hentperson-mangler_adresse.xml"))); //mottakerPlugin
		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () ->
				restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class));

		verify(postRequestedFor(urlEqualTo("/VIRKSOMHET_PERSONV3")));
		assertEquals(NOT_FOUND, e.getStatusCode());

	}

	@Test
	public void shouldThrowWhenPersonV3FailsFunctionalInvalidSecurityToken() {

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () ->
				restTemplateNoHeader.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class));
		assertEquals(NOT_FOUND, e.getStatusCode());
	}

	@Test
	public void shouldThrowWhenPersonHasUkjentAdresse() {

		stubFor(post("/VIRKSOMHET_PERSONV3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/personV3/hentperson-gjeldende_adresse_ukjent.xml"))); //mottakerPlugin

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class));
		assertEquals(NOT_FOUND, e.getStatusCode());
	}

	@Test
	public void shouldThrowWhenPersonV3FailsSecurityErrorNoAccess() {
		stubFor(post("/VIRKSOMHET_PERSONV3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/personV3/hentPerson-FunksjonellFeil-SikkerhetsBegrensning-responsebody.xml")));

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class));
		assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
	}

	@Test
	public void shouldThrowFunctionalExceptionFromPersonPlugin() {
		//Stub web services:
		stubFor(post("/VIRKSOMHET_PERSONV3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/personV3/hentPerson-FunksjonellFeil-PersonIkkeFunnet-responsebody.xml")));
		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class));

		verify(postRequestedFor(urlEqualTo("/VIRKSOMHET_PERSONV3")));
		assertEquals(NOT_FOUND, e.getStatusCode());
	}

	@Test
	public void shouldThrowFunctionalExceptionFromNorgPlugins() {
		//Stub web services:
		stubFor(post("/ORGANISASJONENHETKONTAKTINFORMASJON_V1")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/norg/hentEnhet-FunksjonellFeil-EnhetIkkeFunnet.xml"))); //mottakerPlugin
		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () ->
				restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class));
		verify(postRequestedFor(urlEqualTo("/ORGANISASJONENHETKONTAKTINFORMASJON_V1")));
		assertEquals(NOT_FOUND, e.getStatusCode());
	}

	@Test
	public void shouldThrowTechnicalExceptionFromPersonPlugin() {
		//Stub web services:
		stubFor(post("/VIRKSOMHET_PERSONV3")
				.willReturn(aResponse().withStatus(NOT_FOUND.value())));

		HttpServerErrorException e = assertThrows(HttpServerErrorException.class, () ->
				restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class));

		verify(new CountMatchingStrategy(CountMatchingStrategy.GREATER_THAN_OR_EQUAL, 5), postRequestedFor(urlEqualTo("/VIRKSOMHET_PERSONV3")));
		assertEquals(INTERNAL_SERVER_ERROR, e.getStatusCode());

	}

	@Test
	public void shouldThrowTechnicalExceptionFromOrgPlugin() {
		//Stub web services:
		stubFor(post("/ORGANISASJON_V4")
				.willReturn(aResponse().withStatus(NOT_FOUND.value())));
		HttpServerErrorException e = assertThrows(HttpServerErrorException.class, () ->
				restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_request_orgv4.xml"), KompletterBrevdataResponse.class));

		verify(new CountMatchingStrategy(CountMatchingStrategy.GREATER_THAN_OR_EQUAL, 5), postRequestedFor(urlEqualTo("/ORGANISASJON_V4")));
		assertEquals(INTERNAL_SERVER_ERROR, e.getStatusCode());
	}

	@Test
	public void shouldThrowTechnicalExceptionFromNorgPlugin() {
		//Stub web services:
		stubFor(post("/ORGANISASJONENHETKONTAKTINFORMASJON_V1")
				.willReturn(notFound().withStatus(NOT_FOUND.value())));

		HttpServerErrorException e = assertThrows(HttpServerErrorException.class, () ->
				restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_norg2_request.xml"), KompletterBrevdataResponse.class));

		verify(new CountMatchingStrategy(CountMatchingStrategy.GREATER_THAN_OR_EQUAL, 5), postRequestedFor(urlEqualTo("/ORGANISASJONENHETKONTAKTINFORMASJON_V1")));
		assertEquals(INTERNAL_SERVER_ERROR, e.getStatusCode());
	}

	@Test
	public void shouldThrowTechnicalExceptionWhenReceivedNotFoundFromDokkat() {
		//Stub web services:
		stubFor(get(urlPathMatching("/DOKUMENTTYPEINFO_V3(.*)"))
				.willReturn(aResponse().withStatus(NOT_FOUND.value())
						.withHeader("Content-Type", "application/json")
						.withBodyFile("treg001/dokkat/dokkat_happy-response.json")));
		HttpServerErrorException e = assertThrows(HttpServerErrorException.class, () ->
				restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class));

		verify(getRequestedFor(urlEqualTo("/DOKUMENTTYPEINFO_V3/123")));
		assertEquals(INTERNAL_SERVER_ERROR, e.getStatusCode());
	}

	@Test
	public void shouldThrowTechnicalExceptionFromDokkat() {
		//Stub web services:
		stubFor(get(urlPathMatching("/DOKUMENTTYPEINFO_V3(.*)"))
				.willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.value())
						.withHeader("Content-Type", "application/json")
						.withBodyFile("treg001/dokkat/dokkat_happy-response.json")));

		HttpServerErrorException e = assertThrows(HttpServerErrorException.class, () ->
				restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class));
		verify(new CountMatchingStrategy(CountMatchingStrategy.GREATER_THAN_OR_EQUAL, 5), getRequestedFor(urlEqualTo("/DOKUMENTTYPEINFO_V3/123")));
		assertEquals(INTERNAL_SERVER_ERROR, e.getStatusCode());
	}

	@Test
	public void shouldThrowFunctionalExceptionWhenPersonDodAndNoAdress() {
		//Stub web services:
		stubFor(post("/VIRKSOMHET_PERSONV3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/personV3/hentperson-dod_mangler_adresse.xml")));

				restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH,
						createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class);
		verify(postRequestedFor(urlEqualTo("/VIRKSOMHET_PERSONV3")));
	}

	private KompletterBrevdataRequest createRequest(String path) {
		return KompletterBrevdataRequest.builder()
				.dokumentTypeId(DOKUMENTTYPEID)
				.brevdata(classpathToString(path))
				.build();
	}
}