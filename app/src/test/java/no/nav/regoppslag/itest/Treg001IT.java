package no.nav.regoppslag.itest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.nav.regoppslag.consumer.dokkat.Tkat020DokumenttypeInfo.DOKKAT;
import static no.nav.regoppslag.consumer.dokkat.Tkat020DokumenttypeInfo.HENT_DOKKAT_SPRAAKINFO;
import static no.nav.regoppslag.consumer.norg2.OrganisasjonEnhetKontaktinformasjonV1Consumer.HENT_KONTAKTINFORMASJON_FOR_ENHET;
import static no.nav.regoppslag.consumer.organisasjonv4.OrganisasjonV4Consumer.HENT_ORGANISASJON;
import static no.nav.regoppslag.consumer.personv3.PersonV3Consumer.HENT_PERSON;
import static no.nav.regoppslag.metrics.PrometheusLabels.NORG2;
import static no.nav.regoppslag.metrics.PrometheusLabels.ORGANISASJONV4;
import static no.nav.regoppslag.metrics.PrometheusLabels.PERSONV3;
import static no.nav.regoppslag.metrics.PrometheusLabels.SERVICE_CODE_TREG001;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestLatency;
import static no.nav.regoppslag.rest.RegisteroppslagRestController.KOMPLETTER_BREVDATA_URI_PATH;
import static no.nav.regoppslag.rest.RegisteroppslagRestController.REST;
import static no.nav.regoppslag.util.TestUtil.classpathToString;
import static no.nav.regoppslag.util.TestUtil.resourceUrlToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.common.io.Resources;
import no.nav.regoppslag.common.KompletterBrevdataRequest;
import no.nav.regoppslag.common.KompletterBrevdataResponse;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import java.net.URL;
import java.util.Arrays;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 * @author Ketill Fenne, Visma Consulting
 */

public class Treg001IT extends AbstractIT {
	
	private final String DOKUMENTTYPEID = "123";
	
	private URL brevdataResponse_URL = Resources.getResource("__files/treg001/treg001_full_response.xml");
	private URL brevdataResponseOrg_URL = Resources.getResource("__files/treg001/treg001_full_response_orgv4.xml");
	private String expectedBrevdataFerdigUtfylt = resourceUrlToString(brevdataResponse_URL);
	private String expectedBrevdataFerdigUtfyltOrg = resourceUrlToString(brevdataResponseOrg_URL);
	
	private KompletterBrevdataRequest request = createRequest("__files/treg001/treg001_full_request.xml");
	private KompletterBrevdataRequest requestOrgFull = createRequest("__files/treg001/treg001_full_request_orgv4.xml");
	private KompletterBrevdataRequest requestOrg = createRequest("__files/treg001/treg001_request_orgv4.xml");
	private KompletterBrevdataRequest requestNorg = createRequest("__files/treg001/treg001_norg2_request.xml");
	
	
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
		
		request = createRequest("__files/treg001/treg001_full_request.xml");
		requestNorg = createRequest("__files/treg001/treg001_norg2_request.xml");
		requestOrg = createRequest("__files/treg001/treg001_request_orgv4.xml");
		requestOrgFull = createRequest("__files/treg001/treg001_full_request_orgv4.xml");
		
