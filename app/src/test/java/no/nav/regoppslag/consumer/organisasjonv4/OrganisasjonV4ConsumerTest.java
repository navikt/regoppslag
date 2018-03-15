package no.nav.regoppslag.consumer.organisasjonv4;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.nav.regoppslag.consumer.organisasjonv4.OrganisasjonV4Consumer;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentNoekkelinfoOrganisasjonOrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonOrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.OrganisasjonV4;
import no.nav.tjeneste.virksomhet.organisasjon.v4.feil.OrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.SammensattNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentNoekkelinfoOrganisasjonRequest;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentNoekkelinfoOrganisasjonResponse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonRequest;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonResponse;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void shouldHentOrganisasjon() throws Exception {
		when(organisasjonV4.hentOrganisasjon(any(HentOrganisasjonRequest.class))).thenReturn(defaultResponse());

		Organisasjon organisasjon = organisasjonV4Consumer.hentOrganisasjon(ORGNR);

		assertThat(sammensattNavn(organisasjon.getNavn()), is(ORGNAVN));
	}

	@Test
	public void shouldHentOrganisasjonWithMultipleNavnelinje() throws Exception {
		when(organisasjonV4.hentOrganisasjon(any(HentOrganisasjonRequest.class))).thenReturn(createResponse(Arrays.asList(ORGNAVN, ORGNAVN_2)));

		Organisasjon organisasjon = organisasjonV4Consumer.hentOrganisasjon(ORGNR);

		assertThat(sammensattNavn(organisasjon.getNavn()), is(ORGNAVN + " " + ORGNAVN_2));
	}

	@Test
	public void shouldThrowExceptionWhenOrganisasjonNotFound() throws Exception {
		thrown.expect(RegOppslagFunctionalException.class);
		thrown.expectMessage("Nav enhet finnes ikke for enhetNr=999999999");
		when(organisasjonV4.hentOrganisasjon(any(HentOrganisasjonRequest.class)))
				.thenThrow(new HentOrganisasjonOrganisasjonIkkeFunnet("organisasjon not found", new OrganisasjonIkkeFunnet()));

		Organisasjon organisasjon = organisasjonV4Consumer.hentOrganisasjon(ORGNR);
	}

	@Test
	public void shouldReturnNullWhenNavnIsNull() throws Exception {
		HentOrganisasjonResponse response = defaultResponse();
		response.setOrganisasjon(null);
		when(organisasjonV4.hentOrganisasjon(any(HentOrganisasjonRequest.class))).thenReturn(response);

		Organisasjon organisasjon = organisasjonV4Consumer.hentOrganisasjon(ORGNR);

		assertThat(organisasjon, nullValue());
	}

	@Test
	public void shouldReturnNullWhenNavnWrongInstance() throws Exception {
		HentOrganisasjonResponse response = defaultResponse();
		Organisasjon org = new Organisasjon();
		org.setNavn(new SammensattNavn() {
		});
		response.setOrganisasjon(org);
		when(organisasjonV4.hentOrganisasjon(any(HentOrganisasjonRequest.class))).thenReturn(response);

		Organisasjon organisasjon = organisasjonV4Consumer.hentOrganisasjon(ORGNR);

		assertThat(sammensattNavn(organisasjon.getNavn()), nullValue());
	}

	private HentOrganisasjonResponse defaultResponse() {
		return createResponse(Collections.singletonList(ORGNAVN));
	}

	private HentOrganisasjonResponse createResponse(List<String> lines) {
		HentOrganisasjonResponse response = new HentOrganisasjonResponse();
		Organisasjon organisasjon = new Organisasjon();
		UstrukturertNavn ustrukturertNavn = new UstrukturertNavn();
		ustrukturertNavn.getNavnelinje().addAll(lines);
		organisasjon.setNavn(ustrukturertNavn);
		response.setOrganisasjon(organisasjon);
		return response;
	}

	public String sammensattNavn(SammensattNavn sammensattNavn) {
		if (sammensattNavn instanceof UstrukturertNavn) {
			UstrukturertNavn navn = (UstrukturertNavn) sammensattNavn;
			StringBuilder sb = new StringBuilder();
			navn.getNavnelinje().forEach(s -> sb.append(s.trim()).append(" "));
			return sb.toString().trim();
		} else {
			return null;
		}
	}

}