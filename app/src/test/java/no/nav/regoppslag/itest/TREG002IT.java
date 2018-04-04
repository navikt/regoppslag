package no.nav.regoppslag.itest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingXPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;

import com.github.tomakehurst.wiremock.common.Xml;
import no.nav.regoppslag.common.HentMottakerOgAdresseRequest;
import no.nav.regoppslag.common.HentMottakerOgAdresseResponse;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.HttpStatus;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class Treg002IT extends AbstractIT {
	
	
	@Before
	public void setUp(){
		
		stubFor(post("/STS")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("/felles/sts/sts_signature-responsebody.xml")));
		
		stubFor(post("/VIRKSOMHET_PERSON_V3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/personV3/hentperson-happypath-responsebody.xml")));
		
		stubFor(post("/VIRKSOMHET_ORGANISASJON_V4")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/organisasjonv4/organisasjonv4-happy.xml")));
	}
	
	@Test
	public void shouldGetMottakerAndAdresseForPerson() throws Exception{
		
		HentMottakerOgAdresseResponse response = registeroppslagRestController.hentMottakerOgAdresse(createRequest("PERSON"));
		assertAdresse(response);
		assertEquals(response.getIdentifikator(),"0102030405");
		assertEquals(response.getNavn(),"Geir Appleson");
		
		verify(postRequestedFor(urlMatching("/VIRKSOMHET_PERSON_V3")).withRequestBody(matchingXPath("//ident/text()", equalTo("0102030405"))));
		verify(postRequestedFor(urlMatching("/VIRKSOMHET_PERSON_V3")).withRequestBody(matchingXPath("//informasjonsbehov/text()", equalTo("adresse"))));
	}
	
	@Test
	public void shouldThrowWhenPersonV3FailsFunctionalNotFound() throws Exception{
		stubFor(post("/VIRKSOMHET_PERSON_V3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/personV3/hentPerson-FunksjonellFeil-PersonIkkeFunnet-responsebody.xml")));
		exception.expect(RegOppslagFunctionalException.class);
		exception.expectMessage("PersonV3.hentPerson fant ikke person med ident:0102030405, message=Ingen forekomster funnet");
		
		registeroppslagRestController.hentMottakerOgAdresse(createRequest("PERSON"));
		
	}
	
	@Test
	public void shouldThrowWhenPersonV3FailsFunctionalSecurityError() throws Exception{
		stubFor(post("/VIRKSOMHET_PERSON_V3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/personV3/hentPerson-FunksjonellFeil-SikkerhetsBegrensning-responsebody.xml")));
		exception.expect(RegOppslagFunctionalException.class);
		exception.expectMessage("PersonV3.hentPerson feiler på grunn av sikkerhetsbegresning for ident: 0102030405, message=Sikkerhetsfeil");
		
		registeroppslagRestController.hentMottakerOgAdresse(createRequest("PERSON"));
		
	}
	
	@Test
	public void shouldThrowWhenPersonV3FailsTechnical() throws Exception{
		stubFor(post("/VIRKSOMHET_PERSON_V3")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg002/personV3/hentPerson-Tecnical-responsebody.xml")));
		exception.expect(RegOppslagTechnicalException.class);
		exception.expectMessage("Teknisk feil: errorMsg=Noe gikk galt i kall til PersonV3.hentPerson for ident: 0102030405, message=Feil med server. Overbelastning?");
		
		registeroppslagRestController.hentMottakerOgAdresse(createRequest("PERSON"));
		
	}
	
	@Test
	public void shouldThrowWhenTypeIsIncorrect() throws Exception {
		
		exception.expect(RegOppslagFunctionalException.class);
		exception.expectMessage("Mottakertype var FESDASd. Det må være PERSON eller ORGANISASJON.");
		
		registeroppslagRestController.hentMottakerOgAdresse(createRequest("FESDASd"));
	}
	
	@Test
	public void shouldThrowWhenIdentifikatorIsEmpty() throws Exception {
		exception.expect(RegOppslagFunctionalException.class);
		exception.expectMessage("Identifikator kan ikke være null");
		
		HentMottakerOgAdresseRequest request = createRequest("PERSON");
		request.setIdentifikator(null);
		registeroppslagRestController.hentMottakerOgAdresse(request);
	}
	
	
	@Test
	public void shouldThrowWhenTypeIsEmpty() throws Exception {
		exception.expect(RegOppslagFunctionalException.class);
		exception.expectMessage("Mottakertype kan ikke være null");
		
		HentMottakerOgAdresseRequest request = createRequest("PERSON");
		request.setType(null);
		registeroppslagRestController.hentMottakerOgAdresse(request);
	}
	
	@Test
	public void shouldThrowWhenInputIsNull() throws Exception {
		exception.expect(RegOppslagFunctionalException.class);
		exception.expectMessage("Input body er null");
		
		registeroppslagRestController.hentMottakerOgAdresse(null);
	}
	
	
	@Test
	@Ignore
	public void shouldGetMottakerAndAdresseForOrganisasjon() throws Exception{
		
		HentMottakerOgAdresseResponse response = registeroppslagRestController.hentMottakerOgAdresse(createRequest("ORGANISASJON"));
		assertAdresse(response);
		assertEquals(response.getIdentifikator(),"0102030405");
		assertEquals(response.getNavn(),"Geir Appleson");
	}
	
	private void assertAdresse(HentMottakerOgAdresseResponse response){
		assertEquals(response.getAdresse().getAdresselinje1(), "Bak Gate 10");
		assertEquals(response.getAdresse().getAdresselinje2(), null);
		assertEquals(response.getAdresse().getAdresselinje3(), null);
		assertEquals(response.getAdresse().getLandkode(), "NO");
		assertEquals(response.getAdresse().getPostnummer(), "0350");
		assertEquals(response.getAdresse().getPoststed(), "OSLO");
	}
	
	private HentMottakerOgAdresseRequest createRequest(String type){
		return HentMottakerOgAdresseRequest.builder()
				.identifikator("0102030405")
				.type(type).build();
	}


	
	
}
