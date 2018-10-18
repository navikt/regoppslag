package no.nav.regoppslag.itest;

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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import com.github.tomakehurst.wiremock.client.CountMatchingStrategy;
import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.regoppslag.api.KompletterBrevdataRequest;
import no.nav.regoppslag.api.KompletterBrevdataResponse;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 * @author Ketill Fenne, Visma Consulting
 */

public class Treg001IT extends AbstractIT {

	private final String DOKUMENTTYPEID = "123";

	@Before
	public void runBefore() {
		WireMock.removeAllMappings();
		WireMock.resetAllRequests();
		WireMock.reset();

		stubFor(get(urlPathMatching("/DOKUMENTTYPEINFO_V3(.*)"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader("Content-Type", "application/json")
						.withBodyFile("treg001/dokkat/dokkat_happy-response.json"))); //Brukes til hentDokumenttypeinfo for Spraak

		//Stub web services:
		stubFor(post("/VIRKSOMHET_ORGANISASJONENHETKONTAKTINFORMASJON_V1")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/norg/happy-response.xml")));

		stubFor(post("/STS")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("felles/sts/sts_signature-responsebody.xml"))); //mottakerPlugin

		stubFor(post("/VIRKSOMHET_PERSON_V3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/personV3/hentperson-happypath-responsebody.xml"))); //mottakerPlugin

		stubFor(post("/VIRKSOMHET_ORGANISASJON_V4")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/organisasjonv4/organisasjonv4-happy.xml"))); //mottakerPlugin

	}

	/**
	 * Kompletterer fullt brevdatasett der mottaker er person
	 */
	@Test
	public void shouldGetKomplettBrevdataPerson() throws Exception {
		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class);
		assertEquals(classpathToString("__files/treg001/treg001_full_response.xml").replaceAll("[\n\t\r ]", ""), actualResponse.getBrevdata()
				.replaceAll("[\n\t\r ]", ""));
	}