		requestLatency.clear();
	}
	
	/**
	 * Komplertterer fullt brevdatasett der mottaker er person
	 */
	@Test
	public void shouldGetKomplettBrevdataPerson() throws Exception {
		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, request, KompletterBrevdataResponse.class);
		assertEquals(expectedBrevdataFerdigUtfylt.replaceAll("[\n\t\r ]", ""), actualResponse.getBrevdata()
				.replaceAll("[\n\t\r ]", ""));
	}
	
	/**
	 * Komplertterer fullt brevdatasett der mottaker er organisasjon
	 */
	@Test
	public void shouldGetKomplettBrevdataOrg() throws Exception {
		KompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, requestOrgFull, KompletterBrevdataResponse.class);
		assertEquals(expectedBrevdataFerdigUtfyltOrg.replaceAll("[\n\t\r ]", ""), actualResponse.getBrevdata()
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
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, requestOrg, KompletterBrevdataResponse.class);
			fail("Test did not throw exception");
		} catch (HttpStatusCodeException e) {
			assertThat(((Double) requestLatency.labels(SERVICE_CODE_TREG001, ORGANISASJONV4, HENT_ORGANISASJON)
					.get().buckets[14]).intValue(), is(Matchers.equalTo(1)));
			assertEquals(e.getStatusCode(), HttpStatus.BAD_REQUEST);
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Ingen organisasjon ble funnet med orgnr: 111111111"));
		}
	}

	@Test
	public void shouldThrowIfPersonIsMissingAdresse() throws Exception {

		stubFor(post("/VIRKSOMHET_PERSON_V3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/personV3/hentperson-mangler_adresse.xml"))); //mottakerPlugin
		try {
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, request, KompletterBrevdataResponse.class);
			fail("Should throw functional Exception");
		}catch (HttpStatusCodeException e) {
			verify(1,postRequestedFor(urlEqualTo("/VIRKSOMHET_PERSON_V3")));
			assertEquals(e.getStatusCode(), HttpStatus.BAD_REQUEST);
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Ugyldig postadresse. Adressen mangler Land, Adresselinje1, Postnummer og Poststed"));
		}
	}
	
	@Test
	public void shouldThrowWhenPersonV3FailsFunctionalInvalidSecurityToken() throws Exception {
		
		try {
			restTemplateNoHeader.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, request, KompletterBrevdataResponse.class);
			fail("Test did not throw exception");
		} catch (HttpClientErrorException e) {
			assertEquals(e.getStatusCode(), HttpStatus.BAD_REQUEST);
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Fant ingen SAML assertion token i sikkerhetskontekst. SAML assertion token kreves for å kunne kalle PersonV3"));
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("RegOppslagFunctionalException"));
		}
		
	}
	
	@Test
	public void shouldThrowWhenPersonV3FailsSecurityErrorNoAccess() throws Exception {
		stubFor(post("/VIRKSOMHET_PERSON_V3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/personV3/hentPerson-FunksjonellFeil-SikkerhetsBegrensning-responsebody.xml")));
		
		try {
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, request, KompletterBrevdataResponse.class);
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
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, request, KompletterBrevdataResponse.class);
			assertFalse(Boolean.TRUE);
		} catch (HttpStatusCodeException e) {
			assertThat(((Double) requestLatency.labels(SERVICE_CODE_TREG001, PERSONV3, HENT_PERSON)
					.get().buckets[14]).intValue(), is(Matchers.equalTo(1)));
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
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, request, KompletterBrevdataResponse.class);
			assertFalse(Boolean.TRUE);
		} catch (HttpStatusCodeException e) {
			assertThat(((Double) requestLatency.labels(SERVICE_CODE_TREG001, NORG2, HENT_KONTAKTINFORMASJON_FOR_ENHET)
					.get().buckets[14]).intValue(), isIn(Arrays.asList(1, 2, 3, 4)));
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
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, request, KompletterBrevdataResponse.class);
			assertFalse(Boolean.TRUE);
		} catch (HttpStatusCodeException e) {
			assertThat(((Double) requestLatency.labels(SERVICE_CODE_TREG001, PERSONV3, HENT_PERSON)
					.get().buckets[14]).intValue(), is(Matchers.equalTo(5)));
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
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, requestOrg, KompletterBrevdataResponse.class);
			assertFalse(Boolean.TRUE);
		} catch (HttpStatusCodeException e) {
			assertThat(((Double) requestLatency.labels(SERVICE_CODE_TREG001, ORGANISASJONV4, HENT_ORGANISASJON)
					.get().buckets[14]).intValue(), is(Matchers.equalTo(5)));
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
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, requestNorg, KompletterBrevdataResponse.class);
			assertFalse(Boolean.TRUE);
		} catch (HttpStatusCodeException e) {
			assertThat(((Double) requestLatency.labels(SERVICE_CODE_TREG001, NORG2, HENT_KONTAKTINFORMASJON_FOR_ENHET)
					.get().buckets[14]).intValue(), is(Matchers.equalTo(5)));
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
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, request, KompletterBrevdataResponse.class);
			assertFalse(Boolean.TRUE);
		} catch (HttpServerErrorException e) {
			assertThat(((Double) requestLatency.labels(SERVICE_CODE_TREG001, DOKKAT, HENT_DOKKAT_SPRAAKINFO)
					.get().buckets[14]).intValue(), is(Matchers.equalTo(1)));
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
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + KOMPLETTER_BREVDATA_URI_PATH, request, KompletterBrevdataResponse.class);
			assertFalse(Boolean.TRUE);
		} catch (HttpServerErrorException e) {
			assertThat(((Double) requestLatency.labels(SERVICE_CODE_TREG001, DOKKAT, HENT_DOKKAT_SPRAAKINFO)
					.get().buckets[14]).intValue(), is(Matchers.equalTo(5)));
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