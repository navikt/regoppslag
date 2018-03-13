package no.nav.regoppslag.consumer.dokkat;

import no.nav.dokkat.api.tkat020.v3.SpraakInfoTo;

import java.util.ArrayList;
import java.util.List;

public class Tkat020DokumenttypeInfoTest {

	private static final String DOKDUMENTYPE_ID = "123";
	private static final String LANG1= "nb";
	private static final String LANG2= "no";

	private Tkat020DokumenttypeInfo tkatConsumer;

//	@Test
	public void shouldHentSpraakinfo() throws Exception{
//		when(tkatConsumer.hentDokumenttypeInfoSpraak(any(String.class)).thenReturn(defaultResponse(Arrays.asList(LANG1, LANG2))));

		List<SpraakInfoTo> sprakinfos = tkatConsumer.hentDokumenttypeInfoSpraak(DOKDUMENTYPE_ID);

//		assertThat(sprakinfos.get(0).getSpraaklag(), is(LANG1));
	}

	List<SpraakInfoTo> defaultResponse(List<String> langs) {
		List<SpraakInfoTo> list = new ArrayList<>();
		//langs.forEach(lang -> list.add(new SpraakInfoTo().lang));
//		list.add(langs.forEach(list.add(kabng)); -> setSpraaklag(lang) );

		return list;
	}

}
