package no.nav.regoppslag.consumer.norg2;

import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.binding.HentKontaktinformasjonForEnhetBolkUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.binding.OrganisasjonEnhetKontaktinformasjonV1;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.feil.UgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.FeiletEnhet;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.Organisasjonsenhet;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.meldinger.HentKontaktinformasjonForEnhetBolkRequest;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.meldinger.HentKontaktinformasjonForEnhetBolkResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class OrganisasjonEnhetKontaktinformasjonV1ConsumerTest {
	private OrganisasjonEnhetKontaktinformasjonV1 organisasjonEnhetKontaktinformasjonV1 = mock(OrganisasjonEnhetKontaktinformasjonV1.class);
	private MicrometerMetrics metrics = mock(MicrometerMetrics.class);
	private OrganisasjonEnhetKontaktinformasjonV1Consumer organisasjonEnhetKontaktinformasjonV1Consumer =
			new OrganisasjonEnhetKontaktinformasjonV1Consumer(organisasjonEnhetKontaktinformasjonV1, metrics);

	private final String ENHET_NR = "1234";
	private final String ENHET_NAVN = "NAV Husnes";

	@Test
	public void shouldHentEnhetNavn() throws Exception {
		when(organisasjonEnhetKontaktinformasjonV1.hentKontaktinformasjonForEnhetBolk(any(HentKontaktinformasjonForEnhetBolkRequest.class))).thenReturn(defaultResponse());

		Organisasjonsenhet enhet = organisasjonEnhetKontaktinformasjonV1Consumer.hentKontaktinformasjonForEnhet(ENHET_NR);

		assertEquals(ENHET_NAVN, enhet.getEnhetNavn());
	}

	@Test
	public void shouldReturnNullWhenNameNotInResponse() throws Exception {
		HentKontaktinformasjonForEnhetBolkResponse response = defaultResponse();
		response.getEnhetListe().get(0).setEnhetNavn(null);
		when(organisasjonEnhetKontaktinformasjonV1.hentKontaktinformasjonForEnhetBolk(any(HentKontaktinformasjonForEnhetBolkRequest.class))).thenReturn(response);

		Organisasjonsenhet enhet = organisasjonEnhetKontaktinformasjonV1Consumer.hentKontaktinformasjonForEnhet(ENHET_NR);

		assertNull(enhet.getEnhetNavn());
	}

	@Test
	public void shouldReturnNullWhenFeilEnhetListe() throws Exception {
		HentKontaktinformasjonForEnhetBolkResponse response = defaultResponse();
		response.getEnhetListe().clear();
		FeiletEnhet feiletEnhet = new FeiletEnhet();
		feiletEnhet.setEnhetId(ENHET_NR);
		feiletEnhet.setFeilmelding("Fant ikke enheten");
		response.getFeiletEnhetListe().add(0, feiletEnhet);
		when(organisasjonEnhetKontaktinformasjonV1.hentKontaktinformasjonForEnhetBolk(any(HentKontaktinformasjonForEnhetBolkRequest.class))).thenReturn(response);
		RegOppslagFunctionalException e = assertThrows(RegOppslagFunctionalException.class,
				() -> organisasjonEnhetKontaktinformasjonV1Consumer.hentKontaktinformasjonForEnhet(ENHET_NR), "Nav enhet finnes ikke for enhetNr=" + ENHET_NR);

	}

	@Test
	public void shouldThrowFunctionalErrorWhenUgyldigInput() throws Exception {
		when(organisasjonEnhetKontaktinformasjonV1.hentKontaktinformasjonForEnhetBolk(any(HentKontaktinformasjonForEnhetBolkRequest.class))).thenThrow(new HentKontaktinformasjonForEnhetBolkUgyldigInput("Ugyldig input", new UgyldigInput()));

		RegOppslagFunctionalException e = assertThrows(RegOppslagFunctionalException.class,
				() -> organisasjonEnhetKontaktinformasjonV1Consumer.hentKontaktinformasjonForEnhet(ENHET_NR), "Ugyldig input");
	}

	@Test
	public void shouldThrowTechnicalErrorErrorWhenRuntimeException() throws Exception {
		when(organisasjonEnhetKontaktinformasjonV1.hentKontaktinformasjonForEnhetBolk(any(HentKontaktinformasjonForEnhetBolkRequest.class))).thenThrow(new RuntimeException());

		RegOppslagTechnicalException e = assertThrows(RegOppslagTechnicalException.class,
				() -> organisasjonEnhetKontaktinformasjonV1Consumer.hentKontaktinformasjonForEnhet(ENHET_NR), "Noe gikk galt i kall til Norg for enhetNr=" + ENHET_NR);

	}

	private HentKontaktinformasjonForEnhetBolkResponse defaultResponse() {
		return createResponse(ENHET_NAVN);
	}

	private HentKontaktinformasjonForEnhetBolkResponse createResponse(String enhetNavn) {
		HentKontaktinformasjonForEnhetBolkResponse response = new HentKontaktinformasjonForEnhetBolkResponse();
		Organisasjonsenhet organisasjonsenhet = new Organisasjonsenhet();
		organisasjonsenhet.setEnhetNavn(enhetNavn);
		response.getEnhetListe().add(0, organisasjonsenhet);
		return response;
	}
}
