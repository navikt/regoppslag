package no.nav.regoppslag.itest;

import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.regoppslag.api.HentMottakerOgAdresseRequest;
import no.nav.regoppslag.api.HentMottakerOgAdresseResponse;
import no.nav.regoppslag.consumer.pdl.to.Metadata;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingXPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.nav.regoppslag.rest.RegisteroppslagRestController.HENT_MOTTAKEROGADRESSE_URI_PATH;
import static no.nav.regoppslag.rest.RegisteroppslagRestController.REST;
import static no.nav.regoppslag.util.PDLResponseUtil.ADRESSELINJE_POSTBOKS;
import static no.nav.regoppslag.util.PDLResponseUtil.ADRESSENAVN_1;
import static no.nav.regoppslag.util.PDLResponseUtil.COADRESSENAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.FULLT_NAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.LANDKODE_NORGE;
import static no.nav.regoppslag.util.PDLResponseUtil.PERSON_IDENT;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTNUMMER;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTSTED;
import static no.nav.regoppslag.util.PDLResponseUtil.getStsToken;
import static no.nav.regoppslag.util.PDLResponseUtil.postPdlGraphql;
import static no.nav.regoppslag.util.PDLResponseUtil.postPdlGraphqlWithErrorResponse;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * @author Tsigab Angosom, NAV.
 */
public class Treg002MotPDLIT extends AbstractIT {

	@BeforeEach
	public void setUpStubs() {
		WireMock.reset();
		WireMock.resetAllRequests();
		WireMock.removeAllMappings();
	}

