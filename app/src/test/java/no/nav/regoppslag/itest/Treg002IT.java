package no.nav.regoppslag.itest;

import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.regoppslag.api.HentMottakerOgAdresseRequest;
import no.nav.regoppslag.api.HentMottakerOgAdresseResponse;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Disabled
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
        HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
                () -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("ORGANISASJON"), HentMottakerOgAdresseResponse.class),
                "Test did not throw exception");

        assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());

    }

    @Test
    public void shouldThrowIfPersonIsMissingAdresse() {
        stubFor(post("/VIRKSOMHET_PERSONV3")
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                        .withBodyFile("treg001/personV3/hentperson-mangler_adresse.xml"))); //mottakerPlugin
        HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class,
                () -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("PERSON"), HentMottakerOgAdresseResponse.class),
                "Should throw technical Exception");

        verify(1, postRequestedFor(urlEqualTo("/VIRKSOMHET_PERSONV3")));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }

    @Test
    public void shouldThrowIfPersonIsDoedAndMissingAdresse() {
        stubFor(post("/VIRKSOMHET_PERSONV3")
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                        .withBodyFile("treg002/personV3/hentperson-dod_mangler_adresse.xml"))); //mottakerPlugin
        HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class,
                () -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("PERSON"), HentMottakerOgAdresseResponse.class),
                "Should throw technical Exception");
        verify(1, postRequestedFor(urlEqualTo("/VIRKSOMHET_PERSONV3")));
        assertEquals(HttpStatus.GONE, e.getStatusCode());
        assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Mottaker er registrert som død og har gjeldendePostadressetype=UKJENT_ADRESSE"));

    }

    @Test
    public void shouldThrowWhenOrganisasjonV4FailsFunctionalNotFound() {
        stubFor(post("/ORGANISASJON_V4")
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                        .withBodyFile("treg002/organisasjonv4/organisasjonv4-ikkefunnet-response.xml")));

        HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class,
                () -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("ORGANISASJON"), HentMottakerOgAdresseResponse.class),
                "Test did not throw exception");

        assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
        assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Nav enhet finnes ikke for enhetNr=0102030405, message=Ingen organisasjon ble funnet med orgnr: 889640732"));

    }

    @Test
    public void shouldThrowWhenOrganisasjonV4FailsTechnical() {
        stubFor(post("/ORGANISASJON_V4")
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                        .withBodyFile("treg002/organisasjonv4/organisasjonv4-tekniskfeil-response.xml")));
        HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class,
                () -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("ORGANISASJON"), HentMottakerOgAdresseResponse.class),
                "Test did not throw exception");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatusCode());
        assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Noe gikk galt i kall til OrganisasjonV4.hentOrganisasjon for enhetNr=0102030405"));

    }


    @Test
    public void shouldThrowWhenPersonV3FailsFunctionalNotFound() {
        stubFor(post("/VIRKSOMHET_PERSONV3")
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                        .withBodyFile("treg002/personV3/hentPerson-FunksjonellFeil-PersonIkkeFunnet-responsebody.xml")));

        HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class,
                () -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("PERSON"), HentMottakerOgAdresseResponse.class),
                "Test did not throw exception");

        assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
        assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("PersonV3.hentPerson fant ikke person med ident=0102030405, message=Ingen forekomster funnet"));

    }

    @Test
    public void shouldThrowWhenPersonV3FailsSecurityErrorNoAccess() {
        stubFor(post("/VIRKSOMHET_PERSONV3")
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                        .withBodyFile("treg002/personV3/hentPerson-FunksjonellFeil-SikkerhetsBegrensning-responsebody.xml")));

        HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class,
                () -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("PERSON"), HentMottakerOgAdresseResponse.class),
                "Test did not throw exception");

        assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
        assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("PersonV3.hentPerson feiler på grunn av sikkerhetsbegresning. Message=Ingen tilgang"));

    }

    @Test
    public void shouldThrowWhenPersonV3FailsFunctionalInvalidSecurityToken() {
        stubFor(post("/VIRKSOMHET_PERSONV3")
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                        .withBodyFile("treg002/personV3/hentPerson-FunksjonellFeil-SikkerhetsBegrensning-responsebody.xml")));

        HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class,
                () -> restTemplateNoHeader.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("PERSON"), HentMottakerOgAdresseResponse.class),
                "Test did not throw exception");

        assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
        assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Fant ingen SAML assertion token i sikkerhetskontekst. SAML assertion token kreves for å kunne kalle PersonV3"));
    }

    @Test
    public void shouldThrowWhenPersonV3FailsTechnical() {
        stubFor(post("/VIRKSOMHET_PERSONV3")
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                        .withBodyFile("treg002/personV3/hentPerson-Tecnical-responsebody.xml")));

        HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class,
                () -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("PERSON"), HentMottakerOgAdresseResponse.class),
                "Test did not throw exception");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatusCode());
    }

    @Test
    public void shouldThrowWhenTypeIsIncorrect() {

        HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class,
                () -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, createRequest("FESDASd"), HentMottakerOgAdresseResponse.class),
                "Test did not throw exception");

        assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
        assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Mottakertype var FESDASd. Det må være PERSON eller ORGANISASJON."));

    }

    @Test
    public void shouldThrowWhenIdentifikatorIsEmpty() {
        HentMottakerOgAdresseRequest request = createRequest("PERSON");
        request.setIdentifikator(null);
        HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class,
                () -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, request, HentMottakerOgAdresseResponse.class),
                "Test did not throw exception");

        assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
        assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Identifikator kan ikke være null"));
    }

    @Test
    public void shouldThrowWhenTypeIsEmpty() {
        HentMottakerOgAdresseRequest request = createRequest("PERSON");
        request.setType(null);
        HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class,
                () -> restTemplate.postForObject(LOCAL_ENDPOINT_URL + REST + HENT_MOTTAKEROGADRESSE_URI_PATH, request, HentMottakerOgAdresseResponse.class),
                "Test did not throw exception");

        assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
        assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("Mottakertype kan ikke være null"));

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
