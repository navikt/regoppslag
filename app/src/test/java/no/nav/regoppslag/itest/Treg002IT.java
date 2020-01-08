package no.nav.regoppslag.itest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingXPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.nav.regoppslag.rest.RegisteroppslagRestController.HENT_MOTTAKEROGADRESSE_URI_PATH;
import static no.nav.regoppslag.rest.RegisteroppslagRestController.REST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.regoppslag.api.HentMottakerOgAdresseRequest;
import no.nav.regoppslag.api.HentMottakerOgAdresseResponse;
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
	public void setUpStubs() {
		WireMock.reset();
		WireMock.resetAllRequests();
		WireMock.removeAllMappings();
		stubFor(post("/STS")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader("Content-Type", "text/xml")
						.withBodyFile("/xsd/felles/sts/sts_signature-responsebody.xml")));

		stubFor(post("/ORGANISASJON_V4")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/organisasjonv4/organisasjonv4-happy.xml")));
	}

	@Test
	public void shouldGetMottakerAndAdresseForPersonWhenLandIsNull() {
		stubFor(post("/VIRKSOMHET_PERSONV3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/personV3/hentperson-happypath-null-land-responsebody.xml")));
		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("PERSON"), HentMottakerOgAdresseResponse.class);
		assertEquals("0102030405", response.getIdentifikator());
		assertEquals("Geir Appleson", response.getNavn());
		assertEquals("???", response.getAdresse().getLandkode());

	}

	@Test
	public void shouldGetMottakerAndAdresseForPerson() {
		stubFor(post("/VIRKSOMHET_PERSONV3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/personV3/hentperson-happypath-responsebody.xml")));
		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("PERSON"), HentMottakerOgAdresseResponse.class);

		assertNotNull(response);
		assertPersonAdresse(response);
		assertEquals("0102030405", response.getIdentifikator());
		assertEquals("Geir Appleson", response.getNavn());

		verify(postRequestedFor(urlMatching("/VIRKSOMHET_PERSONV3")).withRequestBody(matchingXPath("//ident/text()", equalTo("0102030405"))));
		verify(postRequestedFor(urlMatching("/VIRKSOMHET_PERSONV3")).withRequestBody(matchingXPath("//informasjonsbehov/text()", equalTo("adresse"))));
	}

	@Test
	public void shouldGetMottakerAndTilleggsAdresseForPerson() {
		stubFor(post("/VIRKSOMHET_PERSONV3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/personV3/hentperson-CO-responsebody.xml")));
		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("PERSON"), HentMottakerOgAdresseResponse.class);

		assertNotNull(response);
		assertPersonCOAdresse(response);
		assertEquals("0102030405", response.getIdentifikator());
		assertEquals("Nytt Navn", response.getNavn());
		assertEquals("C/O Bjarne Betjent", response.getAdresse().getAdresselinje1());
		assertEquals("Flesbergveien 381", response.getAdresse().getAdresselinje2());

		verify(postRequestedFor(urlMatching("/VIRKSOMHET_PERSONV3")).withRequestBody(matchingXPath("//ident/text()", equalTo("0102030405"))));
		verify(postRequestedFor(urlMatching("/VIRKSOMHET_PERSONV3")).withRequestBody(matchingXPath("//informasjonsbehov/text()", equalTo("adresse"))));
	}

	@Test
	public void shouldGetMottakerAndAdresseForOrganisasjonHasPostadresse() {
		stubFor(post("/VIRKSOMHET_PERSONV3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/personV3/hentperson-happypath-responsebody.xml")));
		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("ORGANISASJON"), HentMottakerOgAdresseResponse.class);

		assertEquals("0102030405", response.getIdentifikator());
		assertEquals("ARBEIDS- OG VELFERDSETATEN", response.getNavn());
		assertOrgAdresse(response);

		verify(postRequestedFor(urlMatching("/ORGANISASJON_V4")).withRequestBody(matchingXPath("//orgnummer/text()", equalTo("0102030405"))));
	}

	@Test
	public void shouldGetMottakerAndAdresseForOrganisasjonNoPostadresseOnlyForretningsadresse() {
		stubFor(post("/ORGANISASJON_V4")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/organisasjonv4/organisasjonv4-ingenpostadresse.xml")));
		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("ORGANISASJON"), HentMottakerOgAdresseResponse.class);

		assertEquals("0102030405", response.getIdentifikator());
		assertEquals("ARBEIDS- OG VELFERDSETATEN", response.getNavn());
		assertEquals("Hesteveien 94", response.getAdresse().getAdresselinje1());
		assertEquals("0579", response.getAdresse().getPostnummer());
		assertEquals("OSLO", response.getAdresse().getPoststed());
		assertEquals("NO", response.getAdresse().getLandkode());

		verify(postRequestedFor(urlMatching("/ORGANISASJON_V4")).withRequestBody(matchingXPath("//orgnummer/text()", equalTo("0102030405"))));
	}

	@Test
	public void shouldThrowWhenOrganisasjonV4FailsFunctionalInvalidInput() {
		stubFor(post("/ORGANISASJON_V4")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/organisasjonv4/organisasjonv4-ugyldigInput-response.xml")));
		try {
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("ORGANISASJON"), HentMottakerOgAdresseResponse.class);
			fail("Test did not throw exception");
		} catch (HttpStatusCodeException e) {
			assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Nav enhet finnes ikke for enhetNr=0102030405, message=Ugyldig inndata: Organisasjonsnummeret (8896407842) er pÃ¥ et ugyldig format"));
		}
	}

	@Test
	public void shouldThrowIfPersonIsMissingAdresse() {

		stubFor(post("/VIRKSOMHET_PERSONV3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/personV3/hentperson-mangler_adresse.xml"))); //mottakerPlugin
		try {
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("PERSON"), HentMottakerOgAdresseResponse.class);
			fail("Should throw technical Exception");
		} catch (HttpStatusCodeException e) {
			verify(1, postRequestedFor(urlEqualTo("/VIRKSOMHET_PERSONV3")));
			assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Ugyldig postadresse. Adresse mangler adresselinje1, postnummer, poststed og land."));
		}
	}

	@Test
	public void shouldThrowWhenOrganisasjonV4FailsFunctionalNotFound() {
		stubFor(post("/ORGANISASJON_V4")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/organisasjonv4/organisasjonv4-ikkefunnet-response.xml")));

		try {
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("ORGANISASJON"), HentMottakerOgAdresseResponse.class);
			fail("Test did not throw exception");
		} catch (HttpStatusCodeException e) {
			assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Nav enhet finnes ikke for enhetNr=0102030405, message=Ingen organisasjon ble funnet med orgnr: 889640732"));
		}
	}

	@Test
	public void shouldThrowWhenOrganisasjonV4FailsTechnical() {
		stubFor(post("/ORGANISASJON_V4")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/organisasjonv4/organisasjonv4-tekniskfeil-response.xml")));
		try {
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("ORGANISASJON"), HentMottakerOgAdresseResponse.class);
			fail("Test did not throw exception");
		} catch (HttpStatusCodeException e) {
			assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatusCode());
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Noe gikk galt i kall til OrganisasjonV4.hentOrganisasjon for enhetNr=0102030405"));
		}
	}


	@Test
	public void shouldThrowWhenPersonV3FailsFunctionalNotFound() {
		stubFor(post("/VIRKSOMHET_PERSONV3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/personV3/hentPerson-FunksjonellFeil-PersonIkkeFunnet-responsebody.xml")));

		try {
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("PERSON"), HentMottakerOgAdresseResponse.class);
			fail("Test did not throw exception");
		} catch (HttpStatusCodeException e) {
			assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("PersonV3.hentPerson fant ikke person med ident=0102030405, message=Ingen forekomster funnet"));
		}
	}

	@Test
	public void shouldThrowWhenPersonV3FailsSecurityErrorNoAccess() {
		stubFor(post("/VIRKSOMHET_PERSONV3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/personV3/hentPerson-FunksjonellFeil-SikkerhetsBegrensning-responsebody.xml")));

		try {
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("PERSON"), HentMottakerOgAdresseResponse.class);
			fail("Test did not throw exception");
		} catch (HttpStatusCodeException e) {
			assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("PersonV3.hentPerson feiler på grunn av sikkerhetsbegresning. Message=Ingen tilgang"));
		}
	}

	@Test
	public void shouldThrowWhenPersonV3FailsFunctionalInvalidSecurityToken() {
		stubFor(post("/VIRKSOMHET_PERSONV3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/personV3/hentPerson-FunksjonellFeil-SikkerhetsBegrensning-responsebody.xml")));

		try {
			restTemplateNoHeader.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("PERSON"), HentMottakerOgAdresseResponse.class);
			fail("Test did not throw exception");
		} catch (HttpClientErrorException e) {
			assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Fant ingen SAML assertion token i sikkerhetskontekst. SAML assertion token kreves for å kunne kalle PersonV3"));
		}
	}

	@Test
	public void shouldThrowWhenPersonV3FailsTechnical() {
		stubFor(post("/VIRKSOMHET_PERSONV3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/personV3/hentPerson-Tecnical-responsebody.xml")));

		try {
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("PERSON"), HentMottakerOgAdresseResponse.class);
			fail("Test did not throw exception");
		} catch (HttpStatusCodeException e) {
			assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatusCode());
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Teknisk feil: feilmelding=Noe gikk galt i kall til PersonV3.hentPerson. Message=Feil med server. Overbelastning?"));
		}
	}

	@Test
	public void shouldThrowWhenTypeIsIncorrect() {

		try {
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("FESDASd"), HentMottakerOgAdresseResponse.class);
			fail("Test did not throw exception");
		} catch (HttpStatusCodeException e) {
			assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Mottakertype var FESDASd. Det må være PERSON eller ORGANISASJON."));
		}
	}

	@Test
	public void shouldThrowWhenIdentifikatorIsEmpty() {
		try {
			HentMottakerOgAdresseRequest request = createRequest("PERSON");
			request.setIdentifikator(null);
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, request, HentMottakerOgAdresseResponse.class);
			fail("Test did not throw exception");
		} catch (HttpStatusCodeException e) {
			assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Identifikator kan ikke være null"));
		}
	}

	@Test
	public void shouldThrowWhenTypeIsEmpty() {
		try {
			HentMottakerOgAdresseRequest request = createRequest("PERSON");
			request.setType(null);
			restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, request, HentMottakerOgAdresseResponse.class);
			fail("Test did not throw exception");
		} catch (HttpStatusCodeException e) {
			assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
			assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Mottakertype kan ikke være null"));
		}
	}

	private void assertPersonAdresse(HentMottakerOgAdresseResponse response) {
		assertEquals("Bak Gate 10", response.getAdresse().getAdresselinje1());
		assertNull(response.getAdresse().getAdresselinje2());
		assertNull(response.getAdresse().getAdresselinje3());
		assertEquals("NO", response.getAdresse().getLandkode());
		assertEquals("0350", response.getAdresse().getPostnummer());
		assertEquals("OSLO", response.getAdresse().getPoststed());
	}

	private void assertPersonCOAdresse(HentMottakerOgAdresseResponse response) {
		assertEquals("C/O Bjarne Betjent", response.getAdresse().getAdresselinje1());
		assertEquals("Flesbergveien 381", response.getAdresse().getAdresselinje2());
		assertNull(response.getAdresse().getAdresselinje3());
		assertEquals("NO", response.getAdresse().getLandkode());
		assertEquals("3960", response.getAdresse().getPostnummer());
		assertEquals("STATHELLE", response.getAdresse().getPoststed());
	}

	private void assertOrgAdresse(HentMottakerOgAdresseResponse response) {
		assertEquals("Postboks 5 St Olavs Plass", response.getAdresse().getAdresselinje1());
		assertNull(response.getAdresse().getAdresselinje2());
		assertNull(response.getAdresse().getAdresselinje3());
		assertEquals("NO", response.getAdresse().getLandkode());
		assertEquals("0130", response.getAdresse().getPostnummer());
		assertEquals("OSLO", response.getAdresse().getPoststed());
	}


	private HentMottakerOgAdresseRequest createRequest(String type) {
		return HentMottakerOgAdresseRequest.builder()
				.identifikator("0102030405")
				.type(type).build();
	}


}
