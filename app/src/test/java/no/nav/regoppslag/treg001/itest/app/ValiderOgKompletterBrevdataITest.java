package no.nav.regoppslag.treg001.itest.app;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static no.nav.regoppslag.util.TestUtil.resourceUrlToString;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.common.io.Resources;
import no.nav.regoppslag.Application;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.rest.RegisteroppslagRestController;
import no.nav.regoppslag.treg001.RegOppslagRequest;
import no.nav.regoppslag.treg001.RegOppslagResponse;
import no.nav.regoppslag.treg001.itest.config.MockLdapTestConfig;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.net.URL;
import java.util.ArrayList;

/**
 * TODO gjør om denne testen til en comp-test. Dvs fjerne mock av service-laget, og mock ut registrene i stedet. Få registrene til å kaste tekniske feil og funksjonelle feil eller returnere med fungerende oppsett.
 *
 * @author Jarl Øystein Samseth, Visma Consulting
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MockLdapTestConfig.class,Application.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("itest")
public class ValiderOgKompletterBrevdataITest {
	private final String DOKUMENTTYPEID = "123";
	@Inject
	public RegisteroppslagRestController registeroppslagRestController;
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	@Mock
	private LdapTemplate ldapTemplate;
	
	private URL brevdataRequest_URL = Resources.getResource("__files/treg001/validerOgKompletterBrevdata_happypath_REST_requestcontent-brevdata.xml");
	private String brevdataUtfylt = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ole>brumm</ole>";
	private RegOppslagRequest request = RegOppslagRequest.builder()
			.dokumentTypeId(DOKUMENTTYPEID)
			.brevdata(resourceUrlToString(brevdataRequest_URL))
			.build();
	
	@Before
	public void setUp() {
		WireMock.reset();
		WireMock.resetAllRequests();
		
		stubOppslagAD();  //FIXME: MockLdapTestConfig sine bønner gjør ingen forskjell fra eller til. derfor er ldaptemplate = null når Skasbehandlerplugin.processElement blir kalt.
	}
	
	private void stubOppslagAD() {
		
		when(ldapTemplate.search(Matchers.<LdapQuery>any(), Matchers.<AttributesMapper<String>>any())).thenReturn(new ArrayList<String>() {{
			add("en vilkaarlig autentisert person");
		}});
	}
	
	
	/**
	 * Happypath: HVIS ufullstendig brevdata sendes inn, skal brevdata valideres og kompletteres med data fra registrene.
	 */
	@Test
	public void shouldGetKomplettBrevdata() throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		happypathStubs();
		RegOppslagResponse actualResponse = registeroppslagRestController.validerOgKompletterBrevdata(request);
		assertEquals(brevdataUtfylt, actualResponse.getBrevdata());
	}
	
	@Test(expected = RegOppslagTechnicalException.class)
	public void shouldThrowTechnicalExceptionFromPlugin() throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		registeroppslagRestController.validerOgKompletterBrevdata(request);
	}
	
	@Test
	public void shouldThrowFunctionalExceptionFromPlugin() throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		exception.expect(RegOppslagFunctionalException.class);
		exception.expectMessage("Person med fnr 010524042317 ikke funnet.");
		exception.expectMessage("Feil i SaksbehandlerPlugin: Fant ikke saksbehandlernavn");
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
		//Stub web services:
// TODO		stubFor(post("/DOKUMENTTYPEINFO_V3").willReturn()); //Brukes til hentDokumenttypeinfo for Spraak
//	TODO	stubFor(post("/VIRKSOMHET_ORGANISASJONENHETKONTAKTINFORMASJON_V1")
//				.withRequestBody(containing("hentKontaktinformasjonForEnhetBolkRequest"))
//				.willReturn());
//		stubFor(post("/VIRKSOMHET_ORGANISASJON_V4"))
		
		stubFor(post("/VIRKSOMHET_PERSON_V3")
				.withRequestBody(containing("hentPersonRequest"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/personV3/hentperson-happypath-responsebody.xml"))); //mottakerPlugin
	}
	
	
}