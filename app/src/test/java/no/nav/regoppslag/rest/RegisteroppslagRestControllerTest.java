package no.nav.regoppslag.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.service.RegOppslagService;
import no.nav.regoppslag.treg001.RegOppslagRequestTo;
import no.nav.regoppslag.treg001.RegOppslagResponseTo;
import no.nav.regoppslag.xmlenricher.exceptions.MissingPluginException;
import no.nav.regoppslag.xmlenricher.exceptions.MultiExceptionHolder;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import javax.xml.xpath.XPathExpressionException;

/**TODO gjør om denne testen til en comp-test (i-test). Dvs fjerne mock av service-laget, og mock ut registrene i stedet. Få registrene til å kaste tekniske feil og funksjonelle feil eller returnere med fungerende oppsett.
 * @author Jarl Øystein Samseth, Visma Consulting
 */
public class RegisteroppslagRestControllerTest {
	private RegOppslagRequestTo request;
	private RegOppslagResponseTo response;
	private String brevdata = "<ole>brumm</ole>";
	private String brevdataUtfylt = "<ole>brumm</ole>";
	RegOppslagService regOppslagService = mock(RegOppslagService.class);
	RegisteroppslagRestController registeroppslagRestController = new RegisteroppslagRestController(regOppslagService);
	ExpectedException exception = ExpectedException.none();

	@Before
	public void setUp() throws MultiExceptionHolder, RegOppslagFunctionalException, RegOppslagTechnicalException {
		request = RegOppslagRequestTo.builder().dokumentTypeId("123").brevdata(brevdata).build();
		response = RegOppslagResponseTo.builder().brevdata(brevdataUtfylt).build();
		when(regOppslagService.hentBrevdataFraRegistre(request)).thenReturn(response);
	}
	
	@Test
	public void shouldGetKomplettBrevdata() throws MultiExceptionHolder, XPathExpressionException, MissingPluginException, RegOppslagFunctionalException, RegOppslagTechnicalException {
		RegOppslagResponseTo actualResponse = registeroppslagRestController.validerOgKompletterBrevdata(request);
		assertEquals(brevdata, actualResponse.getBrevdata());
		Mockito.verify(regOppslagService, Mockito.times(1)).hentBrevdataFraRegistre(any());
	}
	
	/** TODO HVIS feil kastes, så skal de sendes til bruker
	 * Bør disse skrives som restTemplate-test? (i stedet for postman?) */
	
	
	/** HVIS Teknisk og funksjonell feil kastes, så skal funksjonell feil kastes til bruker */
	@Test
	@Ignore("under arbeid")
	public void shouldHandleMultiExceptionHolder() throws RegOppslagFunctionalException, RegOppslagTechnicalException, MultiExceptionHolder {
		exception.expect(RegOppslagFunctionalException.class);
		MultiExceptionHolder exceptionHolder = new MultiExceptionHolder("registeroppslag feilet");
		//TODO lag exceptions.
		when(regOppslagService.hentBrevdataFraRegistre(any())).thenThrow(exceptionHolder);
		registeroppslagRestController.validerOgKompletterBrevdata(request);
	}
	
}