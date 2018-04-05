package no.nav.regoppslag.treg001.support;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import no.nav.dok.metaforcemal.jaxb2.gen.Mottaker;
import no.nav.dok.metaforcemal.jaxb2.gen.Spraakkode;
import no.nav.dokkat.api.tkat020.v3.SpraakInfoTo;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MaalformTest {

	private Maalform maalform = new Maalform();

	@Test
	public void spraakPaaMalOgMottakerMatcher() {
		Mottaker mottaker = new Mottaker();
		mottaker.setSpraakkode(Spraakkode.NB);
		List<SpraakInfoTo> list = createTkatResponse(Arrays.asList("NB", "EN", "NN"));
		maalform.setMaalform(mottaker, list);
		assertThat(mottaker.getSpraakkode(), is(Spraakkode.NB));

		mottaker.setSpraakkode(Spraakkode.NN);
		maalform.setMaalform(mottaker, list);
		assertThat(mottaker.getSpraakkode(), is(Spraakkode.NN));

		mottaker.setSpraakkode(Spraakkode.EN);
		maalform.setMaalform(mottaker, list);
		assertThat(mottaker.getSpraakkode(), is(Spraakkode.EN));
	}

	@Test
	public void spraakPaaMalOgMottakerMatcherSprakNO() {
		// NO og NB skal behandles likt
		Mottaker mottaker = new Mottaker();
		mottaker.setSpraakkode(Spraakkode.NB);
		List<SpraakInfoTo> list = createTkatResponse(Arrays.asList("NO", "EN", "NN"));
		maalform.setMaalform(mottaker, list);
		assertThat(mottaker.getSpraakkode(), is(Spraakkode.NB));
	}

	@Test
	public void spraakPaaMalOgMottakerMatcherIkkeOgNB() {
		Mottaker mottaker = new Mottaker();
		mottaker.setSpraakkode(Spraakkode.NB);
		List<SpraakInfoTo> list = createTkatResponse(Arrays.asList("NN", "EN"));
		maalform.setMaalform(mottaker, list);
		assertThat(mottaker.getSpraakkode(), is(Spraakkode.NN));
	}

	@Test
	public void spraakPaaMalOgMottakerMatcherIkkeOgNN() {
		Mottaker mottaker = new Mottaker();
		mottaker.setSpraakkode(Spraakkode.NN);
		List<SpraakInfoTo> list = createTkatResponse(Arrays.asList("NB", "EN"));
		maalform.setMaalform(mottaker, list);
		assertThat(mottaker.getSpraakkode(), is(Spraakkode.NB));
	}

	@Test
	public void spraakPaaMalOgMottakerMatcherIkkeOgEN() {
		Mottaker mottaker = new Mottaker();
		mottaker.setSpraakkode(Spraakkode.NN);
		List<SpraakInfoTo> list = createTkatResponse(Arrays.asList("EN"));
		maalform.setMaalform(mottaker, list);
		assertThat(mottaker.getSpraakkode(), is(Spraakkode.EN));
	}

	@Test
	public void spraakPaaMalOgMottakerMatcherIkkeOgUkjentSpråk() {
		Mottaker mottaker = new Mottaker();
		mottaker.setSpraakkode(Spraakkode.NN);
		List<SpraakInfoTo> list = createTkatResponse(Arrays.asList("FR"));
		maalform.setMaalform(mottaker, list);
		assertThat(mottaker.getSpraakkode(), is(Spraakkode.NB));
	}

	@Test
	public void spraakPaaMalErTomBrukerHarSattMaalform() {
		Mottaker mottaker = new Mottaker();
		mottaker.setSpraakkode(Spraakkode.NN);
		maalform.setMaalform(mottaker, null);
		assertThat(mottaker.getSpraakkode(), is(Spraakkode.NB));
	}


	@Test
	public void spraakPaaMalOgMottakerSpraakNULL_NB() {
		Mottaker mottaker = new Mottaker();
		List<SpraakInfoTo> list = createTkatResponse(Arrays.asList("EN", "NB"));
		maalform.setMaalform(mottaker, list);
		assertThat(mottaker.getSpraakkode(), is(Spraakkode.NB));
	}

	@Test
	public void spraakPaaMalOgMottakerSpraakNULL_EN() {
		Mottaker mottaker = new Mottaker();
		List<SpraakInfoTo> list = createTkatResponse(Arrays.asList("EN"));
		maalform.setMaalform(mottaker, list);
		assertThat(mottaker.getSpraakkode(), is(Spraakkode.EN));
	}

	@Test
	public void spraakPaaMalOgMottakerSpraakNULL_NN() {
		Mottaker mottaker = new Mottaker();
		List<SpraakInfoTo> list = createTkatResponse(Arrays.asList("EN", "NN"));
		maalform.setMaalform(mottaker, list);
		assertThat(mottaker.getSpraakkode(), is(Spraakkode.NN));
	}

	@Test
	public void spraakPaaMalOgMottakerSpraakNULL_UGYLDIGSPRAAK() {
		Mottaker mottaker = new Mottaker();
		List<SpraakInfoTo> list = createTkatResponse(Arrays.asList("HOHO"));
		maalform.setMaalform(mottaker, list);
		assertThat(mottaker.getSpraakkode(), is(Spraakkode.NB));
	}


	@Test
	public void spraakIkkesatt() {
		Mottaker mottaker = new Mottaker();
		List<SpraakInfoTo> list = null;
		maalform.setMaalform(mottaker, list);
		assertThat(mottaker.getSpraakkode(), is(Spraakkode.NB));
	}

	private List<SpraakInfoTo> createTkatResponse(List<String> langs) {
		List<SpraakInfoTo> list = new ArrayList<>();
		if (langs != null) {
			langs.forEach(lang -> {
				SpraakInfoTo spraakInfoTo = new SpraakInfoTo();
				spraakInfoTo.setSpraaklag(lang);
				list.add(spraakInfoTo);
			});
		}
		return list;
	}
}
