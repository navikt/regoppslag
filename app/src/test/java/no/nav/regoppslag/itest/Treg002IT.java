package no.nav.regoppslag.itest;

import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.regoppslag.api.HentMottakerOgAdresseRequest;
import no.nav.regoppslag.api.HentMottakerOgAdresseResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpStatus.GONE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class Treg002IT extends AbstractIT {


	@BeforeEach
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

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () ->
				restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("ORGANISASJON"), HentMottakerOgAdresseResponse.class));
		assertEquals(NOT_FOUND, e.getStatusCode());
	}

	@Test
	public void shouldThrowIfPersonIsMissingAdresse() {
		stubFor(post("/VIRKSOMHET_PERSONV3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/personV3/hentperson-mangler_adresse.xml"))); //mottakerPlugin
		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("PERSON"), HentMottakerOgAdresseResponse.class));
		verify(1, postRequestedFor(urlEqualTo("/VIRKSOMHET_PERSONV3")));
		assertEquals(NOT_FOUND, e.getStatusCode());
	}

	@Test
	public void shouldThrowIfPersonIsDoedAndMissingAdresse() {
		stubFor(post("/VIRKSOMHET_PERSONV3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/personV3/hentperson-dod_mangler_adresse.xml"))); //mottakerPlugin

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("PERSON"), HentMottakerOgAdresseResponse.class));

		verify(1, postRequestedFor(urlEqualTo("/VIRKSOMHET_PERSONV3")));
		assertEquals(GONE, e.getStatusCode());
	}

	@Test
	public void shouldThrowWhenOrganisasjonV4FailsFunctionalNotFound() {
		stubFor(post("/ORGANISASJON_V4")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/organisasjonv4/organisasjonv4-ikkefunnet-response.xml")));

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () ->
				restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("ORGANISASJON"), HentMottakerOgAdresseResponse.class));
		assertEquals(NOT_FOUND, e.getStatusCode());

	}

	@Test
	public void shouldThrowWhenOrganisasjonV4FailsTechnical() {
		stubFor(post("/ORGANISASJON_V4")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/organisasjonv4/organisasjonv4-tekniskfeil-response.xml")));
		HttpServerErrorException e = assertThrows(HttpServerErrorException.class, () ->
				restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("ORGANISASJON"), HentMottakerOgAdresseResponse.class));
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatusCode());
	}


	@Test
	public void shouldThrowWhenPersonV3FailsFunctionalNotFound() {
		stubFor(post("/VIRKSOMHET_PERSONV3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/personV3/hentPerson-FunksjonellFeil-PersonIkkeFunnet-responsebody.xml")));

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () ->
				restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("PERSON"), HentMottakerOgAdresseResponse.class));

		assertEquals(NOT_FOUND, e.getStatusCode());

	}

	@Test
	public void shouldThrowWhenPersonV3FailsSecurityErrorNoAccess() {
		stubFor(post("/VIRKSOMHET_PERSONV3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/personV3/hentPerson-FunksjonellFeil-SikkerhetsBegrensning-responsebody.xml")));

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () ->
				restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("PERSON"), HentMottakerOgAdresseResponse.class));
		assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());

	}

	@Test
	public void shouldThrowWhenPersonV3FailsFunctionalInvalidSecurityToken() {
		stubFor(post("/VIRKSOMHET_PERSONV3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/personV3/hentPerson-FunksjonellFeil-SikkerhetsBegrensning-responsebody.xml")));

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () ->
				restTemplateNoHeader.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("PERSON"), HentMottakerOgAdresseResponse.class));
		assertEquals(NOT_FOUND, e.getStatusCode());

	}

	@Test
	public void shouldThrowWhenPersonV3FailsTechnical() {
		stubFor(post("/VIRKSOMHET_PERSONV3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/personV3/hentPerson-Tecnical-responsebody.xml")));

		HttpServerErrorException e = assertThrows(HttpServerErrorException.class, () ->
				restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("PERSON"), HentMottakerOgAdresseResponse.class));
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatusCode());

	}

	@Test
	public void shouldThrowWhenTypeIsIncorrect() {

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("FESDASd"), HentMottakerOgAdresseResponse.class));
		assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
	}

	@Test
	public void shouldThrowWhenIdentifikatorIsEmpty() {
		HentMottakerOgAdresseRequest request = createRequest("PERSON");
		request.setIdentifikator(null);
		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () ->
				restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, request, HentMottakerOgAdresseResponse.class));
		assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
	}

	@Test
	public void shouldThrowWhenTypeIsEmpty() {

		HentMottakerOgAdresseRequest request = createRequest("PERSON");
		request.setType(null);
		HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () ->
				restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, request, HentMottakerOgAdresseResponse.class));
		assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());

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
