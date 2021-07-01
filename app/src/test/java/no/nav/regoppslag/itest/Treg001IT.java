package no.nav.regoppslag.itest;

import com.github.tomakehurst.wiremock.client.CountMatchingStrategy;
import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.regoppslag.api.KompletterBrevdataRequest;
import no.nav.regoppslag.api.KompletterBrevdataResponse;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
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
import static no.nav.regoppslag.util.PDLResponseUtil.postPdlGraphql;
import static no.nav.regoppslag.util.TestUtil.classpathToString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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


		stubFor(post("/ORGANISASJON_V4")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/organisasjonv4/organisasjonv4-happy.xml"))); //mottakerPlugin

	}

	/**
	 * Kompletterer fullt brevdatasett der mottaker er person
	 */
	@Test
	public void shouldGetKomplettBrevdataPerson() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/doedperson.json");
		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class);
		assertEquals(classpathToString("__files/treg001/treg001_full_response.xml").replaceAll("[\n\t\r ]", ""), actualResponse.getBrevdata()
				.replaceAll("[\n\t\r ]", ""));
	}

	@Test
	public void shouldGetKomplettBrevdataPersonMaalFormEN() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/doedperson.json");

		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class);
		assertEquals(classpathToString("__files/treg001/treg001_response_maalform_en.xml").replaceAll("[\n\t\r ]", ""), actualResponse
				.getBrevdata()
				.replaceAll("[\n\t\r ]", ""));
	}

	@Test
	public void shouldGetKomplettBrevdataPersonMaalFormIkkeSkandinavisk() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/doedperson.json");

		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class);
		assertEquals(classpathToString("__files/treg001/treg001_response_maalform_en.xml").replaceAll("[\n\t\r ]", ""), actualResponse
				.getBrevdata()
				.replaceAll("[\n\t\r ]", ""));
	}

	@Test
	public void shouldGetKomplettBrevdataPersonMaalFormDansk() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/doedperson.json");

		//mottakerPlugin
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
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/doedperson.json");

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
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/doedperson.json");

		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request_is_berik_false.xml"), KompletterBrevdataResponse.class);
		assertEquals(classpathToString("__files/treg001/treg001_is_berik_false_response.xml").replaceAll("[\n\t\r ]", ""), actualResponse
				.getBrevdata()
				.replaceAll("[\n\t\r ]", ""));
	}

	@Test
	public void shouldNotMapBehandlendeEnhetWhenBerikIsFalse() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/doedperson.json");

		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request_behandlende_enhet_8020.xml"), KompletterBrevdataResponse.class);
		assertEquals(classpathToString("__files/treg001/treg001_full_response_behandlendeEnhet_8020.xml").replaceAll("[\n\t\r ]", ""), actualResponse
				.getBrevdata()
				.replaceAll("[\n\t\r ]", ""));
	}

	@Test
	public void shouldGetKomplettBrevdataWhenDodPerson() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/doedperson.json");

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


		HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_request_orgv4.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");
		verify(postRequestedFor(urlEqualTo("/ORGANISASJON_V4")));
		assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
		assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Ingen organisasjon ble funnet med orgnr: 111111111"));

	}

	@Test
	public void shouldThrowTechnicalIfPersonIsMissingAdresse() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/doedperson.json");

		HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class),
				"Should throw techical Exception");

		verify(postRequestedFor(urlEqualTo("/VIRKSOMHET_PERSONV3")));
		assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
		assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Ugyldig postadresse. Adresse mangler adresselinje1, postnummer, poststed og land."));
	}

	@Test
	public void shouldThrowWhenPersonV3FailsFunctionalInvalidSecurityToken() {
		HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class, () ->
						restTemplateNoHeader.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");

		assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
		assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Fant ingen SAML assertion token i sikkerhetskontekst. SAML assertion token kreves for å kunne kalle PersonV3"));
	}

	@Test
	public void shouldThrowWhenPersonHasUkjentAdresse() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/doedperson.json"); //mottakerPlugin

		HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");
		assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
		assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Kunne ikke mappe postadresse for mottaker fordi gjeldendePostadressetype=UKJENT_ADRESSE"));
	}

	@Test
	public void shouldThrowWhenPersonV3FailsSecurityErrorNoAccess() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/doedperson.json");

		HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");
		assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("PersonV3.hentPerson feiler på grunn av sikkerhetsbegresning. Message=Ingen tilgang"));
		assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
	}

	@Test
	public void shouldThrowFunctionalExceptionFromPersonPlugin() {
		//Stub web services:
		stubFor(post("/VIRKSOMHET_PERSONV3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/personV3/hentPerson-FunksjonellFeil-PersonIkkeFunnet-responsebody.xml")));
		HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");

		verify(postRequestedFor(urlEqualTo("/VIRKSOMHET_PERSONV3")));
		assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
		assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("PersonV3.hentPerson fant ikke person med ident=20096828390, message=Ingen forekomster funnet"));
	}

	@Test
	public void shouldThrowFunctionalExceptionFromNorgPlugins() {
		//Stub web services:
		stubFor(post("/ORGANISASJONENHETKONTAKTINFORMASJON_V1")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/norg/hentEnhet-FunksjonellFeil-EnhetIkkeFunnet.xml"))); //mottakerPlugin
		HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");

		verify(postRequestedFor(urlEqualTo("/ORGANISASJONENHETKONTAKTINFORMASJON_V1")));
		assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
		assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Nav enhet finnes ikke for enhetNr=0136"));
	}

	@Test
	public void shouldThrowTechnicalExceptionFromPersonPlugin() {
		//Stub web services:
		stubFor(post("/VIRKSOMHET_PERSONV3")
				.willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));
		HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");

		verify(new CountMatchingStrategy(CountMatchingStrategy.GREATER_THAN_OR_EQUAL, 5), postRequestedFor(urlEqualTo("/VIRKSOMHET_PERSONV3")));
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatusCode());
		assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Noe gikk galt i kall til PersonV3.hentPerson. Message=Could not send Message."));
	}

	@Test
	public void shouldThrowTechnicalExceptionFromOrgPlugin() {
		//Stub web services:
		stubFor(post("/ORGANISASJON_V4")
				.willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));
		HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_request_orgv4.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");

		verify(new CountMatchingStrategy(CountMatchingStrategy.GREATER_THAN_OR_EQUAL, 5), postRequestedFor(urlEqualTo("/ORGANISASJON_V4")));
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatusCode());
		assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Noe gikk galt i kall til OrganisasjonV4.hentOrganisasjon for enhetNr=111111111"));
	}

	@Test
	public void shouldThrowTechnicalExceptionFromNorgPlugin() {
		//Stub web services:
		stubFor(post("/ORGANISASJONENHETKONTAKTINFORMASJON_V1")
				.willReturn(notFound().withStatus(HttpStatus.NOT_FOUND.value())));
		HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_norg2_request.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");
		verify(new CountMatchingStrategy(CountMatchingStrategy.GREATER_THAN_OR_EQUAL, 5), postRequestedFor(urlEqualTo("/ORGANISASJONENHETKONTAKTINFORMASJON_V1")));
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatusCode());
		assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Noe gikk galt i kall til Norg for enhetNr=0136"));
	}

	@Test
	public void shouldThrowTechnicalExceptionWhenReceivedNotFoundFromDokkat() {
		//Stub web services:
		stubFor(get(urlPathMatching("/DOKUMENTTYPEINFO_V3(.*)"))
				.willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())
						.withHeader("Content-Type", "application/json")
						.withBodyFile("treg001/dokkat/dokkat_happy-response.json")));
		HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");
		verify(getRequestedFor(urlEqualTo("/DOKUMENTTYPEINFO_V3/123")));
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatusCode());
		assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Dokkat.TKAT020 feilet med statusKode=404 NOT_FOUND. Fant ingen dokumenttypeInfo med dokumenttypeId=123."));
	}

	@Test
	public void shouldThrowTechnicalExceptionFromDokkat() {
		//Stub web services:
		stubFor(get(urlPathMatching("/DOKUMENTTYPEINFO_V3(.*)"))
				.willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
						.withHeader("Content-Type", "application/json")
						.withBodyFile("treg001/dokkat/dokkat_happy-response.json")));
		HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class),
				"Test did not throw exception");
		verify(new CountMatchingStrategy(CountMatchingStrategy.GREATER_THAN_OR_EQUAL, 5), getRequestedFor(urlEqualTo("/DOKUMENTTYPEINFO_V3/123")));
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatusCode());
		assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Dokkat.TKAT020 feilet teknisk med statusKode=500 INTERNAL_SERVER_ERROR for dokumenttypeId=123"));
	}

	@Test
	public void shouldThrowFunctionalExceptionWhenPersonDodAndNoAdress() {
		//Stub web services:
		stubFor(post("/VIRKSOMHET_PERSONV3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/personV3/hentperson-dod_mangler_adresse.xml")));
		HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class, () ->
						restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class),
				"Should throw techical Exception");
		verify(postRequestedFor(urlEqualTo("/VIRKSOMHET_PERSONV3")));
		assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
		assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Mottaker er registrert som død og har gjeldendePostadressetype=UKJENT_ADRESSE"));
	}

	private KompletterBrevdataRequest createRequest(String path) {
		return KompletterBrevdataRequest.builder()
				.dokumentTypeId(DOKUMENTTYPEID)
				.brevdata(classpathToString(path))
				.build();
	}
}