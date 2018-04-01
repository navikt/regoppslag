package no.nav.regoppslag.treg001.itest.app;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static no.nav.regoppslag.consumer.ldap.LdapAdeoUserLookup.HENT_FULLT_NAVN;
import static no.nav.regoppslag.util.TestUtil.resourceUrlToString;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.common.io.Resources;
import no.nav.regoppslag.Application;
import no.nav.regoppslag.common.ValiderOgKompletterBrevdataRequest;
import no.nav.regoppslag.common.ValiderOgKompletterBrevdataResponse;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.rest.RegisteroppslagRestController;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.net.URL;
import java.util.ArrayList;

/**
 * TODO gjør om denne testen til en comp-test. Dvs fjerne mock av service-laget, og mock ut registrene i stedet. Få registrene til å kaste tekniske feil og funksjonelle feil eller returnere med fungerende oppsett.
 *
 * @author Jarl Øystein Samseth, Visma Consulting
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {Application.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("itest")
@Ignore
public class ValiderOgKompletterBrevdataITest {
	private final String DOKUMENTTYPEID = "123";
	@Inject
	public RegisteroppslagRestController registeroppslagRestController;
	
	@Inject
	CacheManager cacheManager;
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	@Mock
	private LdapTemplate ldapTemplate;
	
	private URL brevdataRequest_URL = Resources.getResource("__files/treg001/validerOgKompletterBrevdata_happypath_REST_requestcontent-brevdata.xml");
	private URL brevdataResponse_URL = Resources.getResource("__files/treg001/validerOgKompletterBrevdata_happypath_REST_responsebody.xml");
	private String expectedBrevdataFerdigUtfylt = resourceUrlToString(brevdataResponse_URL);
	private ValiderOgKompletterBrevdataRequest request = ValiderOgKompletterBrevdataRequest.builder()
			.dokumentTypeId(DOKUMENTTYPEID)
			.brevdata(resourceUrlToString(brevdataRequest_URL))
			.build();
	
	@Before
	public void setUp() {
		WireMock.reset();
		WireMock.resetAllRequests();

		//TODO se på disse cachegreiene, foreløpig bare kommentert ut
		clearCachene();
		cacheManager.getCache(HENT_FULLT_NAVN).put("Z991006","en vilkaarlig saksbehandler");
		stubOppslagLDAP();
//		stubFor(post("/STS").willReturn(aResponse().withBody("treg001/sts_signature-responsebody.xml")));
	}
	
	private void clearCachene() {
		cacheManager.getCacheNames().forEach(names -> cacheManager.getCache(names).clear());
	}
	
	private void stubOppslagLDAP() {
		when(ldapTemplate.search(Matchers.<LdapQuery>any(), Matchers.<AttributesMapper<String>>any())).thenReturn(new ArrayList<String>() {{
			add("en vilkaarlig autentisert person");
		}});
	}
	
	
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
		stubOppslagLDAP();
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
		
		stubFor(post("/VIRKSOMHET_PERSON_V3")
				.withRequestBody(containing("hentPersonRequest"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("treg001/personV3/hentperson-happypath-responsebody.xml"))); //mottakerPlugin
	}
	
}