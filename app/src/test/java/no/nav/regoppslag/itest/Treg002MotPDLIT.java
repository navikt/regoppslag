package no.nav.regoppslag.itest;

import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.regoppslag.treg002.HentMottakerOgAdresseRequest;
import no.nav.regoppslag.treg002.HentMottakerOgAdresseResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.nav.regoppslag.rest.RegisteroppslagRestController.HENT_MOTTAKEROGADRESSE_URI_PATH;
import static no.nav.regoppslag.rest.RegisteroppslagRestController.REST;
import static no.nav.regoppslag.util.PDLResponseUtil.ADRESSELINJE1_POSTBOKS;
import static no.nav.regoppslag.util.PDLResponseUtil.ADRESSELINJE2_POSTBOKS;
import static no.nav.regoppslag.util.PDLResponseUtil.ADRESSELINJE_POSTBOKS;
import static no.nav.regoppslag.util.PDLResponseUtil.ADRESSENAVN_1;
import static no.nav.regoppslag.util.PDLResponseUtil.ALPHA2_SWEDEN_LANDKODE;
import static no.nav.regoppslag.util.PDLResponseUtil.COADRESSENAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.CONAVN_UTENLANDSK_ADRESSELINJE1;
import static no.nav.regoppslag.util.PDLResponseUtil.CONAVN_UTENLANDSK_ADRESSELINJE2;
import static no.nav.regoppslag.util.PDLResponseUtil.CONAVN_UTENLANDSK_ADRESSELINJE3;
import static no.nav.regoppslag.util.PDLResponseUtil.D_NUMMER;
import static no.nav.regoppslag.util.PDLResponseUtil.FULLT_NAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.FULLT_NAVN2;
import static no.nav.regoppslag.util.PDLResponseUtil.GREECE_LANDKODE;
import static no.nav.regoppslag.util.PDLResponseUtil.LANDKODE_NORGE;
import static no.nav.regoppslag.util.PDLResponseUtil.LANDKODE_POLAND;
import static no.nav.regoppslag.util.PDLResponseUtil.LANDKODE_US;
import static no.nav.regoppslag.util.PDLResponseUtil.LAND_UTENLANDSK;
import static no.nav.regoppslag.util.PDLResponseUtil.ORGANISASJONNUMMER;
import static no.nav.regoppslag.util.PDLResponseUtil.PERSON_IDENT;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTNUMMER;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTSTED;
import static no.nav.regoppslag.util.PDLResponseUtil.UTENLANDSK_ADRESSELINJE1;
import static no.nav.regoppslag.util.PDLResponseUtil.UTENLANDSK_ADRESSELINJE2;
import static no.nav.regoppslag.util.PDLResponseUtil.UTENLANDSK_ADRESSELINJE3;
import static no.nav.regoppslag.util.PDLResponseUtil.V_ADRESSENAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.getStsToken;
import static no.nav.regoppslag.util.PDLResponseUtil.postPdlGraphql;
import static no.nav.regoppslag.util.PDLResponseUtil.postPdlGraphqlWithErrorResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @author Tsigab Angosom, NAV.
 */
public class Treg002MotPDLIT extends AbstractIT {

	private String token;

	@BeforeEach
	public void setUpStubs() {
		WireMock.reset();
		WireMock.resetAllRequests();
		WireMock.removeAllMappings();

		this.token = token("subject1");
	}