	@Test
	public void shouldGetMottakerAndAdresseForPersonWhenLandIsNull() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/BosattVegadresse.json");
		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("PERSON"), HentMottakerOgAdresseResponse.class);

		verify(1, postRequestedFor(urlMatching("/graphql")));
		verify(1, getRequestedFor(urlEqualTo("/stsRest/token?grant_type=client_credentials&scope=openid")));
		assertEquals(PERSON_IDENT, response.getIdentifikator());
		assertEquals(ADRESSENAVN_1, response.getAdresse().getAdresselinje1());
		assertEquals(FULLT_NAVN, response.getNavn());
		assertEquals(LANDKODE_NORGE, response.getAdresse().getLandkode());
		assertNull(response.getAdresse().getAdresselinje2());
		assertNull(response.getAdresse().getAdresselinje3());
		assertEquals(POSTNUMMER, response.getAdresse().getPostnummer());
		assertEquals(POSTSTED, response.getAdresse().getPoststed());
	}

	@Test
	public void shouldGetOffentligKontaktAdresseWhenPersonErDoed() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/doedperson.json");
		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("PERSON"), HentMottakerOgAdresseResponse.class);
		assertPersonAdresseWithCO(response);
		verify(1, postRequestedFor(urlMatching("/graphql")));
		verify(1, getRequestedFor(urlEqualTo("/stsRest/token?grant_type=client_credentials&scope=openid")));
	}

	@Test
	public void shouldGetMottakerAndAdresseForPerson() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/BosattVegadresse.json");
		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("PERSON"), HentMottakerOgAdresseResponse.class);

		assertNotNull(response);
		assertPersonAdresse(response);
		assertEquals(PERSON_IDENT, response.getIdentifikator());
		assertEquals(FULLT_NAVN, response.getNavn());

		verify(1, postRequestedFor(urlMatching("/graphql")));
		verify(1, getRequestedFor(urlEqualTo("/stsRest/token?grant_type=client_credentials&scope=openid")));
	}

	@Test
	public void shouldGetMottakerAndAdresseOgPrioriterBySource() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/kontaktadresse.json");
		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("PERSON"), HentMottakerOgAdresseResponse.class);

		assertNotNull(response);
		assertNotNull(response.getAdresse());


		verify(1, postRequestedFor(urlMatching("/graphql")));
		verify(1, getRequestedFor(urlEqualTo("/stsRest/token?grant_type=client_credentials&scope=openid")));
	}

	@Test
	public void shouldGetMottakerWithCoAdresse() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/bosattadressemedconavn.json");
		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("PERSON"), HentMottakerOgAdresseResponse.class);

		assertNotNull(response);
		assertPersonCOAdresse(response);
		assertEquals(PERSON_IDENT, response.getIdentifikator());
		assertEquals(FULLT_NAVN, response.getNavn());
		verify(1, postRequestedFor(urlMatching("/graphql")));
		verify(1, getRequestedFor(urlEqualTo("/stsRest/token?grant_type=client_credentials&scope=openid")));
	}

	@Test
	public void shouldGetMottakerAndAdresseForOrganisasjonHasPostadresse() {
		stubFor(post("/ORGANISASJON_V4")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/organisasjonv4/organisasjonv4-happy.xml")));
		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("ORGANISASJON"), HentMottakerOgAdresseResponse.class);

		assertEquals(PERSON_IDENT, response.getIdentifikator());
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
		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("ORGANISASJON"), HentMottakerOgAdresseResponse.class),
				"Test did not throw exception");

		assertEquals(NOT_FOUND, e.getStatusCode());
		assertThat(e.getResponseBodyAsString(), containsString("Nav enhet finnes ikke for enhetNr=0102030405, message=Ugyldig inndata: Organisasjonsnummeret (8896407842) er pÃ¥ et ugyldig format"));
	}

	@Test
	public void shouldThrowIfBadRequestMotPDL() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphqlWithErrorResponse(HttpStatus.BAD_REQUEST.value()); //mottakerPlugin
		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("PERSON"), HentMottakerOgAdresseResponse.class),
				"Funksjonell feil: feilmelding=Kunne ikke hente person fra pdl.");

		verify(1, postRequestedFor(urlEqualTo("/graphql")));
		assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
	}

	@Test
	public void shouldThrowIfPersonIsDoedAndMissingAdresse() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/doedpersonutenadresse.json");
		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("PERSON"), HentMottakerOgAdresseResponse.class);
		assertNull(response);
	}

	@Test
	public void shouldThrowWhenOrganisasjonV4FailsFunctionalNotFound() {
		stubFor(post("/ORGANISASJON_V4")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/organisasjonv4/organisasjonv4-ikkefunnet-response.xml")));

		HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("ORGANISASJON"), HentMottakerOgAdresseResponse.class),
				"Test did not throw exception");

		assertEquals(NOT_FOUND, e.getStatusCode());
	}

	@Test
	public void shouldThrowWhenOrganisasjonV4FailsTechnical() {
		stubFor(post("/ORGANISASJON_V4")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/organisasjonv4/organisasjonv4-tekniskfeil-response.xml")));
		HttpServerErrorException e = assertThrows(HttpServerErrorException.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("ORGANISASJON"), HentMottakerOgAdresseResponse.class));

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatusCode());

	}


	@Test
	public void shouldThrowWhenMottakerErUkjentbostedFailsFunctionalNotFound() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/ukjentbosted.json");
		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("PERSON"), HentMottakerOgAdresseResponse.class),
				"Test did not throw exception");
		assertEquals(NOT_FOUND, e.getStatusCode());
	}

	@Test
	public void shouldThrowWhenPDLFailsFunctionalInvalidSecurityToken() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/unauthenticated-error-response.json");
		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplateNoHeader.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("PERSON"), HentMottakerOgAdresseResponse.class),
				"Test did not throw exception");

		assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
	}

	@Test
	public void shouldThrowWhenPDLFailsTechnical() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphqlWithErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value());
		HttpServerErrorException e = assertThrows(HttpServerErrorException.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("PERSON"), HentMottakerOgAdresseResponse.class),
				"Test did not throw exception");

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatusCode());
	}

	@Test
	public void shouldThrowWhenTypeIsIncorrect() {

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("FESDASd"), HentMottakerOgAdresseResponse.class),
				"Mottakertype var FESDASd. Det må være PERSON eller ORGANISASJON.");

		assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());

	}

	@Test
	public void shouldThrowWhenIdentifikatorIsEmpty() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/BosattVegadresse.json");
		HentMottakerOgAdresseRequest request = createRequest("PERSON");
		request.setIdentifikator(null);
		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, request, HentMottakerOgAdresseResponse.class),
				"Identifikator kan ikke være null");

		assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
	}

	@Test
	public void shouldThrowWhenTypeIsEmpty() {
		HentMottakerOgAdresseRequest request = createRequest("PERSON");
		request.setType(null);
		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, request, HentMottakerOgAdresseResponse.class),
				"Mottakertype kan ikke være null");

		assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
	}

	private void assertPersonAdresse(HentMottakerOgAdresseResponse response) {
		assertEquals(ADRESSENAVN_1, response.getAdresse().getAdresselinje1());
		assertNull(response.getAdresse().getAdresselinje2());
		assertNull(response.getAdresse().getAdresselinje3());
		assertEquals(LANDKODE_NORGE, response.getAdresse().getLandkode());
		assertEquals(POSTNUMMER, response.getAdresse().getPostnummer());
		assertEquals(POSTSTED, response.getAdresse().getPoststed());
	}

	private void assertPersonAdresseWithCO(HentMottakerOgAdresseResponse response) {
		assertEquals(COADRESSENAVN, response.getAdresse().getAdresselinje1());
		assertEquals(ADRESSELINJE_POSTBOKS, response.getAdresse().getAdresselinje2());
		assertNull(response.getAdresse().getAdresselinje3());
		assertEquals(LANDKODE_NORGE, response.getAdresse().getLandkode());
		assertEquals(POSTNUMMER, response.getAdresse().getPostnummer());
		assertEquals(POSTSTED, response.getAdresse().getPoststed());
	}

	private void assertPersonCOAdresse(HentMottakerOgAdresseResponse response) {
		assertEquals(COADRESSENAVN, response.getAdresse().getAdresselinje1());
		assertEquals(ADRESSENAVN_1, response.getAdresse().getAdresselinje2());
		assertNull(response.getAdresse().getAdresselinje3());
		assertEquals(LANDKODE_NORGE, response.getAdresse().getLandkode());
		assertEquals(POSTNUMMER, response.getAdresse().getPostnummer());
		assertEquals(POSTSTED, response.getAdresse().getPoststed());
	}

	private void assertOrgAdresse(HentMottakerOgAdresseResponse response) {
		assertEquals("Postboks 5 St Olavs Plass", response.getAdresse().getAdresselinje1());
		assertNull(response.getAdresse().getAdresselinje2());
		assertNull(response.getAdresse().getAdresselinje3());
		assertEquals(LANDKODE_NORGE, response.getAdresse().getLandkode());
		assertEquals("0130", response.getAdresse().getPostnummer());
		assertEquals("OSLO", response.getAdresse().getPoststed());
	}


	private HentMottakerOgAdresseRequest createRequest(String type) {
		return HentMottakerOgAdresseRequest.builder()
				.identifikator("0102030405")
				.tema("PEN")
				.type(type).build();
	}


}
