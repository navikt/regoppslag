package no.nav.regoppslag.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.nav.regoppslag.common.ValiderOgKompletterBrevdataRequest;
import no.nav.regoppslag.common.ValiderOgKompletterBrevdataResponse;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.treg001.KompletterBrevdataService;
import no.nav.regoppslag.treg002.HentMottakerOgAdresseService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**TODO gjør om denne testen til en comp-test. Dvs fjerne mock av service-laget, og mock ut registrene i stedet. Få registrene til å kaste tekniske feil og funksjonelle feil eller returnere med fungerende oppsett.
 * @author Jarl Øystein Samseth, Visma Consulting
 */
public class RegisteroppslagRestControllerTest {
	private ValiderOgKompletterBrevdataRequest request;
	private ValiderOgKompletterBrevdataResponse response;
	private String brevdata = "<ole>brumm</ole>";
	private String brevdataUtfylt = "<ole>brumm</ole>";
	KompletterBrevdataService kompletterBrevdataService = mock(KompletterBrevdataService.class);
	HentMottakerOgAdresseService hentMottakerOgAdresseService = mock(HentMottakerOgAdresseService.class);
	RegisteroppslagRestController registeroppslagRestController = new RegisteroppslagRestController(kompletterBrevdataService,hentMottakerOgAdresseService);

	@Before
	public void setUp() throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		request = ValiderOgKompletterBrevdataRequest.builder().dokumentTypeId("123").brevdata(brevdata).build();
		response = ValiderOgKompletterBrevdataResponse.builder().brevdata(brevdataUtfylt).build();
		when(kompletterBrevdataService.hentBrevdataFraRegistre(request)).thenReturn(response);
	}
	
	@Test
	public void shouldGetKomplettBrevdata() throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		ValiderOgKompletterBrevdataResponse actualResponse = registeroppslagRestController.validerOgKompletterBrevdata(request);
		assertEquals(brevdata, actualResponse.getBrevdata());
		Mockito.verify(kompletterBrevdataService, Mockito.times(1)).hentBrevdataFraRegistre(any());
	}
	
	/** HVIS feil kastes, så skal feilmeldingene returneres i REST-responsen */
	//TODO Skriv testen
}