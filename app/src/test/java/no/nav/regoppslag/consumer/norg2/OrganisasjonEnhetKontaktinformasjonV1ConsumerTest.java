package no.nav.regoppslag.consumer.norg2;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.binding.HentKontaktinformasjonForEnhetBolkUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.binding.OrganisasjonEnhetKontaktinformasjonV1;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.feil.UgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.FeiletEnhet;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.Organisasjonsenhet;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.meldinger.HentKontaktinformasjonForEnhetBolkRequest;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.meldinger.HentKontaktinformasjonForEnhetBolkResponse;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class OrganisasjonEnhetKontaktinformasjonV1ConsumerTest {
	private OrganisasjonEnhetKontaktinformasjonV1 organisasjonEnhetKontaktinformasjonV1 = mock(OrganisasjonEnhetKontaktinformasjonV1.class);
	private OrganisasjonEnhetKontaktinformasjonV1Consumer organisasjonEnhetKontaktinformasjonV1Consumer = new OrganisasjonEnhetKontaktinformasjonV1Consumer(organisasjonEnhetKontaktinformasjonV1);

	private final String ENHET_NR = "1234";
	private final String ENHET_NAVN = "NAV Husnes";

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void shouldHentEnhetNavn() throws Exception {
		when(organisasjonEnhetKontaktinformasjonV1.hentKontaktinformasjonForEnhetBolk(any(HentKontaktinformasjonForEnhetBolkRequest.class))).thenReturn(defaultResponse());

		Organisasjonsenhet enhet = organisasjonEnhetKontaktinformasjonV1Consumer.hentKontaktinformasjonForEnhet(ENHET_NR);

		assertThat(enhet.getEnhetNavn(), is(ENHET_NAVN));
	}

	@Test
	public void shouldReturnNullWhenEmptyEnhetListe() throws Exception{
		HentKontaktinformasjonForEnhetBolkResponse response = defaultResponse();
		response.getEnhetListe().clear();
		when(organisasjonEnhetKontaktinformasjonV1.hentKontaktinformasjonForEnhetBolk(any(HentKontaktinformasjonForEnhetBolkRequest.class))).thenReturn(response);

		Organisasjonsenhet enhet = organisasjonEnhetKontaktinformasjonV1Consumer.hentKontaktinformasjonForEnhet(ENHET_NR);

		assertThat(enhet, nullValue());
	}

	@Test
	public void shouldReturnNullWhenNameNotInResponse() throws Exception{
		HentKontaktinformasjonForEnhetBolkResponse response = defaultResponse();
		response.getEnhetListe().get(0).setEnhetNavn(null);
		when(organisasjonEnhetKontaktinformasjonV1.hentKontaktinformasjonForEnhetBolk(any(HentKontaktinformasjonForEnhetBolkRequest.class))).thenReturn(response);

		Organisasjonsenhet enhet = organisasjonEnhetKontaktinformasjonV1Consumer.hentKontaktinformasjonForEnhet(ENHET_NR);

		assertThat(enhet.getEnhetNavn(), nullValue());
	}

	@Test
	public void shouldReturnNullWhenFeilEnhetListe() throws Exception{
		expectedException.expect(RegOppslagFunctionalException.class);
		expectedException.expectMessage("Nav enhet finnes ikke for enhetNr="+ ENHET_NR);

		HentKontaktinformasjonForEnhetBolkResponse response = defaultResponse();
		response.getEnhetListe().clear();
		FeiletEnhet feiletEnhet = new FeiletEnhet();
		feiletEnhet.setEnhetId(ENHET_NR);
		feiletEnhet.setFeilmelding("Fant ikke enheten");
		response.getFeiletEnhetListe().add(0, feiletEnhet);
		when(organisasjonEnhetKontaktinformasjonV1.hentKontaktinformasjonForEnhetBolk(any(HentKontaktinformasjonForEnhetBolkRequest.class))).thenReturn(response);

		organisasjonEnhetKontaktinformasjonV1Consumer.hentKontaktinformasjonForEnhet(ENHET_NR);
	}

	@Test
	public void shouldThrowFunctionalErrorWhenUgyldigInput() throws Exception {
		when(organisasjonEnhetKontaktinformasjonV1.hentKontaktinformasjonForEnhetBolk(any(HentKontaktinformasjonForEnhetBolkRequest.class))).thenThrow(new HentKontaktinformasjonForEnhetBolkUgyldigInput("Ugyldig input", new UgyldigInput()));

		expectedException.expect(RegOppslagFunctionalException.class);
		expectedException.expectMessage("Nav enhet finnes ikke for enhetNr="+ENHET_NR+", message=Ugyldig input");

		organisasjonEnhetKontaktinformasjonV1Consumer.hentKontaktinformasjonForEnhet(ENHET_NR);

	}

	@Test
	public void shouldThrowTechnicalErrorErrorWhenRuntimeException() throws Exception {
		when(organisasjonEnhetKontaktinformasjonV1.hentKontaktinformasjonForEnhetBolk(any(HentKontaktinformasjonForEnhetBolkRequest.class))).thenThrow(new RuntimeException());

		expectedException.expect(RegOppslagTechnicalException.class);
		expectedException.expectMessage("Noe gikk galt i kall til Norg for enhetNr="+ENHET_NR);

		Organisasjonsenhet enhet = organisasjonEnhetKontaktinformasjonV1Consumer.hentKontaktinformasjonForEnhet(ENHET_NR);

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
