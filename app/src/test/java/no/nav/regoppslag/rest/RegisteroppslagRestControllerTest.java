package no.nav.regoppslag.rest;

import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.treg001.KompletterBrevdataRequest;
import no.nav.regoppslag.treg001.KompletterBrevdataResponse;
import no.nav.regoppslag.treg001.KompletterBrevdataService;
import no.nav.regoppslag.treg002.HentMottakerOgAdresseRequest;
import no.nav.regoppslag.treg002.HentMottakerOgAdresseResponse;
import no.nav.regoppslag.treg002.HentMottakerOgAdresseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RegisteroppslagRestControllerTest {

	private static final String BREVDATA = "<ole>brumm</ole>";
	private static final String FNR = "12345678901";
	private static final String NAVN = "Anders Andersen";

	@Mock
	KompletterBrevdataService kompletterBrevdataService;

	@Mock
	HentMottakerOgAdresseService hentMottakerOgAdresseService;

	@InjectMocks
	RegisteroppslagRestController registeroppslagRestController;

	@Test
	public void shouldGetKomplettBrevdata() throws RegOppslagSecurityException {
		var request = createKompletterBrevdataRequest();
		var response = createKompletterBrevdataResponse(BREVDATA);
		when(kompletterBrevdataService.hentBrevdataFraRegistre(request)).thenReturn(response);

		KompletterBrevdataResponse actualResponse = registeroppslagRestController.kompletterBrevdata(request);

		assertEquals(BREVDATA, actualResponse.getBrevdata());
		verify(kompletterBrevdataService, times(1)).hentBrevdataFraRegistre(any());
	}

	@Test
	public void shouldGetHentMottakerOgAdresse() throws RegOppslagSecurityException {
		var request = createHentMottakerOgAdresseRequest();
		var response = createHentMottakerOgAdresseResponse();
		when(hentMottakerOgAdresseService.hentMottakerOgAdresseInfo(request)).thenReturn(response);

		HentMottakerOgAdresseResponse actualResponse = registeroppslagRestController.hentMottakerOgAdresse(request);

		assertEquals(response, actualResponse);
		verify(hentMottakerOgAdresseService, times(1)).hentMottakerOgAdresseInfo(any(HentMottakerOgAdresseRequest.class));
	}

	private static HentMottakerOgAdresseResponse createHentMottakerOgAdresseResponse() {
		return HentMottakerOgAdresseResponse.builder()
				.identifikator(FNR)
				.navn(NAVN)
				.adresse(null)
				.build();
	}

	private static HentMottakerOgAdresseRequest createHentMottakerOgAdresseRequest() {
		return HentMottakerOgAdresseRequest.builder()
				.identifikator("12345678901")
				.type("PERSON")
				.tema("FOR")
				.build();
	}

	private static KompletterBrevdataResponse createKompletterBrevdataResponse(String brevdata) {
		return KompletterBrevdataResponse.builder()
				.brevdata(brevdata)
				.build();
	}

	private static KompletterBrevdataRequest createKompletterBrevdataRequest() {
		return KompletterBrevdataRequest.builder()
				.dokumentTypeId("123")
				.build();
	}

}