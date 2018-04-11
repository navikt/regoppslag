package no.nav.regoppslag.itest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static no.nav.regoppslag.rest.RegisteroppslagRestController.KOMPLETTER_BREVDATA_URI_PATH;
import static no.nav.regoppslag.util.TestUtil.classpathToString;
import static no.nav.regoppslag.util.TestUtil.resourceUrlToString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import com.google.common.io.Resources;
import no.nav.regoppslag.common.ValiderOgKompletterBrevdataRequest;
import no.nav.regoppslag.common.ValiderOgKompletterBrevdataResponse;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import java.net.URL;

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
	
	private ValiderOgKompletterBrevdataRequest request = createRequest("__files/treg001/treg001_full_request.xml");
	private ValiderOgKompletterBrevdataRequest requestOrgFull = createRequest("__files/treg001/treg001_full_request_orgv4.xml");
	private ValiderOgKompletterBrevdataRequest requestOrg = createRequest("__files/treg001/treg001_request_orgv4.xml");
	private ValiderOgKompletterBrevdataRequest requestNorg = createRequest("__files/treg001/treg001_norg2_request.xml");
	
	
	@Before
	public void runBefore() {
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
						.withBodyFile("treg001/sts_signature-responsebody.xml"))); //mottakerPlugin
		
		stubFor(post("/VIRKSOMHET_PERSON_V3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/personV3/hentperson-happypath-responsebody.xml"))); //mottakerPlugin
		
		stubFor(post("/VIRKSOMHET_ORGANISASJON_V4")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/organisasjonv4/organisasjonv4-happy.xml"))); //mottakerPlugin
		
	}
	
	/**
	 * Komplertterer fullt brevdatasett der mottaker er person
	 */
	@Test
	public void shouldGetKomplettBrevdataPerson() throws Exception {
		ValiderOgKompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + KOMPLETTER_BREVDATA_URI_PATH, request, ValiderOgKompletterBrevdataResponse.class);
		assertEquals(expectedBrevdataFerdigUtfylt.replaceAll("`\n", "").replaceAll("`\t", ""), actualResponse.getBrevdata()
				.replaceAll("`\n", "")
				.replaceAll("`\t", ""));
	}
	
	/**
	 * Komplertterer fullt brevdatasett der mottaker er organisasjon
	 */
	@Test
	public void shouldGetKomplettBrevdataOrg() throws Exception {
		ValiderOgKompletterBrevdataResponse actualResponse = restTemplate.postForObject(LOCAL_ENDPOINT_URL + KOMPLETTER_BREVDATA_URI_PATH, requestOrgFull, ValiderOgKompletterBrevdataResponse.class);
		assertEquals(expectedBrevdataFerdigUtfyltOrg.replaceAll("`\n", "").replaceAll("`\t", ""), actualResponse.getBrevdata()
				.replaceAll("`\n", "")
				.replaceAll("`\t", ""));
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
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + KOMPLETTER_BREVDATA_URI_PATH, requestOrg, ValiderOgKompletterBrevdataResponse.class);
			assertFalse("Test did not throw exception", true);
		} catch (HttpStatusCodeException e) {
			assertEquals(e.getStatusCode(), HttpStatus.BAD_REQUEST);
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Ingen organisasjon ble funnet med orgnr: 111111111"));
		}
	}
	
	
	@Test
	public void shouldThrowWhenPersonV3FailsFunctionalInvalidSecurityToken() throws Exception {
		
		try {
			restTemplateNoHeader.postForObject(LOCAL_ENDPOINT_URL + KOMPLETTER_BREVDATA_URI_PATH, request, ValiderOgKompletterBrevdataResponse.class);
			assertFalse("Test did not throw exception", true);
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
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + KOMPLETTER_BREVDATA_URI_PATH, request, ValiderOgKompletterBrevdataResponse.class);
			assertFalse("Test did not throw exception", true);
		} catch (HttpStatusCodeException e) {
			assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("PersonV3.hentPerson feiler på grunn av sikkerhetsbegresning for ident: 20096828390, message=Ingen tilgang"));
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("RegOppslagSecurityException"));
		}
		
	}
	
	@Test
	public void shouldThrowFunctionalExceptionFromPersonPlugin() throws Exception {
		//Stub web services:
		stubFor(post("/VIRKSOMHET_PERSON_V3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/personV3/hentPerson-FunksjonellFeil-PersonIkkeFunnet-responsebody.xml")));
		try {
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + KOMPLETTER_BREVDATA_URI_PATH, request, ValiderOgKompletterBrevdataResponse.class);
			assertFalse("Test did not throw exception", true);
		} catch (HttpStatusCodeException e) {
			assertEquals(e.getStatusCode(), HttpStatus.BAD_REQUEST);
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("PersonV3.hentPerson fant ikke person med ident:20096828390, message=Ingen forekomster funnet"));
		}
	}
	
	@Test
	public void shouldThrowFunctionalExceptionFromNorgPlugins() throws Exception {
		//Stub web services:
		stubFor(post("/VIRKSOMHET_ORGANISASJONENHETKONTAKTINFORMASJON_V1")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/norg/hentEnhet-FunksjonellFeil-EnhetIkkeFunnet.xml"))); //mottakerPlugin
		try {
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + KOMPLETTER_BREVDATA_URI_PATH, request, ValiderOgKompletterBrevdataResponse.class);
		} catch (HttpStatusCodeException e) {
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
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + KOMPLETTER_BREVDATA_URI_PATH, request, ValiderOgKompletterBrevdataResponse.class);
			assertFalse(true);
		} catch (HttpStatusCodeException e) {
			assertEquals(e.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Noe gikk galt i kall til PersonV3.hentPerson for ident: 20096828390"));
		}
	}
	
	@Test
	public void shouldThrowTechnicalExceptionFromOrgPlugin() throws Exception {
		//Stub web services:
		stubFor(post("/VIRKSOMHET_ORGANISASJON_V4")
				.willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));
		try {
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + KOMPLETTER_BREVDATA_URI_PATH, requestOrg, ValiderOgKompletterBrevdataResponse.class);
			assertFalse(true);
		} catch (HttpStatusCodeException e) {
			assertEquals(e.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString(" Noe gikk galt i kall til OrganisasjonV4.hentOrganisasjon for enhetNr=111111111"));
		}
	}
	
	@Test
	public void shouldThrowTechnicalExceptionFromNorgPlugin() throws Exception {
		//Stub web services:
		stubFor(post("/VIRKSOMHET_ORGANISASJONENHETKONTAKTINFORMASJON_V1")
				.willReturn(notFound().withStatus(HttpStatus.NOT_FOUND.value())));
		try {
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + KOMPLETTER_BREVDATA_URI_PATH, requestNorg, ValiderOgKompletterBrevdataResponse.class);
			assertFalse(true);
		} catch (HttpStatusCodeException e) {
			assertEquals(e.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Noe gikk galt i kall til Norg for enhetNr=0136"));
		}
	}
	
	private ValiderOgKompletterBrevdataRequest createRequest(String path) {
		return ValiderOgKompletterBrevdataRequest.builder()
				.dokumentTypeId(DOKUMENTTYPEID)
				.brevdata(classpathToString(path))
				.build();
	}
}