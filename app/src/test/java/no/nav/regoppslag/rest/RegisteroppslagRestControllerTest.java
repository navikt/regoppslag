package no.nav.regoppslag.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.nav.regoppslag.common.HentMottakerOgAdresseRequest;
import no.nav.regoppslag.common.HentMottakerOgAdresseResponse;
import no.nav.regoppslag.common.ValiderOgKompletterBrevdataRequest;
import no.nav.regoppslag.common.ValiderOgKompletterBrevdataResponse;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.treg001.KompletterBrevdataService;
import no.nav.regoppslag.treg002.HentMottakerOgAdresseService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
public class RegisteroppslagRestControllerTest {
	private ValiderOgKompletterBrevdataRequest request;
	private ValiderOgKompletterBrevdataResponse response;
	private HentMottakerOgAdresseResponse responseMogA;
	private String brevdata = "<ole>brumm</ole>";
	private String brevdataUtfylt = "<ole>brumm</ole>";
	HentMottakerOgAdresseRequest mottakerOgAdresseRequest = mock(HentMottakerOgAdresseRequest.class);
	KompletterBrevdataService kompletterBrevdataService = mock(KompletterBrevdataService.class);
	HentMottakerOgAdresseService hentMottakerOgAdresseService = mock(HentMottakerOgAdresseService.class);
	RegisteroppslagRestController registeroppslagRestController = new RegisteroppslagRestController(kompletterBrevdataService,hentMottakerOgAdresseService);

	@Before
	public void setUp() throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		request = ValiderOgKompletterBrevdataRequest.builder().dokumentTypeId("123").brevdata(brevdata).build();
		response = ValiderOgKompletterBrevdataResponse.builder().brevdata(brevdataUtfylt).build();
		when(kompletterBrevdataService.hentBrevdataFraRegistre(request)).thenReturn(response);
		when(hentMottakerOgAdresseService.hentMottakerOgAdresseInfo(mottakerOgAdresseRequest)).thenReturn(responseMogA);
	}
	
	@Test
	public void shouldGetKomplettBrevdata() throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		ValiderOgKompletterBrevdataResponse actualResponse = registeroppslagRestController.validerOgKompletterBrevdata(request);
		assertEquals(brevdata, actualResponse.getBrevdata());
		Mockito.verify(kompletterBrevdataService, Mockito.times(1)).hentBrevdataFraRegistre(any());
	}

	@Test
	public void shouldGetHentMottakerOgAdresse() throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		HentMottakerOgAdresseResponse actualResponse = registeroppslagRestController.hentMottakerOgAdresse(mottakerOgAdresseRequest);
		assertEquals(responseMogA, actualResponse);
		Mockito.verify(hentMottakerOgAdresseService, Mockito.times(1)).hentMottakerOgAdresseInfo(any(HentMottakerOgAdresseRequest.class));
	}

}