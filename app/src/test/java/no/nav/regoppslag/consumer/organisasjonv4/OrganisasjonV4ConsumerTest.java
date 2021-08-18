package no.nav.regoppslag.consumer.organisasjonv4;

import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonOrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.OrganisasjonV4;
import no.nav.tjeneste.virksomhet.organisasjon.v4.feil.OrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.SammensattNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonRequest;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class OrganisasjonV4ConsumerTest {

	private static final String ORGNR = "999999999";
	private static final String ORGNAVN = "NAV AS";
	private static final String ORGNAVN_2 = "SAGENE";
	private OrganisasjonV4 organisasjonV4 = mock(OrganisasjonV4.class);
	private MicrometerMetrics metrics = mock(MicrometerMetrics.class);
	private OrganisasjonV4Consumer organisasjonV4Consumer = new OrganisasjonV4Consumer(organisasjonV4, metrics);


	@Test
	public void shouldHentOrganisasjon() throws Exception {
		when(organisasjonV4.hentOrganisasjon(any(HentOrganisasjonRequest.class))).thenReturn(defaultResponse());

		Organisasjon organisasjon = organisasjonV4Consumer.hentOrganisasjon(ORGNR);

		assertEquals(ORGNAVN, sammensattNavn(organisasjon.getNavn()));
	}

	@Test
	public void shouldHentOrganisasjonWithMultipleNavnelinje() throws Exception {
		when(organisasjonV4.hentOrganisasjon(any(HentOrganisasjonRequest.class))).thenReturn(createResponse(Arrays.asList(ORGNAVN, ORGNAVN_2)));

		Organisasjon organisasjon = organisasjonV4Consumer.hentOrganisasjon(ORGNR);

		assertEquals(ORGNAVN + " " + ORGNAVN_2, sammensattNavn(organisasjon.getNavn()));
	}

	@Test
	public void shouldThrowExceptionWhenOrganisasjonNotFound() throws Exception {
		when(organisasjonV4.hentOrganisasjon(any(HentOrganisasjonRequest.class)))
				.thenThrow(new HentOrganisasjonOrganisasjonIkkeFunnet("organisasjon not found", new OrganisasjonIkkeFunnet()));
		RegOppslagFunctionalException e = Assertions.assertThrows(RegOppslagFunctionalException.class,
				() -> organisasjonV4Consumer.hentOrganisasjon(ORGNR), "Nav enhet finnes ikke for enhetNr=999999999");
		assertEquals(NOT_FOUND, e.getHttpStatus());
	}

	@Test
	public void shouldThrowTechnicalExceptionWhenRuntimeExceptionThrown() throws Exception {
		when(organisasjonV4.hentOrganisasjon(any(HentOrganisasjonRequest.class)))
				.thenThrow(new RuntimeException());
		RegOppslagTechnicalException e = Assertions.assertThrows(RegOppslagTechnicalException.class,
				() -> organisasjonV4Consumer.hentOrganisasjon(ORGNR), "Noe gikk galt i kall til OrganisasjonV4.hentOrganisasjon for enhetNr=999999999");
		assertEquals("Noe gikk galt i kall til OrganisasjonV4.hentOrganisasjon for enhetNr=999999999, message=null", e.getMessage());
	}


	@Test
	public void shouldReturnNullWhenNavnIsNull() throws Exception {
		HentOrganisasjonResponse response = defaultResponse();
		response.setOrganisasjon(null);
		when(organisasjonV4.hentOrganisasjon(any(HentOrganisasjonRequest.class))).thenReturn(response);

		Organisasjon organisasjon = organisasjonV4Consumer.hentOrganisasjon(ORGNR);

		assertNull(organisasjon);
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

		assertNull(sammensattNavn(organisasjon.getNavn()));
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