package no.nav.regoppslag.itest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static no.nav.regoppslag.consumer.ldap.LdapAdeoUserLookup.HENT_FULLT_NAVN;
import static no.nav.regoppslag.rest.RegisteroppslagRestController.KOMPLETTER_BREVDATA_URI_PATH;
import static no.nav.regoppslag.util.TestUtil.classpathToString;
import static no.nav.regoppslag.util.TestUtil.resourceUrlToString;
import static org.junit.Assert.assertEquals;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.common.io.Resources;
import no.nav.regoppslag.common.ValiderOgKompletterBrevdataRequest;
import no.nav.regoppslag.common.ValiderOgKompletterBrevdataResponse;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import java.net.URL;

/**
 *
 * @author Jarl Øystein Samseth, Visma Consulting
 */

public class Treg001IT extends AbstractIT{
	
	private final String DOKUMENTTYPEID = "123";
	
	
	private URL brevdataResponse_URL = Resources.getResource("__files/treg001/validerOgKompletterBrevdata_happypath_REST_responsebody.xml");
	private String expectedBrevdataFerdigUtfylt = resourceUrlToString(brevdataResponse_URL);
	
	private ValiderOgKompletterBrevdataRequest request = createRequest("__files/treg001/validerOgKompletterBrevdata_happypath_REST_requestcontent-brevdata.xml");
	

	
	
	/**
	 * Happypath testbetingelser:
	 * HVIS ufullstendig brevdata sendes inn, skal brevdata valideres og kompletteres med data fra registrene.
	 * - HVIS hentPerson ok SÅ skal output returners ihht tabell i behandlingssteg 4
	 * - HVIS operasjonen får treff på ident i LDAP SÅ skal verdien i navn utledes og returneres i XML
	 * - HVIS hentKontaktInformasjonForEnhetBolk går ok SÅ skal output returneres ihht tabell i behandlingssteg 3
	 * - HVIS hentKontaktInformasjonForEnhetBolk går ok SÅ skal output returners ihht tabell i behandlingssteg 3
	 * - HVIS brevdataelement støttes av   berikerplugin SÅ gjør kall mot beriker OG populer attributter
	 * 			(her testes følgende plugins ved request brevdata:
	 * 		--	mottakerPlugin ved attributtet "mottaker"
	 * 		--	SaksbehandlerPlugin ved "signerendeSaksbehandler"
	 * 		--	NavOrgenhetPlugin ved "kontaktinformasjon"
 * 				)
	 * - HVIS brevdataelementet IKKE er støttet av   en berikeplugin SÅ fortsett til neste brevdataelement (her testet ved attributtet signerendeBeslutter i request brevdata)
	 * - HVIS alle brevdataelementer er kontrollert mot beriker SÅ SKAL brevdata returneres
	 */
	@Test
	public void shouldGetKomplettBrevdata() throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		happypathStubs();
		ValiderOgKompletterBrevdataResponse actualResponse = registeroppslagRestController.validerOgKompletterBrevdata(request);
		assertEquals(expectedBrevdataFerdigUtfylt, actualResponse.getBrevdata());
	}
	
	/**Testbetingelser:
	 * - HVIS det oppstår en teknisk feil for et   brevdataelement i en berikerplugin SÅ oppdater feillogg teknisk feil OG   fortsett til neste brevdataelement
	 * - HVIS det er opprettet en feillogg   tekniske feil SÅ SKAL loggen returneres
	 */
	 @Test
	public void shouldThrowTechnicalExceptionFromPlugins() throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		//TODO hvordan kontrollert trigge tekniske feil? Utelate stubs slik at alle kall fra plugins gir timeoutException?
		exception.expect(RegOppslagTechnicalException.class);
		exception.expectMessage("TODO ");
		registeroppslagRestController.validerOgKompletterBrevdata(request);
	}
	
	/**Testbetingelser:
	 * -HVIS det oppstår en funksjonell feil for   et brevdataelement i en berikerplugin SÅ oppdater feillogg funksjonelle feil   OG fortsett til neste brevdataelement
	 * - HVIS det er opprettet en feillogg funksjonelle feil SÅ SKAL loggen returneres
	 */
	@Test
	public void shouldThrowFunctionalExceptionFromPlugins() throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		exception.expect(RegOppslagFunctionalException.class);
		exception.expectMessage("Person med fnr 010524042317 ikke funnet.");
		exception.expectMessage("Feil i SaksbehandlerPlugin: Fant ikke saksbehandlernavn");
		exception.expectMessage("TODO velg en feilmelding for hentkontaktinformasjon");
		functionalExceptionStubs();
		registeroppslagRestController.validerOgKompletterBrevdata(request);
	}
	
	private void functionalExceptionStubs() {
		stubFor(post("/VIRKSOMHET_PERSON_V3")
				.withRequestBody(containing("hentPersonRequest"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/personV3/hentPerson-FunksjonellFeil-PersonIkkeFunnet-responsebody.xml")));
	}
	
	private void happypathStubs() {
		//Stub REST services:
//		stubFor(post("/DOKUMENTTYPEINFO_V3/*").willReturn(aResponse().withStatus(HttpStatus.OK.value()).withBody("treg001/tkat020-dokumenttypeinfo-responsebody.json"))); //Brukes til hentDokumenttypeinfo for Spraak
		
		//Stub web services:
//		stubFor(post("/VIRKSOMHET_ORGANISASJONENHETKONTAKTINFORMASJON_V1")
//				.withRequestBody(containing("hentKontaktinformasjonForEnhetBolkRequest"))
//				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
//						.withBodyFile(""))); //TODO interceptor
		
		stubFor(post("/STS")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/sts_signature-responsebody.xml"))); //mottakerPlugin
		stubFor(post("/VIRKSOMHET_PERSON_V3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/personV3/hentperson-happypath-responsebody.xml"))); //mottakerPlugin
	}
	
	
	private ValiderOgKompletterBrevdataRequest createRequest(String path){
		return ValiderOgKompletterBrevdataRequest.builder()
				.dokumentTypeId(DOKUMENTTYPEID)
				.brevdata(classpathToString(path))
				.build();
	}
}