	@Test
	public void shouldGetMottakerAndAdresseForPersonWhenLandIsNull() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/BosattVegadresse.json");
		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(PERSON_IDENT, "PERSON"), HentMottakerOgAdresseResponse.class);

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
	public void shouldMapKontaktadresseFrittFormatUtenlandskadresse() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/frittformat_utenlandskadresse.json");
		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(D_NUMMER, "PERSON"), HentMottakerOgAdresseResponse.class);

		verify(1, postRequestedFor(urlMatching("/graphql")));
		verify(1, getRequestedFor(urlEqualTo("/stsRest/token?grant_type=client_credentials&scope=openid")));
		assertEquals(D_NUMMER, response.getIdentifikator());
		assertEquals(FULLT_NAVN, response.getNavn());
		assertEquals(LANDKODE_POLAND, response.getAdresse().getLandkode());
		assertEquals(UTENLANDSK_ADRESSELINJE1, response.getAdresse().getAdresselinje1());
		assertEquals(UTENLANDSK_ADRESSELINJE2, response.getAdresse().getAdresselinje2());
		assertEquals(UTENLANDSK_ADRESSELINJE3, response.getAdresse().getAdresselinje3());
		assertNull(response.getAdresse().getPostnummer());
		assertNull(response.getAdresse().getPoststed());
	}

	@Test
	public void shouldMapUtenlandskadresseWithCoAdressenavn() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/UtenlandskadresseWithCoAdressenavn.json");
		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(D_NUMMER, "PERSON"), HentMottakerOgAdresseResponse.class);

		verify(1, postRequestedFor(urlMatching("/graphql")));
		verify(1, getRequestedFor(urlEqualTo("/stsRest/token?grant_type=client_credentials&scope=openid")));
		assertEquals(D_NUMMER, response.getIdentifikator());
		assertEquals(FULLT_NAVN, response.getNavn());
		assertEquals(LANDKODE_US, response.getAdresse().getLandkode());
		assertEquals(COADRESSENAVN, response.getAdresse().getAdresselinje1());
		assertEquals(CONAVN_UTENLANDSK_ADRESSELINJE1, response.getAdresse().getAdresselinje2());
		assertEquals(CONAVN_UTENLANDSK_ADRESSELINJE2, response.getAdresse().getAdresselinje3());
		assertNull(response.getAdresse().getPostnummer());
		assertNull(response.getAdresse().getPoststed());
	}

	@Test
	public void shouldMapUtenlandskadresseWithCoAdressenavnAndDistriktOmraade() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/UtenlandskadresseWithCoAdressenavnAndDistriktOmraade.json");
		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(D_NUMMER, "PERSON"), HentMottakerOgAdresseResponse.class);

		verify(1, postRequestedFor(urlMatching("/graphql")));
		verify(1, getRequestedFor(urlEqualTo("/stsRest/token?grant_type=client_credentials&scope=openid")));
		assertEquals(D_NUMMER, response.getIdentifikator());
		assertEquals(COADRESSENAVN + ", " + CONAVN_UTENLANDSK_ADRESSELINJE1, response.getAdresse().getAdresselinje1());
		assertEquals(FULLT_NAVN, response.getNavn());
		assertEquals(LANDKODE_US, response.getAdresse().getLandkode());
		assertEquals(CONAVN_UTENLANDSK_ADRESSELINJE2, response.getAdresse().getAdresselinje2());
		assertEquals(CONAVN_UTENLANDSK_ADRESSELINJE3, response.getAdresse().getAdresselinje3());
		assertNull(response.getAdresse().getPostnummer());
		assertNull(response.getAdresse().getPoststed());
	}

	@Test
	public void shouldMapFromOppholdOrBostedadresse() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/frittformat_utenlandskadresse_null.json");
		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(D_NUMMER, "PERSON"), HentMottakerOgAdresseResponse.class);

		verify(1, postRequestedFor(urlMatching("/graphql")));
		verify(1, getRequestedFor(urlEqualTo("/stsRest/token?grant_type=client_credentials&scope=openid")));
		assertEquals(D_NUMMER, response.getIdentifikator());
		assertEquals(UTENLANDSK_ADRESSELINJE1, response.getAdresse().getAdresselinje1());
		assertNull(response.getAdresse().getAdresselinje3());
		assertEquals(FULLT_NAVN, response.getNavn());
		assertEquals(LANDKODE_POLAND, response.getAdresse().getLandkode());
	}

	@Test
	public void shouldGetMottakerAndAdresseFraBostedsadresseWhenPostnummerInKontaktadresseIsNull() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/kontaktadresse_with_null_postnummer.json");
		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(PERSON_IDENT, "PERSON"), HentMottakerOgAdresseResponse.class);

		verify(1, postRequestedFor(urlMatching("/graphql")));
		verify(1, getRequestedFor(urlEqualTo("/stsRest/token?grant_type=client_credentials&scope=openid")));
		assertEquals(PERSON_IDENT, response.getIdentifikator());
		assertEquals(ADRESSENAVN_1, response.getAdresse().getAdresselinje1());
		assertEquals(FULLT_NAVN2, response.getNavn());
		assertEquals(LANDKODE_NORGE, response.getAdresse().getLandkode());
		assertNull(response.getAdresse().getAdresselinje2());
		assertNull(response.getAdresse().getAdresselinje3());
		assertEquals(POSTNUMMER, response.getAdresse().getPostnummer());
		assertEquals(POSTSTED, response.getAdresse().getPoststed());
	}

	@Test
	public void shouldGetMottakerAndAdresseForPersonWhenAdressenErFraPostboks() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/postbokskontaktadresse.json");
		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(PERSON_IDENT, "PERSON"), HentMottakerOgAdresseResponse.class);

		verify(1, postRequestedFor(urlMatching("/graphql")));
		verify(1, getRequestedFor(urlEqualTo("/stsRest/token?grant_type=client_credentials&scope=openid")));
		assertEquals(PERSON_IDENT, response.getIdentifikator());
		assertEquals(ADRESSELINJE1_POSTBOKS, response.getAdresse().getAdresselinje1());
		assertEquals(ADRESSELINJE2_POSTBOKS, response.getAdresse().getAdresselinje2());
		assertEquals(FULLT_NAVN, response.getNavn());
		assertEquals(LANDKODE_NORGE, response.getAdresse().getLandkode());
		assertEquals(POSTNUMMER, response.getAdresse().getPostnummer());
		assertEquals(POSTSTED, response.getAdresse().getPoststed());
	}

	@Test
	public void shouldGetOffentligKontaktAdresseWhenPersonErDoed() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/doedperson.json");
		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(PERSON_IDENT, "PERSON"), HentMottakerOgAdresseResponse.class);
		assertPersonAdresseWithV(response);
		verify(1, postRequestedFor(urlMatching("/graphql")));
		verify(1, getRequestedFor(urlEqualTo("/stsRest/token?grant_type=client_credentials&scope=openid")));
	}

	@Test
	public void shouldGetMottakerAndAdresseForPerson() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/BosattVegadresse.json");
		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(PERSON_IDENT, "PERSON"), HentMottakerOgAdresseResponse.class);

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
		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(PERSON_IDENT, "PERSON"), HentMottakerOgAdresseResponse.class);

		assertNotNull(response);
		assertNotNull(response.getAdresse());


		verify(1, postRequestedFor(urlMatching("/graphql")));
		verify(1, getRequestedFor(urlEqualTo("/stsRest/token?grant_type=client_credentials&scope=openid")));
	}

	@Test
	public void shouldGetMottakerAndAdresseForUtenlandskFrittKontaktadresse() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/utenlandskfritt_kontaktadresse.json");
		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(PERSON_IDENT, "PERSON"), HentMottakerOgAdresseResponse.class);

		assertNotNull(response);
		assertNotNull(response.getAdresse());
		assertEquals(LAND_UTENLANDSK, response.getAdresse().getAdresselinje3());
		assertEquals(ALPHA2_SWEDEN_LANDKODE, response.getAdresse().getLandkode());

		verify(1, postRequestedFor(urlMatching("/graphql")));
		verify(1, getRequestedFor(urlEqualTo("/stsRest/token?grant_type=client_credentials&scope=openid")));
	}

	@Test
	public void shouldGetMottakerAndAdresseForUtenlandskKontakadresee() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/utenlandsk_kontaktadresse.json");
		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(PERSON_IDENT, "PERSON"), HentMottakerOgAdresseResponse.class);

		assertNotNull(response);
		assertNotNull(response.getAdresse());
		assertNull(response.getAdresse().getAdresselinje3());
		assertEquals(GREECE_LANDKODE, response.getAdresse().getLandkode());


		verify(1, postRequestedFor(urlMatching("/graphql")));
		verify(1, getRequestedFor(urlEqualTo("/stsRest/token?grant_type=client_credentials&scope=openid")));
	}

	@Test
	public void shouldGetMottakerWithCoAdresse() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/bosattadressemedconavn.json");
		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(PERSON_IDENT, "PERSON"), HentMottakerOgAdresseResponse.class);

		assertNotNull(response);
		assertPersonCOAdresse(response);
		assertEquals(PERSON_IDENT, response.getIdentifikator());
		assertEquals(FULLT_NAVN, response.getNavn());
		verify(1, postRequestedFor(urlMatching("/graphql")));
		verify(1, getRequestedFor(urlEqualTo("/stsRest/token?grant_type=client_credentials&scope=openid")));
	}

	@Test
	public void shouldGetMottakerAndAdresseForOrganisasjonHasPostadresse() {
		stubFor(get("/v1/organisasjon/" + ORGANISASJONNUMMER)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withHeader("Connection", "close")
						.withBodyFile("treg002/ereg/ereg-happy.json")));
		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(ORGANISASJONNUMMER, "ORGANISASJON"), HentMottakerOgAdresseResponse.class);

		assertEquals(ORGANISASJONNUMMER, response.getIdentifikator());
		assertEquals("NAV IKT", response.getNavn());
		assertOrgAdresse(response);

		verify(getRequestedFor(urlMatching("/v1/organisasjon/" + ORGANISASJONNUMMER)));
	}

	@Test
	public void shouldGetMottakerAndAdresseForUtenlandskOrganisasjonHasPostadresse() {
		stubFor(get("/v1/organisasjon/" + ORGANISASJONNUMMER)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withHeader("Connection", "close")
						.withBodyFile("treg002/ereg/ereg-happy-utenlandsk.json")));
		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(ORGANISASJONNUMMER, "ORGANISASJON"), HentMottakerOgAdresseResponse.class);

		assertEquals(ORGANISASJONNUMMER, response.getIdentifikator());
		assertEquals("LYS KOSTBAR STRUTS GMBH", response.getNavn());
		assertUtenlandskOrgAdresse(response);

		verify(getRequestedFor(urlMatching("/v1/organisasjon/" + ORGANISASJONNUMMER)));
	}

	@Test
	public void shouldGetMottakerAndAdresseForOrganisasjonNoPostadresseOnlyForretningsadresse() {
		stubFor(get("/v1/organisasjon/" + ORGANISASJONNUMMER)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withHeader("Connection", "close")
						.withBodyFile("treg002/ereg/ereg-ingenpostadresse.json")));
		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(ORGANISASJONNUMMER, "ORGANISASJON"), HentMottakerOgAdresseResponse.class);

		assertEquals(ORGANISASJONNUMMER, response.getIdentifikator());
		assertEquals("LYS KOSTBAR STRUTS GMBH", response.getNavn());
		assertUtenlandskOrgAdresse(response);
	}

	@Test
	public void shouldThrowWhenEregOrganisasjonFailsFunctionalInvalidInput() {
		stubFor(get("/v1/organisasjon/" + ORGANISASJONNUMMER)
				.willReturn(aResponse().withStatus(BAD_REQUEST.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withHeader("Connection", "close")
						.withBodyFile("treg002/ereg/ereg-ugyldiginput.json")));

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(ORGANISASJONNUMMER, "ORGANISASJON"), HentMottakerOgAdresseResponse.class),
				"Test did not throw exception");

		assertEquals(BAD_REQUEST, e.getStatusCode());
	}

	@Test
	public void shouldThrowIfBadRequestMotPDL() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphqlWithErrorResponse(HttpStatus.BAD_REQUEST.value()); //mottakerPlugin
		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(PERSON_IDENT, "PERSON"), HentMottakerOgAdresseResponse.class),
				"Funksjonell feil: feilmelding=Kunne ikke hente person fra pdl.");

		verify(1, postRequestedFor(urlEqualTo("/graphql")));
		assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
	}

	@Test
	public void shouldThrowIfPersonIsDoedAndMissingAdresse() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/doedpersonutenadresse.json");
		HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(PERSON_IDENT, "PERSON"), HentMottakerOgAdresseResponse.class));
		assertEquals(HttpStatus.GONE, e.getStatusCode());
	}

	@Test
	public void shouldThrowWhenEregOrganisasjonFailsFunctionalNotFound() {
		stubFor(get("/v1/organisasjon/" + ORGANISASJONNUMMER)
				.willReturn(aResponse().withStatus(NOT_FOUND.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withHeader("Connection", "close")
						.withBodyFile("treg002/ereg/ereg-ikkefunnet.json")));

		HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(ORGANISASJONNUMMER, "ORGANISASJON"), HentMottakerOgAdresseResponse.class),
				"Test did not throw exception");
		assertEquals(NOT_FOUND, e.getStatusCode());
	}

	@Test
	public void shouldThrowWhenEregOrganisasjonFailsTechnical() {
		stubFor(get("/v1/organisasjon/" + ORGANISASJONNUMMER)
				.willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withHeader("Connection", "close")
						.withBodyFile("treg002/ereg/ereg-tekniskfeil.json")));
		HttpServerErrorException e = assertThrows(HttpServerErrorException.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(ORGANISASJONNUMMER, "ORGANISASJON"), HentMottakerOgAdresseResponse.class));

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatusCode());

	}

	@Test
	public void shouldThrowWhenMottakerErUkjentbostedFailsFunctionalNotFound() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/ukjentbosted.json");
		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(PERSON_IDENT, "PERSON"), HentMottakerOgAdresseResponse.class),
				"Test did not throw exception");
		assertEquals(NOT_FOUND, e.getStatusCode());
	}

	@Test
	public void shouldThrowWhenPDLFailsFunctionalInvalidSecurityToken() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/unauthenticated-error-response.json");
		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplateNoHeader.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequestNoToken("PERSON"), HentMottakerOgAdresseResponse.class),
				"Test did not throw exception");

		assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
	}

	@Test
	public void shouldThrowWhenPDLFailsTechnical() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphqlWithErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value());
		HttpServerErrorException e = assertThrows(HttpServerErrorException.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(PERSON_IDENT, "PERSON"), HentMottakerOgAdresseResponse.class),
				"Test did not throw exception");

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatusCode());
	}

	@Test
	public void shouldThrowWhenTypeIsIncorrect() {
		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(PERSON_IDENT, "FESDASd"), HentMottakerOgAdresseResponse.class),
				"Mottakertype var FESDASd. Det må være PERSON eller ORGANISASJON.");

		assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());

	}

	@Test
	public void shouldThrowWhenIdentifikatorIsEmpty() {
		getStsToken(HttpStatus.OK.value(), "sts/stsResponse_happy.json");
		postPdlGraphql(HttpStatus.OK.value(), "pdl/BosattVegadresse.json");
		HttpEntity<HentMottakerOgAdresseRequest> request = createRequest(PERSON_IDENT, "PERSON");
		request.getBody().setIdentifikator(null);
		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, request, HentMottakerOgAdresseResponse.class),
				"Identifikator kan ikke være null");

		assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
	}

	@Test
	public void shouldThrowWhenTypeIsEmpty() {
		HttpEntity<HentMottakerOgAdresseRequest> request = createRequest(PERSON_IDENT, "PERSON");
		request.getBody().setType(null);
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
		assertEquals(V_ADRESSENAVN, response.getAdresse().getAdresselinje1());
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

	private void assertPersonAdresseWithV(HentMottakerOgAdresseResponse response) {
		assertEquals(V_ADRESSENAVN, response.getAdresse().getAdresselinje1());
		assertEquals(ADRESSELINJE_POSTBOKS, response.getAdresse().getAdresselinje2());
		assertNull(response.getAdresse().getAdresselinje3());
		assertEquals(LANDKODE_NORGE, response.getAdresse().getLandkode());
		assertEquals(POSTNUMMER, response.getAdresse().getPostnummer());
		assertEquals(POSTSTED, response.getAdresse().getPoststed());
	}

	private void assertOrgAdresse(HentMottakerOgAdresseResponse response) {
		assertEquals("Postboks 5 St Olavs plass", response.getAdresse().getAdresselinje1());
		assertNull(response.getAdresse().getAdresselinje2());
		assertNull(response.getAdresse().getAdresselinje3());
		assertEquals(LANDKODE_NORGE, response.getAdresse().getLandkode());
		assertEquals("0130", response.getAdresse().getPostnummer());
		assertEquals("OSLO", response.getAdresse().getPoststed());
	}

	private void assertUtenlandskOrgAdresse(HentMottakerOgAdresseResponse response) {
		assertEquals("Vestre Vollen", response.getAdresse().getAdresselinje1());
		assertEquals("78246 MAXDORF", response.getAdresse().getAdresselinje2());
		assertNull(response.getAdresse().getAdresselinje3());
		assertEquals("DE", response.getAdresse().getLandkode());
		assertNull(response.getAdresse().getPostnummer());
		assertNull(response.getAdresse().getPoststed());
	}


	private HttpEntity<HentMottakerOgAdresseRequest> createRequest(String ident, String type) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + this.token);

		HentMottakerOgAdresseRequest hentMottakerOgAdresseRequest = HentMottakerOgAdresseRequest.builder()
				.identifikator(ident)
				.tema("PEN")
				.type(type).build();

		return new HttpEntity<>(hentMottakerOgAdresseRequest, headers);
	}

	private HttpEntity<HentMottakerOgAdresseRequest> createRequestNoToken(String type) {
		HttpHeaders headers = new HttpHeaders();

		HentMottakerOgAdresseRequest hentMottakerOgAdresseRequest = HentMottakerOgAdresseRequest.builder()
				.identifikator("0102030405")
				.tema("PEN")
				.type(type).build();

		return new HttpEntity<>(hentMottakerOgAdresseRequest, headers);
	}


}
