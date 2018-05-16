package no.nav.regoppslag.consumer.dokkat;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.nav.dokkat.api.tkat020.v3.DokumentProduksjonsInfoToV3;
import no.nav.dokkat.api.tkat020.v3.DokumentTypeInfoToV3;
import no.nav.dokkat.api.tkat020.v3.SpraakInfoTo;
import no.nav.regoppslag.exceptions.DokkatFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Tkat020DokumenttypeInfoTest {

	private static final String DOKDUMENTYPE_ID = "I000003";
	private static final String LANG1 = "nb";
	private static final String LANG2 = "no";


	private RestTemplate restTemplate;
	private Tkat020DokumenttypeInfo tkatConsumer;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		restTemplate = mock(RestTemplate.class);
		tkatConsumer = new Tkat020DokumenttypeInfo(restTemplate);
	}

	@Test
	public void shouldHentSpraakinfo() throws Exception {
		when(restTemplate.getForObject(any(String.class), eq(DokumentTypeInfoToV3.class), any(Map.class)))
				.thenReturn(defaultResponse(Arrays.asList(LANG1, LANG2)));

		List<SpraakInfoTo> sprakinfos = tkatConsumer.hentDokumenttypeInfoSpraak(DOKDUMENTYPE_ID);

		assertThat(sprakinfos, hasSize(2));
		assertThat(sprakinfos.get(0).getSpraaklag(), is(LANG1));
		assertThat(sprakinfos.get(1).getSpraaklag(), is(LANG2));
	}
	
	@Test
	public void shouldThrowDokkatFunctionalExceptionWhenNotFound() throws Exception {
		when(restTemplate.getForObject(any(String.class), eq(DokumentTypeInfoToV3.class), any(Map.class)))
				.thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
		
		expectedException.expectMessage("Dokkat.TKAT020 feilet med statusKode=404. Fant ingen dokumenttypeInfo med dokumenttypeId=I000003.");
		expectedException.expect(DokkatFunctionalException.class);
		
		List<SpraakInfoTo> sprakinfos = tkatConsumer.hentDokumenttypeInfoSpraak(DOKDUMENTYPE_ID);
	}

	@Test
	public void shouldThrowTechnicalExceptionWhenServiceUnavaliable() throws Exception {
		when(restTemplate.getForObject(any(String.class), eq(DokumentTypeInfoToV3.class), any(Map.class)))
				.thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
		
		expectedException.expectMessage("Dokkat.TKAT020 feilet teknisk med statusKode=500 for dokumenttypeId=I000003");
		expectedException.expect(RegOppslagTechnicalException.class);

		List<SpraakInfoTo> sprakinfos = tkatConsumer.hentDokumenttypeInfoSpraak(DOKDUMENTYPE_ID);
	}

	@Test
	public void shouldThrowTechnicalExceptionWhenServerException() throws Exception {
		when(restTemplate.getForObject(any(String.class), eq(DokumentTypeInfoToV3.class), any(Map.class)))
				.thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));

		expectedException.expectMessage("Dokkat.TKAT020 feilet teknisk med statusKode=503 for dokumenttypeId=I000003");
		expectedException.expect(RegOppslagTechnicalException.class);

		List<SpraakInfoTo> sprakinfos = tkatConsumer.hentDokumenttypeInfoSpraak(DOKDUMENTYPE_ID);
	}
	
	private DokumentTypeInfoToV3 defaultResponse(List<String> langs) {
		DokumentTypeInfoToV3 dokumentTypeInfoToV3 = new DokumentTypeInfoToV3();
		DokumentProduksjonsInfoToV3 dokumentProduksjonsInfo = new DokumentProduksjonsInfoToV3();
		List<SpraakInfoTo> list = new ArrayList<>();
		langs.forEach(lang -> {
			SpraakInfoTo spraakInfoTo = new SpraakInfoTo();
			spraakInfoTo.setSpraaklag(lang);
			list.add(spraakInfoTo);
		});
		dokumentProduksjonsInfo.getSpraakInfos().addAll(list);
		dokumentTypeInfoToV3.setDokumentProduksjonsInfo(dokumentProduksjonsInfo);
		return dokumentTypeInfoToV3;
	}

}
