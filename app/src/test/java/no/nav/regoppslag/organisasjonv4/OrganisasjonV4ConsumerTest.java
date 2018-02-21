package no.nav.regoppslag.organisasjonv4;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.nav.regoppslag.organisasjonv4.OrganisasjonV4Consumer;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentNoekkelinfoOrganisasjonOrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.OrganisasjonV4;
import no.nav.tjeneste.virksomhet.organisasjon.v4.feil.OrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.SammensattNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentNoekkelinfoOrganisasjonRequest;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentNoekkelinfoOrganisasjonResponse;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class OrganisasjonV4ConsumerTest {

	private static final String ORGNR = "999999999";
	private static final String ORGNAVN = "NAV AS";
	private static final String ORGNAVN_2 = "SAGENE";
	private OrganisasjonV4 organisasjonV4 = mock(OrganisasjonV4.class);
	private OrganisasjonV4Consumer organisasjonV4Consumer = new OrganisasjonV4Consumer(organisasjonV4);

	@Test
	public void shouldHentOrganisasjonsnavn() throws Exception {
		when(organisasjonV4.hentNoekkelinfoOrganisasjon(any(HentNoekkelinfoOrganisasjonRequest.class))).thenReturn(defaultResponse());

		String organisasjonsnavn = organisasjonV4Consumer.hentOrganisasjonsnavn(ORGNR);

		assertThat(organisasjonsnavn, is(ORGNAVN));
	}

	@Test
	public void shouldHentOrganisasjonsnavnWithMultipleNavnelinje() throws Exception {
		when(organisasjonV4.hentNoekkelinfoOrganisasjon(any(HentNoekkelinfoOrganisasjonRequest.class))).thenReturn(createResponse(Arrays.asList(ORGNAVN, ORGNAVN_2)));

		String organisasjonsnavn = organisasjonV4Consumer.hentOrganisasjonsnavn(ORGNR);

		assertThat(organisasjonsnavn, is(ORGNAVN + " " + ORGNAVN_2));
	}

	@Test
	public void shouldReturnNullWhenOrganisasjonNotFound() throws Exception {
		when(organisasjonV4.hentNoekkelinfoOrganisasjon(any(HentNoekkelinfoOrganisasjonRequest.class)))
				.thenThrow(new HentNoekkelinfoOrganisasjonOrganisasjonIkkeFunnet("organisasjon not found", new OrganisasjonIkkeFunnet()));

		String organisasjonsnavn = organisasjonV4Consumer.hentOrganisasjonsnavn(ORGNR);

		assertThat(organisasjonsnavn, nullValue());
	}

	@Test
	public void shouldReturnNullWhenNavnIsNull() throws Exception {
		HentNoekkelinfoOrganisasjonResponse response = defaultResponse();
		response.setNavn(null);
		when(organisasjonV4.hentNoekkelinfoOrganisasjon(any(HentNoekkelinfoOrganisasjonRequest.class))).thenReturn(response);

		String organisasjonsnavn = organisasjonV4Consumer.hentOrganisasjonsnavn(ORGNR);

		assertThat(organisasjonsnavn, nullValue());
	}

	@Test
	public void shouldReturnNullWhenNavnWrongInstance() throws Exception {
		HentNoekkelinfoOrganisasjonResponse response = defaultResponse();
		response.setNavn(new SammensattNavn() {
		});
		when(organisasjonV4.hentNoekkelinfoOrganisasjon(any(HentNoekkelinfoOrganisasjonRequest.class))).thenReturn(response);

		String organisasjonsnavn = organisasjonV4Consumer.hentOrganisasjonsnavn(ORGNR);

		assertThat(organisasjonsnavn, nullValue());
	}

	private HentNoekkelinfoOrganisasjonResponse defaultResponse() {
		return createResponse(Collections.singletonList(ORGNAVN));
	}

	private HentNoekkelinfoOrganisasjonResponse createResponse(List<String> lines) {
		HentNoekkelinfoOrganisasjonResponse response = new HentNoekkelinfoOrganisasjonResponse();
		UstrukturertNavn ustrukturertNavn = new UstrukturertNavn();
		ustrukturertNavn.getNavnelinje().addAll(lines);
		response.setNavn(ustrukturertNavn);
		return response;
	}
}