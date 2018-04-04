package no.nav.regoppslag.itest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static no.nav.regoppslag.util.TestUtil.classpathToString;
import static no.nav.regoppslag.util.TestUtil.resourceUrlToString;
import static org.junit.Assert.assertEquals;

import com.google.common.io.Resources;
import no.nav.regoppslag.common.ValiderOgKompletterBrevdataRequest;
import no.nav.regoppslag.common.ValiderOgKompletterBrevdataResponse;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import java.net.URL;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */

public class Treg001IT extends AbstractIT {

	private final String DOKUMENTTYPEID = "123";


	private URL brevdataResponse_URL = Resources.getResource("__files/treg001/treg001_full_response.xml");
	private URL brevdataResponseOrg_URL = Resources.getResource("__files/treg001/treg001_full_response_orgv4.xml");
	private String expectedBrevdataFerdigUtfylt = resourceUrlToString(brevdataResponse_URL);
	private String expectedBrevdataFerdigUtfyltOrg = resourceUrlToString(brevdataResponseOrg_URL);

	private ValiderOgKompletterBrevdataRequest request = createRequest("__files/treg001/treg001_full_request.xml");
	private ValiderOgKompletterBrevdataRequest requestOrg = createRequest("__files/treg001/treg001_full_request_orgv4.xml");


	@Before
	public void setup() {
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
	public void shouldGetKomplettBrevdataPerson() throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		ValiderOgKompletterBrevdataResponse actualResponse = registeroppslagRestController.validerOgKompletterBrevdata(request);
		assertEquals(expectedBrevdataFerdigUtfylt, actualResponse.getBrevdata());
	}

	/**
	 * Komplertterer fullt brevdatasett der mottaker er organisasjon
	 */
	@Test
	public void shouldGetKomplettBrevdataOrg() throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		ValiderOgKompletterBrevdataResponse actualResponse = registeroppslagRestController.validerOgKompletterBrevdata(requestOrg);
		assertEquals(expectedBrevdataFerdigUtfyltOrg, actualResponse.getBrevdata());
	}


	/**
	 * Testbetingelser:
	 * -HVIS det oppstår en funksjonell feil for   et brevdataelement i en berikerplugin SÅ oppdater feillogg funksjonelle feil   OG fortsett til neste brevdataelement
	 * - HVIS det er opprettet en feillogg funksjonelle feil SÅ SKAL loggen returneres
	 */
	@Test
	public void shouldThrowFunctionalExceptionFromOrgPlugin() throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		//Stub web services:
		stubFor(post("/VIRKSOMHET_ORGANISASJON_V4")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/organisasjonv4/organisasjonv4_orgIkkeFunnet.xml"))); //mottakerPlugin

		exception.expect(RegOppslagFunctionalException.class);
		exception.expectMessage("Ingen organisasjon ble funnet med orgnr: 111111111");
		registeroppslagRestController.validerOgKompletterBrevdata(requestOrg);
	}

	@Test
	public void shouldThrowFunctionalExceptionFromPersonPlugin() throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		//Stub web services:
		stubFor(post("/VIRKSOMHET_PERSON_V3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/personV3/hentPerson-FunksjonellFeil-PersonIkkeFunnet-responsebody.xml"))); //mottakerPlugin

		exception.expect(RegOppslagFunctionalException.class);
		exception.expectMessage("PersonV3.hentPerson fant ikke person med ident:20096828390, message=Ingen forekomster funnet");
		registeroppslagRestController.validerOgKompletterBrevdata(request);
	}

	@Test
	public void shouldThrowFunctionalExceptionFromNorgPlugins() throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		//Stub web services:
		stubFor(post("/VIRKSOMHET_ORGANISASJONENHETKONTAKTINFORMASJON_V1")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/norg/hentEnhet-FunksjonellFeil-EnhetIkkeFunnet.xml"))); //mottakerPlugin

		exception.expect(RegOppslagFunctionalException.class);
		exception.expectMessage("Kunne ikke finne enhet. enhetId=0136");
		registeroppslagRestController.validerOgKompletterBrevdata(request);
	}

	private ValiderOgKompletterBrevdataRequest createRequest(String path) {
		return ValiderOgKompletterBrevdataRequest.builder()
				.dokumentTypeId(DOKUMENTTYPEID)
				.brevdata(classpathToString(path))
				.build();
	}
}