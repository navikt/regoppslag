package no.nav.regoppslag.consumer.dokkat;

import static com.sun.org.apache.xerces.internal.util.PropertyState.is;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.any;
import static org.mockito.Mockito.when;

import no.nav.dokkat.api.tkat020.v3.SpraakInfoTo;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
		langs.forEach(lang -> list.add(new SpraakInfoTo().lang));
//		list.add(langs.forEach(list.add(kabng)); -> setSpraaklag(lang) );

		return list;
	}

}
