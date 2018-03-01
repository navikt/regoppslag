package no.nav.regoppslag.consumer.norg2;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v2.OrganisasjonEnhetKontaktinformasjonV2;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v2.informasjon.WSFeiletEnhet;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v2.informasjon.WSOrganisasjonsenhet;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v2.meldinger.WSHentKontaktinformasjonForEnhetBolkRequest;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v2.meldinger.WSHentKontaktinformasjonForEnhetBolkResponse;
import org.junit.Test;

public class OrganisasjonEnhetKontaktinformasjonV2ConsumerTest {
	private OrganisasjonEnhetKontaktinformasjonV2 organisasjonEnhetKontaktinformasjonV2 = mock(OrganisasjonEnhetKontaktinformasjonV2.class);
	private OrganisasjonEnhetKontaktinformasjonV2Consumer organisasjonEnhetKontaktinformasjonV2Consumer = new OrganisasjonEnhetKontaktinformasjonV2Consumer(organisasjonEnhetKontaktinformasjonV2);

	private final String ENHET_NR = "1234";
	private final String ENHET_NAVN = "NAV Husnes";

	@Test
	public void shouldHentEnhetNavn() throws Exception {
		when(organisasjonEnhetKontaktinformasjonV2.hentKontaktinformasjonForEnhetBolk(any(WSHentKontaktinformasjonForEnhetBolkRequest.class))).thenReturn(defaultResponse());

		String enhetNavn = organisasjonEnhetKontaktinformasjonV2Consumer.hentEnhetNavn(ENHET_NR);

		assertThat(enhetNavn, is(ENHET_NAVN));
	}

	@Test
	public void shouldReturnNullWhenEmptyEnhetListe() throws Exception{
		WSHentKontaktinformasjonForEnhetBolkResponse response = defaultResponse();
		response.getEnhetListe().clear();
		when(organisasjonEnhetKontaktinformasjonV2.hentKontaktinformasjonForEnhetBolk(any(WSHentKontaktinformasjonForEnhetBolkRequest.class))).thenReturn(response);

		String enhetNavn = organisasjonEnhetKontaktinformasjonV2Consumer.hentEnhetNavn(ENHET_NR);

		assertThat(enhetNavn, nullValue());
	}

	@Test
	public void shouldReturnNullWhenNameNotInResponse() throws Exception{
		WSHentKontaktinformasjonForEnhetBolkResponse response = defaultResponse();
		response.getEnhetListe().get(0).setEnhetNavn(null);
		when(organisasjonEnhetKontaktinformasjonV2.hentKontaktinformasjonForEnhetBolk(any(WSHentKontaktinformasjonForEnhetBolkRequest.class))).thenReturn(response);

		String enhetNavn = organisasjonEnhetKontaktinformasjonV2Consumer.hentEnhetNavn(ENHET_NR);

		assertThat(enhetNavn, nullValue());
	}

	@Test
	public void shouldReturnNullWhenFeilEnhetListe() throws Exception{
		WSHentKontaktinformasjonForEnhetBolkResponse response = defaultResponse();
		response.getEnhetListe().clear();
		response.getFeiletEnhetListe().add(0, new WSFeiletEnhet().withEnhetId(ENHET_NR).withFeilmelding("Fant ikke enheten"));
		when(organisasjonEnhetKontaktinformasjonV2.hentKontaktinformasjonForEnhetBolk(any(WSHentKontaktinformasjonForEnhetBolkRequest.class))).thenReturn(response);

		String enhetNavn = organisasjonEnhetKontaktinformasjonV2Consumer.hentEnhetNavn(ENHET_NR);

		assertThat(enhetNavn, nullValue());
	}

	//TODO
	//@Test
	public void shouldCacheHentEnhetNavn() throws Exception {
		when(organisasjonEnhetKontaktinformasjonV2.hentKontaktinformasjonForEnhetBolk(any(WSHentKontaktinformasjonForEnhetBolkRequest.class))).thenReturn(defaultResponse());

		organisasjonEnhetKontaktinformasjonV2Consumer.hentEnhetNavn(ENHET_NR);

		when(organisasjonEnhetKontaktinformasjonV2.hentKontaktinformasjonForEnhetBolk(any(WSHentKontaktinformasjonForEnhetBolkRequest.class))).thenReturn(createResponse("Denne enheten"));

		String enhetNavn = organisasjonEnhetKontaktinformasjonV2Consumer.hentEnhetNavn(ENHET_NR);

		assertThat(enhetNavn, is(ENHET_NAVN));
	}


	private WSHentKontaktinformasjonForEnhetBolkResponse defaultResponse() {
		return createResponse(ENHET_NAVN);
	}

	private WSHentKontaktinformasjonForEnhetBolkResponse createResponse(String enhetNavn) {
		WSHentKontaktinformasjonForEnhetBolkResponse response = new WSHentKontaktinformasjonForEnhetBolkResponse();
		response.getEnhetListe().add(0, new WSOrganisasjonsenhet().withEnhetNavn(enhetNavn));
		return response;
	}
}
