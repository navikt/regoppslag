package no.nav.regoppslag.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.service.RegOppslagService;
import no.nav.regoppslag.treg001.RegOppslagRequest;
import no.nav.regoppslag.treg001.RegOppslagResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**TODO gjør om denne testen til en comp-test. Dvs fjerne mock av service-laget, og mock ut registrene i stedet. Få registrene til å kaste tekniske feil og funksjonelle feil eller returnere med fungerende oppsett.
 * @author Jarl Øystein Samseth, Visma Consulting
 */
public class RegisteroppslagRestControllerTest {
	private RegOppslagRequest request;
	private RegOppslagResponse response;
	private String brevdata = "<ole>brumm</ole>";
	private String brevdataUtfylt = "<ole>brumm</ole>";
	RegOppslagService regOppslagService = mock(RegOppslagService.class);
	RegisteroppslagRestController registeroppslagRestController = new RegisteroppslagRestController(regOppslagService);

	@Before
	public void setUp() throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		request = RegOppslagRequest.builder().dokumentTypeId("123").brevdata(brevdata).build();
		response = RegOppslagResponse.builder().brevdata(brevdataUtfylt).build();
		when(regOppslagService.hentBrevdataFraRegistre(request)).thenReturn(response);
	}
	
	@Test
	public void shouldGetKomplettBrevdata() throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		RegOppslagResponse actualResponse = registeroppslagRestController.validerOgKompletterBrevdata(request);
		assertEquals(brevdata, actualResponse.getBrevdata());
		Mockito.verify(regOppslagService, Mockito.times(1)).hentBrevdataFraRegistre(any());
	}
	
	/** HVIS feil kastes, så skal feilmeldingene returneres i REST-responsen */
	//TODO Skriv testen
}