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
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Tkat020DokumenttypeInfoTest {

	private static final String DOKDUMENTYPE_ID = "123";
	private static final String LANG1 = "nb";
	private static final String LANG2 = "no";


	private RestTemplate restTemplate;
	private Tkat020DokumenttypeInfo tkatConsumer;

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

	DokumentTypeInfoToV3 defaultResponse(List<String> langs) {
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