	@Test
	public void shouldGetKomplettBrevdataPersonMaalFormEN() throws Exception {
		stubFor(post("/VIRKSOMHET_PERSON_V3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/personV3/hentperson-happypath-responsebody_maalform_en.xml"))); //mottakerPlugin
		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class);
		assertEquals(classpathToString("__files/treg001/treg001_response_maalform_en.xml").replaceAll("[\n\t\r ]", ""), actualResponse
				.getBrevdata()
				.replaceAll("[\n\t\r ]", ""));
	}

	/**
	 * Komplertterer fullt brevdatasett der mottaker er organisasjon
	 */
	@Test
	public void shouldGetKomplettBrevdataOrg() throws Exception {
		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request_orgv4.xml"), KompletterBrevdataResponse.class);
		assertEquals(classpathToString("__files/treg001/treg001_full_response_orgv4.xml").replaceAll("[\n\t\r ]", ""), actualResponse
				.getBrevdata()
				.replaceAll("[\n\t\r ]", ""));
	}

	@Test
	public void shouldNotMapWhenIsBerikIsFalse() throws Exception {
		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request_is_berik_false.xml"), KompletterBrevdataResponse.class);
		assertEquals(classpathToString("__files/treg001/treg001_is_berik_false_response.xml").replaceAll("[\n\t\r ]", ""), actualResponse
				.getBrevdata()
				.replaceAll("[\n\t\r ]", ""));
	}

	@Test
	public void shouldNotMapBehandlendeEnhetWhenBerikIsFalse() throws Exception {
		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request_behandlende_enhet_8020.xml"), KompletterBrevdataResponse.class);
		assertEquals(classpathToString("__files/treg001/treg001_full_response_behandlendeEnhet_8020.xml").replaceAll("[\n\t\r ]", ""), actualResponse
				.getBrevdata()
				.replaceAll("[\n\t\r ]", ""));
	}

	/**
	 * Testbetingelser:
	 * -HVIS det oppstår en funksjonell feil for   et brevdataelement i en berikerplugin SÅ oppdater feillogg funksjonelle feil   OG fortsett til neste brevdataelement
	 * - HVIS det er opprettet en feillogg funksjonelle feil SÅ SKAL loggen returneres
	 */
	@Test
	public void shouldThrowFunctionalExceptionFromOrgPlugin() throws Exception {
		//Stub web services:
		stubFor(post("/VIRKSOMHET_ORGANISASJON_V4")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/organisasjonv4/organisasjonv4_orgIkkeFunnet.xml"))); //mottakerPlugin

		try {
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_request_orgv4.xml"), KompletterBrevdataResponse.class);
			fail("Test did not throw exception");
		} catch (HttpStatusCodeException e) {
			verify(postRequestedFor(urlEqualTo("/VIRKSOMHET_ORGANISASJON_V4")));
			assertEquals(e.getStatusCode(), HttpStatus.BAD_REQUEST);
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Ingen organisasjon ble funnet med orgnr: 111111111"));
		}
	}

	@Test
	public void shouldThrowTechnicalIfPersonIsMissingAdresse() throws Exception {

		stubFor(post("/VIRKSOMHET_PERSON_V3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/personV3/hentperson-mangler_adresse.xml"))); //mottakerPlugin
		try {
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class);
			fail("Should throw techical Exception");
		} catch (HttpStatusCodeException e) {
			verify(postRequestedFor(urlEqualTo("/VIRKSOMHET_PERSON_V3")));
			assertEquals(e.getStatusCode(), HttpStatus.BAD_REQUEST);
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Ugyldig postadresse. Adresse mangler adresselinje1, postnummer, poststed og land."));
		}
	}

	@Test
	public void shouldThrowWhenPersonV3FailsFunctionalInvalidSecurityToken() throws Exception {

		try {
			restTemplateNoHeader.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class);
			fail("Test did not throw exception");
		} catch (HttpClientErrorException e) {
			assertEquals(e.getStatusCode(), HttpStatus.BAD_REQUEST);
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Fant ingen SAML assertion token i sikkerhetskontekst. SAML assertion token kreves for å kunne kalle PersonV3"));
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("RegOppslagFunctionalException"));
		}

	}

	@Test
	public void shouldThrowWhenPersonHasUkjentAdresse() throws Exception {

		stubFor(post("/VIRKSOMHET_PERSON_V3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/personV3/hentperson-gjeldende_adresse_ukjent.xml"))); //mottakerPlugin

		try {
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class);
			fail("Test did not throw exception");
		} catch (HttpClientErrorException e) {
			assertEquals(e.getStatusCode(), HttpStatus.BAD_REQUEST);
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Kunne ikke mappe postadresse for mottaker fordi gjeldendePostadressetype=UKJENT_ADRESSE"));
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("RegOppslagFunctionalException"));
		}

	}

	@Test
	public void shouldThrowWhenPersonV3FailsSecurityErrorNoAccess() throws Exception {
		stubFor(post("/VIRKSOMHET_PERSON_V3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/personV3/hentPerson-FunksjonellFeil-SikkerhetsBegrensning-responsebody.xml")));

		try {
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class);
			fail("Test did not throw exception");
		} catch (HttpStatusCodeException e) {
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("PersonV3.hentPerson feiler på grunn av sikkerhetsbegresning. Message=Ingen tilgang"));
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("RegOppslagSecurityException"));
			assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
		}

	}

	@Test
	public void shouldThrowFunctionalExceptionFromPersonPlugin() throws Exception {
		//Stub web services:
		stubFor(post("/VIRKSOMHET_PERSON_V3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/personV3/hentPerson-FunksjonellFeil-PersonIkkeFunnet-responsebody.xml")));
		try {
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class);
			fail("Test did not throw exception");
		} catch (HttpStatusCodeException e) {
			verify(postRequestedFor(urlEqualTo("/VIRKSOMHET_PERSON_V3")));
			assertEquals(e.getStatusCode(), HttpStatus.BAD_REQUEST);
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("PersonV3.hentPerson fant ikke person med ident=20096828390, message=Ingen forekomster funnet"));
		}
	}

	@Test
	public void shouldThrowFunctionalExceptionFromNorgPlugins() throws Exception {
		//Stub web services:
		stubFor(post("/VIRKSOMHET_ORGANISASJONENHETKONTAKTINFORMASJON_V1")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/norg/hentEnhet-FunksjonellFeil-EnhetIkkeFunnet.xml"))); //mottakerPlugin
		try {
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class);
			fail("Test did not throw exception");
		} catch (HttpStatusCodeException e) {
			verify(postRequestedFor(urlEqualTo("/VIRKSOMHET_ORGANISASJONENHETKONTAKTINFORMASJON_V1")));
			assertEquals(e.getStatusCode(), HttpStatus.BAD_REQUEST);
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Nav enhet finnes ikke for enhetNr=0136"));
		}

	}

	@Test
	public void shouldThrowTechnicalExceptionFromPersonPlugin() throws Exception {
		//Stub web services:
		stubFor(post("/VIRKSOMHET_PERSON_V3")
				.willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));
		try {
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class);
			fail("Test did not throw exception");
		} catch (HttpStatusCodeException e) {
			verify(new CountMatchingStrategy(CountMatchingStrategy.GREATER_THAN_OR_EQUAL, 5), postRequestedFor(urlEqualTo("/VIRKSOMHET_PERSON_V3")));
			assertEquals(e.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Noe gikk galt i kall til PersonV3.hentPerson. Message=Could not send Message."));
		}
	}

	@Test
	public void shouldThrowTechnicalExceptionFromOrgPlugin() throws Exception {
		//Stub web services:
		stubFor(post("/VIRKSOMHET_ORGANISASJON_V4")
				.willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));
		try {
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_request_orgv4.xml"), KompletterBrevdataResponse.class);
			fail("Test did not throw exception");
		} catch (HttpStatusCodeException e) {
			verify(new CountMatchingStrategy(CountMatchingStrategy.GREATER_THAN_OR_EQUAL, 5), postRequestedFor(urlEqualTo("/VIRKSOMHET_ORGANISASJON_V4")));
			assertEquals(e.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Noe gikk galt i kall til OrganisasjonV4.hentOrganisasjon for enhetNr=111111111"));
		}
	}

	@Test
	public void shouldThrowTechnicalExceptionFromNorgPlugin() throws Exception {
		//Stub web services:
		stubFor(post("/VIRKSOMHET_ORGANISASJONENHETKONTAKTINFORMASJON_V1")
				.willReturn(notFound().withStatus(HttpStatus.NOT_FOUND.value())));
		try {
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_norg2_request.xml"), KompletterBrevdataResponse.class);
			fail("Test did not throw exception");
		} catch (HttpStatusCodeException e) {
			verify(new CountMatchingStrategy(CountMatchingStrategy.GREATER_THAN_OR_EQUAL, 5), postRequestedFor(urlEqualTo("/VIRKSOMHET_ORGANISASJONENHETKONTAKTINFORMASJON_V1")));
			assertEquals(e.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Noe gikk galt i kall til Norg for enhetNr=0136"));
		}
	}

	@Test
	public void shouldThrowTechnicalExceptionWhenReceivedNotFoundFromDokkat() throws Exception {
		//Stub web services:
		stubFor(get(urlPathMatching("/DOKUMENTTYPEINFO_V3(.*)"))
				.willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())
						.withHeader("Content-Type", "application/json")
						.withBodyFile("treg001/dokkat/dokkat_happy-response.json")));
		try {
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class);
			fail("Test did not throw exception");
		} catch (HttpServerErrorException e) {
			verify(getRequestedFor(urlEqualTo("/DOKUMENTTYPEINFO_V3/123")));
			assertEquals(e.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Dokkat.TKAT020 feilet med statusKode=404. Fant ingen dokumenttypeInfo med dokumenttypeId=123."));
		}
	}

	@Test
	public void shouldThrowTechnicalExceptionFromDokkat() throws Exception {
		//Stub web services:
		stubFor(get(urlPathMatching("/DOKUMENTTYPEINFO_V3(.*)"))
				.willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
						.withHeader("Content-Type", "application/json")
						.withBodyFile("treg001/dokkat/dokkat_happy-response.json")));
		try {
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, createRequest("__files/treg001/treg001_full_request.xml"), KompletterBrevdataResponse.class);
			fail("Test did not throw exception");
		} catch (HttpServerErrorException e) {
			verify(new CountMatchingStrategy(CountMatchingStrategy.GREATER_THAN_OR_EQUAL, 5), getRequestedFor(urlEqualTo("/DOKUMENTTYPEINFO_V3/123")));
			assertEquals(e.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Dokkat.TKAT020 feilet teknisk med statusKode=500 for dokumenttypeId=123"));
		}
	}

	private KompletterBrevdataRequest createRequest(String path) {
		return KompletterBrevdataRequest.builder()
				.dokumentTypeId(DOKUMENTTYPEID)
				.brevdata(classpathToString(path))
				.build();
	}
}