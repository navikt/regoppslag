package no.nav.regoppslag.itest;

import no.nav.regoppslag.treg002.HentMottakerOgAdresseRequest;
import no.nav.regoppslag.treg002.HentMottakerOgAdresseResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import java.time.Clock;
import java.time.Instant;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.nav.regoppslag.config.TimeConfig.OSLO_ZONE;
import static no.nav.regoppslag.rest.RegisteroppslagRestController.HENT_MOTTAKEROGADRESSE_URI_PATH;
import static no.nav.regoppslag.rest.RegisteroppslagRestController.REST;
import static no.nav.regoppslag.util.PDLResponseUtil.ADRESSELINJE1_POSTBOKS;
import static no.nav.regoppslag.util.PDLResponseUtil.ADRESSELINJE2_POSTBOKS;
import static no.nav.regoppslag.util.PDLResponseUtil.ADRESSELINJE_POSTBOKS;
import static no.nav.regoppslag.util.PDLResponseUtil.ADRESSENAVN_1;
import static no.nav.regoppslag.util.PDLResponseUtil.ALPHA2_SWEDEN_LANDKODE;
import static no.nav.regoppslag.util.PDLResponseUtil.BYGNINGETASJELEILIGHET;
import static no.nav.regoppslag.util.PDLResponseUtil.BYGNING_ETASJE_LEILIGHET_BVH;
import static no.nav.regoppslag.util.PDLResponseUtil.BYSTED_BVH;
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
import static no.nav.regoppslag.util.PDLResponseUtil.POSTKODE_BVH;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTNUMMER;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTSTED;
import static no.nav.regoppslag.util.PDLResponseUtil.UTENLANDSK_ADRESSELINJE1;
import static no.nav.regoppslag.util.PDLResponseUtil.UTENLANDSK_ADRESSELINJE2;
import static no.nav.regoppslag.util.PDLResponseUtil.UTENLANDSK_ADRESSELINJE3;
import static no.nav.regoppslag.util.PDLResponseUtil.V_ADRESSENAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.postPdlGraphql;
import static no.nav.regoppslag.util.PDLResponseUtil.postPdlGraphqlWithErrorResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.GONE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class Treg002MotPDLIT extends AbstractIT {

	private static final Instant TIMECRITICAL_TESTDATA_ADDED_TIME = Instant.parse("2022-08-10T09:00:00.000Z");
	private static final String TEMA = "PEN";
	private static final String TYPE_PERSON = "PERSON";
	private static final String TYPE_ORGANISASJON = "ORGANISASJON";

	@MockBean
	private Clock mockClock;

	@BeforeEach
	void setMockClock() {
		when(mockClock.instant()).thenReturn(TIMECRITICAL_TESTDATA_ADDED_TIME);
		when(mockClock.getZone()).thenReturn(OSLO_ZONE);
	}

	@Test
	public void shouldGetMottakerAndAdresseForPersonWhenLandIsNull() {
		postPdlGraphql(OK.value(), "pdl/BosattVegadresse.json");

		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(PERSON_IDENT, TEMA, TYPE_PERSON), HentMottakerOgAdresseResponse.class);

		assertEquals(PERSON_IDENT, response.getIdentifikator());
		assertEquals(ADRESSENAVN_1, response.getAdresse().getAdresselinje1());
		assertEquals(FULLT_NAVN, response.getNavn());
		assertEquals(LANDKODE_NORGE, response.getAdresse().getLandkode());
		assertNull(response.getAdresse().getAdresselinje2());
		assertNull(response.getAdresse().getAdresselinje3());
		assertEquals(POSTNUMMER, response.getAdresse().getPostnummer());
		assertEquals(POSTSTED, response.getAdresse().getPoststed());

		verify(1, postRequestedFor(urlMatching("/graphql")));
	}

	@Test
	public void shouldMapKontaktadresseFrittFormatUtenlandskadresse() {
		postPdlGraphql(OK.value(), "pdl/frittformat_utenlandskadresse.json");

		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(D_NUMMER, TEMA, TYPE_PERSON), HentMottakerOgAdresseResponse.class);

		assertEquals(D_NUMMER, response.getIdentifikator());
		assertEquals(FULLT_NAVN, response.getNavn());
		assertEquals(LANDKODE_POLAND, response.getAdresse().getLandkode());
		assertEquals(UTENLANDSK_ADRESSELINJE1, response.getAdresse().getAdresselinje1());
		assertEquals(UTENLANDSK_ADRESSELINJE2, response.getAdresse().getAdresselinje2());
		assertEquals(UTENLANDSK_ADRESSELINJE3, response.getAdresse().getAdresselinje3());
		assertNull(response.getAdresse().getPostnummer());
		assertNull(response.getAdresse().getPoststed());

		verify(1, postRequestedFor(urlMatching("/graphql")));
	}

	@Test
	public void shouldMapUtenlandskAdresseWithCoAdressenavn() {
		postPdlGraphql(OK.value(), "pdl/UtenlandskadresseWithCoAdressenavn.json");

		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(D_NUMMER, TEMA, TYPE_PERSON), HentMottakerOgAdresseResponse.class);

		assertEquals(D_NUMMER, response.getIdentifikator());
		assertEquals(FULLT_NAVN, response.getNavn());
		assertEquals(LANDKODE_US, response.getAdresse().getLandkode());
		assertEquals(COADRESSENAVN, response.getAdresse().getAdresselinje1());
		assertEquals(CONAVN_UTENLANDSK_ADRESSELINJE1, response.getAdresse().getAdresselinje2());
		assertEquals(CONAVN_UTENLANDSK_ADRESSELINJE2, response.getAdresse().getAdresselinje3());
		assertNull(response.getAdresse().getPostnummer());
		assertNull(response.getAdresse().getPoststed());

		verify(1, postRequestedFor(urlMatching("/graphql")));
	}

	@Test
	public void shouldMapUtenlandskAdresseWithCoAdressenavnWithoutCoPrefix() {
		postPdlGraphql(OK.value(), "pdl/UtenlandskadresseWithCoAdressenavnUtenCo.json");

		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(D_NUMMER, TEMA, TYPE_PERSON), HentMottakerOgAdresseResponse.class);

		assertEquals(D_NUMMER, response.getIdentifikator());
		assertEquals(FULLT_NAVN, response.getNavn());
		assertEquals(LANDKODE_US, response.getAdresse().getLandkode());
		assertEquals(COADRESSENAVN, response.getAdresse().getAdresselinje1());
		assertEquals(CONAVN_UTENLANDSK_ADRESSELINJE1, response.getAdresse().getAdresselinje2());
		assertEquals(CONAVN_UTENLANDSK_ADRESSELINJE2, response.getAdresse().getAdresselinje3());
		assertNull(response.getAdresse().getPostnummer());
		assertNull(response.getAdresse().getPoststed());

		verify(1, postRequestedFor(urlMatching("/graphql")));
	}

	@Test
	public void shouldMapUtenlandskAdresseWithCoAdressenavnAndDistriktOmraade() {
		postPdlGraphql(OK.value(), "pdl/UtenlandskadresseWithCoAdressenavnAndDistriktOmraade.json");

		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(D_NUMMER, TEMA, TYPE_PERSON), HentMottakerOgAdresseResponse.class);

		assertEquals(D_NUMMER, response.getIdentifikator());
		assertEquals(COADRESSENAVN, response.getAdresse().getAdresselinje1());
		assertEquals(FULLT_NAVN, response.getNavn());
		assertEquals(LANDKODE_US, response.getAdresse().getLandkode());
		assertEquals(CONAVN_UTENLANDSK_ADRESSELINJE1, response.getAdresse().getAdresselinje2());
		assertEquals(CONAVN_UTENLANDSK_ADRESSELINJE2 + ", " + CONAVN_UTENLANDSK_ADRESSELINJE3, response.getAdresse().getAdresselinje3());
		assertNull(response.getAdresse().getPostnummer());
		assertNull(response.getAdresse().getPoststed());

		verify(1, postRequestedFor(urlMatching("/graphql")));
	}

	@Test
	public void shouldMapUtenlandskAdresseWithCoAdressenavnAndBygningEtasjeLeilighet() {
		postPdlGraphql(OK.value(), "pdl/UtenlandskadresseWithCoAdressenavn&BygningLeilighet.json");

		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(D_NUMMER, TEMA, TYPE_PERSON), HentMottakerOgAdresseResponse.class);

		assertEquals(D_NUMMER, response.getIdentifikator());
		assertEquals(COADRESSENAVN + ", " + CONAVN_UTENLANDSK_ADRESSELINJE1, response.getAdresse().getAdresselinje1());
		assertEquals(FULLT_NAVN, response.getNavn());
		assertEquals(LANDKODE_US, response.getAdresse().getLandkode());
		assertEquals(BYGNINGETASJELEILIGHET, response.getAdresse().getAdresselinje2());
		assertEquals(CONAVN_UTENLANDSK_ADRESSELINJE2, response.getAdresse().getAdresselinje3());
		assertNull(response.getAdresse().getPostnummer());
		assertNull(response.getAdresse().getPoststed());

		verify(1, postRequestedFor(urlMatching("/graphql")));

	}

	@Test
	public void shouldMapFromOppholdsadresse() {
		postPdlGraphql(OK.value(), "pdl/frittformat_utenlandskadresse_null.json");

		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(D_NUMMER, TEMA, TYPE_PERSON), HentMottakerOgAdresseResponse.class);

		assertEquals(D_NUMMER, response.getIdentifikator());
		assertEquals(UTENLANDSK_ADRESSELINJE1, response.getAdresse().getAdresselinje1());
		assertNull(response.getAdresse().getAdresselinje3());
		assertEquals(FULLT_NAVN, response.getNavn());
		assertEquals(LANDKODE_POLAND, response.getAdresse().getLandkode());

		verify(1, postRequestedFor(urlMatching("/graphql")));
	}

	@Test
	public void shouldGetMottakerAndAdresseFraBostedsadresseWhenPostnummerInKontaktadresseIsNull() {
		postPdlGraphql(OK.value(), "pdl/kontaktadresse_with_null_postnummer.json");

		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(PERSON_IDENT, TEMA, TYPE_PERSON), HentMottakerOgAdresseResponse.class);

		assertEquals(PERSON_IDENT, response.getIdentifikator());
		assertEquals(ADRESSENAVN_1, response.getAdresse().getAdresselinje1());
		assertEquals(FULLT_NAVN2, response.getNavn());
		assertEquals(LANDKODE_NORGE, response.getAdresse().getLandkode());
		assertNull(response.getAdresse().getAdresselinje2());
		assertNull(response.getAdresse().getAdresselinje3());
		assertEquals(POSTNUMMER, response.getAdresse().getPostnummer());
		assertEquals(POSTSTED, response.getAdresse().getPoststed());

		verify(1, postRequestedFor(urlMatching("/graphql")));
	}

	@Test
	public void shouldNotMapFromNorwegianBostedsadresseWhenNewerThanKontaktadresse() {
		postPdlGraphql(OK.value(), "pdl/kontaktadresse_with_new_bostedadresse.json");

		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(PERSON_IDENT, TEMA, TYPE_PERSON), HentMottakerOgAdresseResponse.class);

		assertEquals(PERSON_IDENT, response.getIdentifikator());
		assertEquals(UTENLANDSK_ADRESSELINJE1, response.getAdresse().getAdresselinje1());
		assertEquals(FULLT_NAVN2, response.getNavn());
		assertEquals(LANDKODE_POLAND, response.getAdresse().getLandkode());
		assertEquals(UTENLANDSK_ADRESSELINJE2, response.getAdresse().getAdresselinje2());
		assertEquals(UTENLANDSK_ADRESSELINJE3, response.getAdresse().getAdresselinje3());
		assertNull(response.getAdresse().getPostnummer());
		assertNull(response.getAdresse().getPoststed());

		verify(1, postRequestedFor(urlMatching("/graphql")));
	}

	@Test
	public void shouldGetMottakerAndAdresseForPersonWhenAdressenErFraPostboks() {
		postPdlGraphql(OK.value(), "pdl/postbokskontaktadresse.json");

		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(PERSON_IDENT, TEMA, TYPE_PERSON), HentMottakerOgAdresseResponse.class);

		assertEquals(PERSON_IDENT, response.getIdentifikator());
		assertEquals(ADRESSELINJE1_POSTBOKS, response.getAdresse().getAdresselinje1());
		assertEquals(ADRESSELINJE2_POSTBOKS, response.getAdresse().getAdresselinje2());
		assertEquals(FULLT_NAVN, response.getNavn());
		assertEquals(LANDKODE_NORGE, response.getAdresse().getLandkode());
		assertEquals(POSTNUMMER, response.getAdresse().getPostnummer());
		assertEquals(POSTSTED, response.getAdresse().getPoststed());

		verify(1, postRequestedFor(urlMatching("/graphql")));
	}

	@Test
	public void shouldGetOffentligKontaktAdresseWhenPersonErDoed() {
		postPdlGraphql(OK.value(), "pdl/doedperson.json");

		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(PERSON_IDENT, TEMA, TYPE_PERSON), HentMottakerOgAdresseResponse.class);

		assertPersonAdresseWithV(response);
		verify(1, postRequestedFor(urlMatching("/graphql")));
	}

	@Test
	public void shouldGetMottakerAndAdresseForPerson() {
		postPdlGraphql(OK.value(), "pdl/BosattVegadresse.json");

		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(PERSON_IDENT, TEMA, TYPE_PERSON), HentMottakerOgAdresseResponse.class);

		assertNotNull(response);
		assertPersonAdresse(response);
		assertEquals(PERSON_IDENT, response.getIdentifikator());
		assertEquals(FULLT_NAVN, response.getNavn());

		verify(1, postRequestedFor(urlMatching("/graphql")));
	}

	@Test
	public void shouldGetMottakerAndAdresseOgPrioriterBySource() {
		postPdlGraphql(OK.value(), "pdl/kontaktadresse.json");

		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(PERSON_IDENT, TEMA, TYPE_PERSON), HentMottakerOgAdresseResponse.class);

		assertNotNull(response);
		assertNotNull(response.getAdresse());

		verify(1, postRequestedFor(urlMatching("/graphql")));
	}

	@Test
	public void shouldGetMottakerAndAdresseForUtenlandskFrittKontaktadresse() {
		postPdlGraphql(OK.value(), "pdl/utenlandskfritt_kontaktadresse.json");

		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(PERSON_IDENT, TEMA, TYPE_PERSON), HentMottakerOgAdresseResponse.class);

		assertNotNull(response);
		assertNotNull(response.getAdresse());
		assertEquals(LAND_UTENLANDSK, response.getAdresse().getAdresselinje3());
		assertEquals(ALPHA2_SWEDEN_LANDKODE, response.getAdresse().getLandkode());

		verify(1, postRequestedFor(urlMatching("/graphql")));
	}

	@Test
	public void shouldGetMottakerAndAdresseForUtenlandskKontakadresee() {
		postPdlGraphql(OK.value(), "pdl/utenlandsk_kontaktadresse.json");

		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(PERSON_IDENT, TEMA, TYPE_PERSON), HentMottakerOgAdresseResponse.class);

		assertNotNull(response);
		assertNotNull(response.getAdresse());
		assertNull(response.getAdresse().getAdresselinje3());
		assertEquals(GREECE_LANDKODE, response.getAdresse().getLandkode());

		verify(1, postRequestedFor(urlMatching("/graphql")));
	}

	@Test
	public void should_dothething_GetMottakerAndAdresseForUtenlandskKontakadresee() {
		postPdlGraphql(OK.value(), "pdl/utenlandsk_uten_postboksadressenavnnummer.json");

		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(PERSON_IDENT, TEMA, TYPE_PERSON), HentMottakerOgAdresseResponse.class);

		assertNotNull(response);
		assertNotNull(response.getAdresse());
		assertEquals(BYGNING_ETASJE_LEILIGHET_BVH, response.getAdresse().getAdresselinje1());
		assertEquals(POSTKODE_BVH + " " + BYSTED_BVH, response.getAdresse().getAdresselinje2());
		assertNull(response.getAdresse().getAdresselinje3());
		assertEquals(LANDKODE_US, response.getAdresse().getLandkode());

		verify(1, postRequestedFor(urlMatching("/graphql")));
	}

	@Test
	public void shouldGetMottakerWithCoAdresse() {
		postPdlGraphql(OK.value(), "pdl/bosattadressemedconavn.json");

		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(PERSON_IDENT, TEMA, TYPE_PERSON), HentMottakerOgAdresseResponse.class);

		assertNotNull(response);
		assertPersonCOAdresse(response);
		assertEquals(PERSON_IDENT, response.getIdentifikator());
		assertEquals(FULLT_NAVN, response.getNavn());

		verify(1, postRequestedFor(urlMatching("/graphql")));
	}

	@Test
	public void shouldGetMottakerWithCoAdresseWithoutCoPrefix() {
		postPdlGraphql(OK.value(), "pdl/bosattadressemedconavnutenco.json");

		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("11111111111", TEMA, TYPE_PERSON), HentMottakerOgAdresseResponse.class);

		assertNotNull(response);

		assertEquals("C/O Max Mekker", response.getAdresse().getAdresselinje1());
		assertEquals("Sesam Stasjon 1A", response.getAdresse().getAdresselinje2());
		assertNull(response.getAdresse().getAdresselinje3());
		assertEquals("1461", response.getAdresse().getPostnummer());
		assertEquals("LØRENSKOG", response.getAdresse().getPoststed());
		assertEquals(LANDKODE_NORGE, response.getAdresse().getLandkode());

		assertEquals("11111111111", response.getIdentifikator());
		assertEquals("BJARNE BETJENT", response.getNavn());

		verify(1, postRequestedFor(urlMatching("/graphql")));
	}

	@Test
	public void shouldGetMottakerAndAdresseForOrganisasjonHasPostadresse() {
		stubFor(get("/v1/organisasjon/" + ORGANISASJONNUMMER)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withHeader("Connection", "close")
						.withBodyFile("treg002/ereg/ereg-happy.json")));

		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(ORGANISASJONNUMMER, TEMA, TYPE_ORGANISASJON), HentMottakerOgAdresseResponse.class);

		assertEquals(ORGANISASJONNUMMER, response.getIdentifikator());
		assertEquals("YARA INTERNATIONAL ASA", response.getNavn());
		assertOrgAdresse(response);

		verify(getRequestedFor(urlMatching("/v1/organisasjon/" + ORGANISASJONNUMMER)));
	}

	@Test
	public void shouldGetMottakerAndAdresseForUtenlandskOrganisasjonHasPostadresse() {
		stubFor(get("/v1/organisasjon/" + ORGANISASJONNUMMER)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withHeader("Connection", "close")
						.withBodyFile("treg002/ereg/ereg-happy-utenlandsk.json")));

		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(ORGANISASJONNUMMER, TEMA, TYPE_ORGANISASJON), HentMottakerOgAdresseResponse.class);

		assertEquals(ORGANISASJONNUMMER, response.getIdentifikator());
		assertEquals("LYS KOSTBAR STRUTS GMBH", response.getNavn());
		assertUtenlandskOrgAdresse(response);

		verify(getRequestedFor(urlMatching("/v1/organisasjon/" + ORGANISASJONNUMMER)));
	}

	@Test
	public void shouldGetMottakerAndAdresseForOrganisasjonNoPostadresseOnlyForretningsadresse() {
		stubFor(get("/v1/organisasjon/" + ORGANISASJONNUMMER)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withHeader("Connection", "close")
						.withBodyFile("treg002/ereg/ereg-ingenpostadresse.json")));

		HentMottakerOgAdresseResponse response = restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(ORGANISASJONNUMMER, TEMA, TYPE_ORGANISASJON), HentMottakerOgAdresseResponse.class);

		assertEquals(ORGANISASJONNUMMER, response.getIdentifikator());
		assertEquals("LYS KOSTBAR STRUTS GMBH", response.getNavn());
		assertUtenlandskOrgAdresse(response);
	}

	@Test
	public void shouldReturnBadRequestWhenEregOrganisasjonFailsFunctionalInvalidInput() {
		stubFor(get("/v1/organisasjon/" + ORGANISASJONNUMMER)
				.willReturn(aResponse()
						.withStatus(BAD_REQUEST.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withHeader("Connection", "close")
						.withBodyFile("treg002/ereg/ereg-ugyldiginput.json")));

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(ORGANISASJONNUMMER, TEMA, TYPE_ORGANISASJON), HentMottakerOgAdresseResponse.class),
				"Test did not throw exception");

		assertEquals(BAD_REQUEST, e.getStatusCode());
	}

	@Test
	public void shouldReturnBadRequestIfBadRequestFromPDL() {
		postPdlGraphqlWithErrorResponse(BAD_REQUEST.value()); //mottakerPlugin

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(PERSON_IDENT, TEMA, TYPE_PERSON), HentMottakerOgAdresseResponse.class),
				"Funksjonell feil: feilmelding=Kunne ikke hente person fra pdl.");

		verify(1, postRequestedFor(urlEqualTo("/graphql")));
		assertEquals(BAD_REQUEST, e.getStatusCode());
	}

	@Test
	public void shouldReturnGoneIfIfPersonIsDoedAndMissingAdresse() {
		postPdlGraphql(OK.value(), "pdl/doedpersonutenadresse.json");

		HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(PERSON_IDENT, TEMA, TYPE_PERSON), HentMottakerOgAdresseResponse.class));

		assertEquals(GONE, e.getStatusCode());
	}

	@Test
	public void shouldReturnNotFoundWhenEregOrganisasjonFailsFunctionalNotFound() {
		stubFor(get("/v1/organisasjon/" + ORGANISASJONNUMMER)
				.willReturn(aResponse()
						.withStatus(NOT_FOUND.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withHeader("Connection", "close")
						.withBodyFile("treg002/ereg/ereg-ikkefunnet.json")));

		HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(ORGANISASJONNUMMER, TEMA, TYPE_ORGANISASJON), HentMottakerOgAdresseResponse.class),
				"Test did not throw exception");

		assertEquals(NOT_FOUND, e.getStatusCode());
	}

	@Test
	public void shouldReturnInternalServerErrorWhenEregOrganisasjonFailsTechnical() {
		stubFor(get("/v1/organisasjon/" + ORGANISASJONNUMMER)
				.willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withHeader("Connection", "close")
						.withBodyFile("treg002/ereg/ereg-tekniskfeil.json")));

		HttpServerErrorException e = assertThrows(HttpServerErrorException.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(ORGANISASJONNUMMER, TEMA, TYPE_ORGANISASJON), HentMottakerOgAdresseResponse.class));

		assertEquals(INTERNAL_SERVER_ERROR, e.getStatusCode());
	}

	@Test
	public void shouldReturnNotFoundWhenMottakerErUkjentbostedFailsFunctionalNotFound() {
		postPdlGraphql(OK.value(), "pdl/ukjentbosted.json");

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(PERSON_IDENT, TEMA, TYPE_PERSON), HentMottakerOgAdresseResponse.class),
				"Test did not throw exception");

		assertEquals(NOT_FOUND, e.getStatusCode());
	}

	@Test
	public void shouldReturnUnauthorizedWhenPDLFailsFunctionalInvalidSecurityToken() {
		postPdlGraphql(OK.value(), "pdl/unauthenticated-error-response.json");

		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplateNoHeader.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequestNoToken(), HentMottakerOgAdresseResponse.class),
				"Test did not throw exception");

		assertEquals(UNAUTHORIZED, e.getStatusCode());
	}

	@Test
	public void shouldReturnInternalServerErrorWhenPDLFailsTechnical() {
		postPdlGraphqlWithErrorResponse(INTERNAL_SERVER_ERROR.value());

		HttpServerErrorException e = assertThrows(HttpServerErrorException.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(PERSON_IDENT, TEMA, TYPE_PERSON), HentMottakerOgAdresseResponse.class),
				"Test did not throw exception");

		assertEquals(INTERNAL_SERVER_ERROR, e.getStatusCode());
	}

	@ParameterizedTest
	@MethodSource
	public void shouldReturnBadRequestForInvalidInput(String ident, String type, String feilmelding) {
		HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
				() -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest(ident, TEMA, type), HentMottakerOgAdresseResponse.class));

		assertEquals(BAD_REQUEST, e.getStatusCode());
		assertTrue(e.getMessage().contains(feilmelding));
	}

	private static Stream<Arguments> shouldReturnBadRequestForInvalidInput() {
		return Stream.of(
				Arguments.of(PERSON_IDENT, "FESDASd", "Mottakertype var FESDASd. Det må være PERSON eller ORGANISASJON."),
				Arguments.of(null, TYPE_PERSON, "Identifikator kan ikke være null"),
				Arguments.of(ORGANISASJONNUMMER + "abc", TYPE_PERSON, "Identifikator kan kun bestå av tall"),
				Arguments.of(PERSON_IDENT, null, "Mottakertype kan ikke være null")
		);
	}

	private void assertPersonAdresse(HentMottakerOgAdresseResponse response) {
		assertEquals(ADRESSENAVN_1, response.getAdresse().getAdresselinje1());
		assertNull(response.getAdresse().getAdresselinje2());
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
		assertEquals("Postboks 343  Skøyen", response.getAdresse().getAdresselinje1());
		assertNull(response.getAdresse().getAdresselinje2());
		assertNull(response.getAdresse().getAdresselinje3());
		assertEquals(LANDKODE_NORGE, response.getAdresse().getLandkode());
		assertEquals("0213", response.getAdresse().getPostnummer());
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

	private HttpEntity<HentMottakerOgAdresseRequest> createRequest(String ident, String tema, String type) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(token("Treg002IT"));

		HentMottakerOgAdresseRequest hentMottakerOgAdresseRequest = getHentMottakerOgAdresseRequest(ident, tema, type);

		return new HttpEntity<>(hentMottakerOgAdresseRequest, headers);
	}

	private HttpEntity<HentMottakerOgAdresseRequest> createRequestNoToken() {
		HttpHeaders headers = new HttpHeaders();

		HentMottakerOgAdresseRequest hentMottakerOgAdresseRequest = getHentMottakerOgAdresseRequest("0102030405", "PEN", TYPE_PERSON);

		return new HttpEntity<>(hentMottakerOgAdresseRequest, headers);
	}

	private static HentMottakerOgAdresseRequest getHentMottakerOgAdresseRequest(String ident, String tema, String type) {
		return HentMottakerOgAdresseRequest.builder()
				.identifikator(ident)
				.tema(tema)
				.type(type).build();
	}

}
