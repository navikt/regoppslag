package no.nav.regoppslag.consumer.ereg;

import no.nav.regoppslag.consumer.ereg.support.Organisasjon;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EregConsumerTest {
	private static final String ORGNR = "999999999";
	private static final String ORGNAVN = "NAV AS";
	private static final String ORGNAVN_2 = "SAGENE";
	private MicrometerMetrics metrics = mock(MicrometerMetrics.class);
	private String eregUrl = "www.test.no";
	private RestTemplateBuilder restTemplateBuilder = mock(RestTemplateBuilder.class);
	private EregConsumer organisasjonV4Consumer = new EregConsumer(eregUrl, restTemplateBuilder, metrics);

 /*
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
		RegOppslagFunctionalException e = assertThrows(RegOppslagFunctionalException.class,
				() -> organisasjonV4Consumer.hentOrganisasjon(ORGNR), "Nav enhet finnes ikke for enhetNr=999999999");
		assertEquals(NOT_FOUND, e.getHttpStatus());
	}

	@Test
	public void shouldThrowTechnicalExceptionWhenRuntimeExceptionThrown() throws Exception {
		when(organisasjonV4.hentOrganisasjon(any(HentOrganisasjonRequest.class)))
				.thenThrow(new RuntimeException());
		RegOppslagTechnicalException e = assertThrows(RegOppslagTechnicalException.class,
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

  */

}
