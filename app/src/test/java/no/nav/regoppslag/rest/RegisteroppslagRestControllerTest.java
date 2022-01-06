package no.nav.regoppslag.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.nav.regoppslag.api.HentMottakerOgAdresseRequest;
import no.nav.regoppslag.api.HentMottakerOgAdresseResponse;
import no.nav.regoppslag.api.KompletterBrevdataRequest;
import no.nav.regoppslag.api.KompletterBrevdataResponse;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.treg001.KompletterBrevdataService;
import no.nav.regoppslag.treg002.HentMottakerOgAdresseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
public class RegisteroppslagRestControllerTest {
	private KompletterBrevdataRequest request;
	private HentMottakerOgAdresseResponse responseMogA;
	private String brevdata = "<ole>brumm</ole>";
	HentMottakerOgAdresseRequest mottakerOgAdresseRequest = mock(HentMottakerOgAdresseRequest.class);
	KompletterBrevdataService kompletterBrevdataService = mock(KompletterBrevdataService.class);
	HentMottakerOgAdresseService hentMottakerOgAdresseService = mock(HentMottakerOgAdresseService.class);
	RegisteroppslagRestController registeroppslagRestController = new RegisteroppslagRestController(kompletterBrevdataService,hentMottakerOgAdresseService);

	@BeforeEach
	public void setUp() throws RegOppslagSecurityException {
		request = KompletterBrevdataRequest.builder().dokumentTypeId("123").brevdata(brevdata).build();
		String brevdataUtfylt = "<ole>brumm</ole>";
		KompletterBrevdataResponse response = KompletterBrevdataResponse.builder()
				.brevdata(brevdataUtfylt)
				.build();
		when(kompletterBrevdataService.hentBrevdataFraRegistre(request)).thenReturn(response);
		when(hentMottakerOgAdresseService.hentMottakerOgAdresseInfo(mottakerOgAdresseRequest)).thenReturn(responseMogA);
	}
	
	@Test
	public void shouldGetKomplettBrevdata() throws RegOppslagSecurityException {
		KompletterBrevdataResponse actualResponse = registeroppslagRestController.kompletterBrevdata(request);
		assertEquals(brevdata, actualResponse.getBrevdata());
		Mockito.verify(kompletterBrevdataService, Mockito.times(1)).hentBrevdataFraRegistre(any());
	}

	@Test
	public void shouldGetHentMottakerOgAdresse() throws RegOppslagSecurityException {
		HentMottakerOgAdresseResponse actualResponse = registeroppslagRestController.hentMottakerOgAdresse(mottakerOgAdresseRequest);
		assertEquals(responseMogA, actualResponse);
		Mockito.verify(hentMottakerOgAdresseService, Mockito.times(1)).hentMottakerOgAdresseInfo(any(HentMottakerOgAdresseRequest.class));
	}

}