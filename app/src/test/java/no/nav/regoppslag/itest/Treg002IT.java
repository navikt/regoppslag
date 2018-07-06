package no.nav.regoppslag.itest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingXPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.nav.regoppslag.rest.RegisteroppslagRestController.HENT_MOTTAKEROGADRESSE_URI_PATH;
import static no.nav.regoppslag.rest.RegisteroppslagRestController.REST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import no.nav.regoppslag.common.HentMottakerOgAdresseRequest;
import no.nav.regoppslag.common.HentMottakerOgAdresseResponse;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class Treg002IT extends AbstractIT {
	
	
	@Before
	public void setUpStubs(){
		
		stubFor(post("/STS")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader("Content-Type", "text/xml")
						.withBodyFile("/xsd/felles/sts/sts_signature-responsebody.xml")));
		
		stubFor(post("/VIRKSOMHET_PERSON_V3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/personV3/hentperson-happypath-responsebody.xml")));
		
		stubFor(post("/VIRKSOMHET_ORGANISASJON_V4")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/organisasjonv4/organisasjonv4-happy.xml")));
	}

	@Test
	public void shouldGetMottakerAndAdresseForPersonWhenLandIsNull(){
		stubFor(post("/VIRKSOMHET_PERSON_V3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/personV3/hentperson-happypath-null-land-responsebody.xml")));
		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("PERSON"), HentMottakerOgAdresseResponse.class);
		assertEquals(response.getIdentifikator(),"0102030405");
		assertEquals(response.getNavn(),"Geir Appleson");
		assertEquals(response.getAdresse().getLandkode(),"???");

	}

	@Test
	public void shouldGetMottakerAndAdresseForPerson() throws Exception{
		
		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("PERSON"), HentMottakerOgAdresseResponse.class);
		
		assertPersonAdresse(response);
		assertEquals(response.getIdentifikator(),"0102030405");
		assertEquals(response.getNavn(),"Geir Appleson");
		
		verify(postRequestedFor(urlMatching("/VIRKSOMHET_PERSON_V3")).withRequestBody(matchingXPath("//ident/text()", equalTo("0102030405"))));
		verify(postRequestedFor(urlMatching("/VIRKSOMHET_PERSON_V3")).withRequestBody(matchingXPath("//informasjonsbehov/text()", equalTo("adresse"))));
	}
	
	@Test
	public void shouldGetMottakerAndAdresseForOrganisasjon() throws Exception{
		
		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("ORGANISASJON"), HentMottakerOgAdresseResponse.class);
		
		assertOrgAdresse(response);
		assertEquals(response.getIdentifikator(),"0102030405");
		assertEquals(response.getNavn(),"ARBEIDS- OG VELFERDSETATEN");
		
		verify(postRequestedFor(urlMatching("/VIRKSOMHET_ORGANISASJON_V4")).withRequestBody(matchingXPath("//orgnummer/text()", equalTo("0102030405"))));
	}
	
	@Test
	public void shouldThrowWhenOrganisasjonV4FailsFunctionalInvalidInput() throws Exception{
		stubFor(post("/VIRKSOMHET_ORGANISASJON_V4")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/organisasjonv4/organisasjonv4-ugyldigInput-response.xml")));
		
		try {
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("ORGANISASJON"), HentMottakerOgAdresseResponse.class);
			assertFalse("Test did not throw exception", Boolean.TRUE);
		} catch (HttpStatusCodeException e) {
			assertEquals(e.getStatusCode(), HttpStatus.BAD_REQUEST);
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Nav enhet finnes ikke for enhetNr=0102030405, message=Ugyldig inndata: Organisasjonsnummeret (8896407842) er pÃ¥ et ugyldig format"));
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("RegOppslagFunctionalException"));
		}
		
		
	}
	
	@Test
	public void shouldThrowWhenOrganisasjonV4FailsFunctionalNotFound() throws Exception{
		stubFor(post("/VIRKSOMHET_ORGANISASJON_V4")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/organisasjonv4/organisasjonv4-ikkefunnet-response.xml")));
		
		try {
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("ORGANISASJON"), HentMottakerOgAdresseResponse.class);
			assertFalse("Test did not throw exception", Boolean.TRUE);
		} catch (HttpStatusCodeException e) {
			assertEquals(e.getStatusCode(), HttpStatus.BAD_REQUEST);
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Nav enhet finnes ikke for enhetNr=0102030405, message=Ingen organisasjon ble funnet med orgnr: 889640732"));
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("RegOppslagFunctionalException"));
		}
		
		
	}
	
	
	@Test
	public void shouldThrowWhenOrganisasjonV4FailsTechnical() throws Exception{
		stubFor(post("/VIRKSOMHET_ORGANISASJON_V4")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/organisasjonv4/organisasjonv4-tekniskfeil-response.xml")));
		
		try {
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("ORGANISASJON"), HentMottakerOgAdresseResponse.class);
			assertFalse("Test did not throw exception", Boolean.TRUE);
		} catch (HttpStatusCodeException e) {
			assertEquals(e.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Noe gikk galt i kall til OrganisasjonV4.hentOrganisasjon for enhetNr=0102030405"));
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("RegOppslagTechnicalException"));
		}
		
		
	}
	
	
	@Test
	public void shouldThrowWhenPersonV3FailsFunctionalNotFound() throws Exception{
		stubFor(post("/VIRKSOMHET_PERSON_V3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/personV3/hentPerson-FunksjonellFeil-PersonIkkeFunnet-responsebody.xml")));
		
		try {
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("PERSON"), HentMottakerOgAdresseResponse.class);
			assertFalse("Test did not throw exception", Boolean.TRUE);
		} catch (HttpStatusCodeException e) {
			assertEquals(e.getStatusCode(), HttpStatus.BAD_REQUEST);
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("PersonV3.hentPerson fant ikke person med ident=0102030405, message=Ingen forekomster funnet"));
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("RegOppslagFunctionalException"));
		}
		
	}
	
	@Test
	public void shouldThrowWhenPersonV3FailsSecurityErrorNoAccess() throws Exception {
		stubFor(post("/VIRKSOMHET_PERSON_V3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/personV3/hentPerson-FunksjonellFeil-SikkerhetsBegrensning-responsebody.xml")));
		
		try {
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("PERSON"), HentMottakerOgAdresseResponse.class);
			assertFalse("Test did not throw exception", true);
		} catch (HttpStatusCodeException e) {
			assertEquals(e.getStatusCode(), HttpStatus.UNAUTHORIZED);
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("PersonV3.hentPerson feiler på grunn av sikkerhetsbegresning. Message=Ingen tilgang"));
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("RegOppslagSecurityException"));
		}
		
	}
	
	@Test
	public void shouldThrowWhenPersonV3FailsFunctionalInvalidSecurityToken() throws Exception {
		stubFor(post("/VIRKSOMHET_PERSON_V3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/personV3/hentPerson-FunksjonellFeil-SikkerhetsBegrensning-responsebody.xml")));
		
		try {
			restTemplateNoHeader.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("PERSON"), HentMottakerOgAdresseResponse.class);
			assertFalse("Test did not throw exception", Boolean.TRUE);
		} catch (HttpClientErrorException e) {
			assertEquals(e.getStatusCode(), HttpStatus.BAD_REQUEST);
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Fant ingen SAML assertion token i sikkerhetskontekst. SAML assertion token kreves for å kunne kalle PersonV3"));
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("RegOppslagFunctionalException"));
		}
		
	}
	
	@Test
	public void shouldThrowWhenPersonV3FailsTechnical() throws Exception{
		stubFor(post("/VIRKSOMHET_PERSON_V3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/personV3/hentPerson-Tecnical-responsebody.xml")));
		
		try {
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("PERSON"), HentMottakerOgAdresseResponse.class);
			assertFalse("Test did not throw exception", Boolean.TRUE);
		} catch (HttpStatusCodeException e) {
			assertEquals(e.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Teknisk feil: feilmelding=Noe gikk galt i kall til PersonV3.hentPerson. Message=Feil med server. Overbelastning?"));
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("RegOppslagTechnicalException"));
		}
		
	}
	
	@Test
	public void shouldThrowWhenTypeIsIncorrect() throws Exception {
		
		try {
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("FESDASd"), HentMottakerOgAdresseResponse.class);
			assertFalse("Test did not throw exception", Boolean.TRUE);
		} catch (HttpStatusCodeException e) {
			assertEquals(e.getStatusCode(), HttpStatus.BAD_REQUEST);
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Mottakertype var FESDASd. Det må være PERSON eller ORGANISASJON."));
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("RegOppslagFunctionalException"));
		}
	}
	
	@Test
	public void shouldThrowWhenIdentifikatorIsEmpty() throws Exception {
		
		try {
			HentMottakerOgAdresseRequest request = createRequest("PERSON");
			request.setIdentifikator(null);
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, request, HentMottakerOgAdresseResponse.class);
			assertFalse("Test did not throw exception", Boolean.TRUE);
		} catch (HttpStatusCodeException e) {
			assertEquals(e.getStatusCode(), HttpStatus.BAD_REQUEST);
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Identifikator kan ikke være null"));
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("RegOppslagFunctionalException"));
		}
	
	}
	
	
	@Test
	public void shouldThrowWhenTypeIsEmpty() throws Exception {
		
		try {
			HentMottakerOgAdresseRequest request = createRequest("PERSON");
			request.setType(null);
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, request, HentMottakerOgAdresseResponse.class);
			assertFalse("Test did not throw exception", Boolean.TRUE);
		} catch (HttpStatusCodeException e) {
			assertEquals(e.getStatusCode(), HttpStatus.BAD_REQUEST);
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Mottakertype kan ikke være null"));
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("RegOppslagFunctionalException"));
		}

	}
	
	
	private void assertPersonAdresse(HentMottakerOgAdresseResponse response){
		assertEquals(response.getAdresse().getAdresselinje1(), "Bak Gate 10");
		assertEquals(response.getAdresse().getAdresselinje2(), null);
		assertEquals(response.getAdresse().getAdresselinje3(), null);
		assertEquals(response.getAdresse().getLandkode(), "NO");
		assertEquals(response.getAdresse().getPostnummer(), "0350");
		assertEquals(response.getAdresse().getPoststed(), "OSLO");
	}
	
	private void assertOrgAdresse(HentMottakerOgAdresseResponse response){
		assertEquals(response.getAdresse().getAdresselinje1(), "Postboks 5 St Olavs Plass");
		assertEquals(response.getAdresse().getAdresselinje2(), null);
		assertEquals(response.getAdresse().getAdresselinje3(), null);
		assertEquals(response.getAdresse().getLandkode(), "NO");
		assertEquals(response.getAdresse().getPostnummer(), "0130");
		assertEquals(response.getAdresse().getPoststed(), "OSLO");
	}
	
	
	private HentMottakerOgAdresseRequest createRequest(String type){
		return HentMottakerOgAdresseRequest.builder()
				.identifikator("0102030405")
				.type(type).build();
	}


	
	
}
