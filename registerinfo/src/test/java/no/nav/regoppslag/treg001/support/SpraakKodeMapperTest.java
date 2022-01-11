package no.nav.regoppslag.treg001.support;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;

import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.Person;
import no.nav.dok.brevdata.felles.v1.simpletypes.Spraakkode;
import no.nav.dokkat.api.tkat020.v3.SpraakInfoTo;
import no.nav.regoppslag.exceptions.IngenGyldigEnumVerdiForSpraakKodeException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpraakKodeMapperTest {

	private SpraakKodeMapper spraakKodeMapper = new SpraakKodeMapper();

	@Test
	public void spraakPaaMalOgMottakerMatcher() throws IngenGyldigEnumVerdiForSpraakKodeException {
		Mottaker mottaker = new Person();
		mottaker.setSpraakkode(Spraakkode.NB);
		List<SpraakInfoTo> list = createTkatResponse(Arrays.asList("NB", "EN", "NN"));
		Spraakkode spraakkode = spraakKodeMapper.getSpraakKode(mottaker, Spraakkode.NB.name(), list);
		assertThat(spraakkode, is(Spraakkode.NB));

		spraakkode = spraakKodeMapper.getSpraakKode(mottaker, "NO", list);
		assertThat(spraakkode, is(Spraakkode.NB));

		mottaker.setSpraakkode(Spraakkode.NN);
		spraakkode = spraakKodeMapper.getSpraakKode(mottaker, Spraakkode.NN.name(), list);
		assertThat(spraakkode, is(Spraakkode.NN));

		mottaker.setSpraakkode(Spraakkode.EN);
		spraakkode = spraakKodeMapper.getSpraakKode(mottaker, Spraakkode.EN.name(), list);
		assertThat(spraakkode, is(Spraakkode.EN));
	}

	@Test
	public void skalPrioritereMottakerSpraak() throws IngenGyldigEnumVerdiForSpraakKodeException {
		Mottaker mottaker = new Person();
		mottaker.setSpraakkode(Spraakkode.EN);
		List<SpraakInfoTo> list = createTkatResponse(Arrays.asList("NO", "EN", "NN"));
		Spraakkode spraakkode = spraakKodeMapper.getSpraakKode(mottaker, Spraakkode.NB.name(), list);
		assertThat(spraakkode, is(Spraakkode.NB));

		mottaker.setSpraakkode(Spraakkode.EN);
		Spraakkode spraakkode2 = spraakKodeMapper.getSpraakKode(mottaker, null, list);
		assertThat(spraakkode2, is(Spraakkode.EN));
	}

	@Test
	public void skalSetteSpraakKodeNBNaarMottakerSpraakErDansk() throws IngenGyldigEnumVerdiForSpraakKodeException {
		Mottaker mottaker = new Person();
		List<SpraakInfoTo> list = createTkatResponse(Arrays.asList("NO", "EN", "NN"));
		Spraakkode spraakkode = spraakKodeMapper.getSpraakKode(mottaker, "DA", list);
		assertThat(spraakkode, is(Spraakkode.NB));
	}

	@Test
	public void skalSetteSpraakKodeNBNaarPersonErUtenlandskMenMalManglerEngelskSpraak() throws IngenGyldigEnumVerdiForSpraakKodeException {
		Mottaker mottaker = new Person();
		List<SpraakInfoTo> list = createTkatResponse(Arrays.asList("NO", "NN"));
		Spraakkode spraakkode = spraakKodeMapper.getSpraakKode(mottaker, "TR", list);
		assertThat(spraakkode, is(Spraakkode.NB));
	}

	@Test
	public void skalSetteSpraakKodeNBNaarMottakerSpraakErSvensk() throws IngenGyldigEnumVerdiForSpraakKodeException {
		Mottaker mottaker = new Person();
		List<SpraakInfoTo> list = createTkatResponse(Arrays.asList("NO", "EN", "NN"));
		Spraakkode spraakkode = spraakKodeMapper.getSpraakKode(mottaker, "SV", list);
		assertThat(spraakkode, is(Spraakkode.NB));
	}

	@Test
	public void skalSetteSpraakKodeENNaarMottakerSpraakErIkkeSkandinavisk() throws IngenGyldigEnumVerdiForSpraakKodeException {
		Mottaker mottaker = new Person();
		List<SpraakInfoTo> list = createTkatResponse(Arrays.asList("NO", "EN", "NN"));
		Spraakkode spraakkode = spraakKodeMapper.getSpraakKode(mottaker, "TR", list);
		assertThat(spraakkode, is(Spraakkode.EN));
	}

	@Test
	public void spraakPaaMalOgMottakerMatcherSprakNO() throws IngenGyldigEnumVerdiForSpraakKodeException {
		// NO og NB skal behandles likt
		Mottaker mottaker = new Person();
		List<SpraakInfoTo> list = createTkatResponse(Arrays.asList("NO", "EN", "NN"));
		Spraakkode spraakkode = spraakKodeMapper.getSpraakKode(mottaker, Spraakkode.NB.name(), list);
		assertThat(spraakkode, is(Spraakkode.NB));
	}

	@Test
	public void spraakPaaMalOgMottakerMatcherIkkeOgNB() throws IngenGyldigEnumVerdiForSpraakKodeException {
		Mottaker mottaker = new Person();
		List<SpraakInfoTo> list = createTkatResponse(Arrays.asList("NN", "EN"));
		Spraakkode spraakkode = spraakKodeMapper.getSpraakKode(mottaker, Spraakkode.NB.name(), list);
		assertThat(spraakkode, is(Spraakkode.NN));
	}

	@Test
	public void spraakPaaMalOgMottakerMatcherIkkeOgNN() throws IngenGyldigEnumVerdiForSpraakKodeException {
		Mottaker mottaker = new Person();
		List<SpraakInfoTo> list = createTkatResponse(Arrays.asList("NB", "EN"));
		Spraakkode spraakkode = spraakKodeMapper.getSpraakKode(mottaker, Spraakkode.NN.name(), list);
		assertThat(spraakkode, is(Spraakkode.NB));
	}

	@Test
	public void spraakPaaMalOgMottakerMatcherIkkeOgEN() throws IngenGyldigEnumVerdiForSpraakKodeException {
		Mottaker mottaker = new Person();
		List<SpraakInfoTo> list = createTkatResponse(singletonList("EN"));
		Spraakkode spraakkode = spraakKodeMapper.getSpraakKode(mottaker, Spraakkode.NN.name(), list);
		assertThat(spraakkode, is(Spraakkode.EN));
	}

	@Test
	public void spraakPaaMalOgMottakerMatcherIkkeOgUkjentSpråk() throws IngenGyldigEnumVerdiForSpraakKodeException {
		Mottaker mottaker = new Person();
		List<SpraakInfoTo> list = createTkatResponse(Arrays.asList("NB", "EN"));
		Spraakkode spraakkode = spraakKodeMapper.getSpraakKode(mottaker, "TR", list);
		assertThat(spraakkode, is(Spraakkode.EN));
	}

	@Test
	public void spraakPaaMalErTomBrukerHarSattMaalform() throws IngenGyldigEnumVerdiForSpraakKodeException {
		Mottaker mottaker = new Person();
		mottaker.setSpraakkode(Spraakkode.NN);
		Spraakkode spraakkode = spraakKodeMapper.getSpraakKode(mottaker, Spraakkode.NN.name(), null);
		assertThat(spraakkode, is(Spraakkode.NB));
	}


	@Test
	public void spraakPaaMalOgMottakerSpraakNULL_NB() throws IngenGyldigEnumVerdiForSpraakKodeException {
		Mottaker mottaker = new Person();
		List<SpraakInfoTo> list = createTkatResponse(Arrays.asList("EN", "NB"));
		Spraakkode spraakkode = spraakKodeMapper.getSpraakKode(mottaker, null, list);
		assertThat(spraakkode, is(Spraakkode.NB));
	}

	@Test
	public void spraakPaaMalOgMottakerSpraakNULL_EN() throws IngenGyldigEnumVerdiForSpraakKodeException {
		Mottaker mottaker = new Person();
		List<SpraakInfoTo> list = createTkatResponse(singletonList("EN"));
		Spraakkode spraakkode = spraakKodeMapper.getSpraakKode(mottaker, null, list);
		assertThat(spraakkode, is(Spraakkode.EN));
	}

	@Test
	public void spraakPaaMalOgMottakerSpraakNULL_NN() throws IngenGyldigEnumVerdiForSpraakKodeException {
		Mottaker mottaker = new Person();
		List<SpraakInfoTo> list = createTkatResponse(Arrays.asList("EN", "NN"));
		Spraakkode spraakkode = spraakKodeMapper.getSpraakKode(mottaker, null, list);
		assertThat(spraakkode, is(Spraakkode.NN));
	}

	@Test
	public void spraakPaaMalOgMottakerSpraakNULL_UGYLDIGSPRAAK() throws IngenGyldigEnumVerdiForSpraakKodeException {
		Mottaker mottaker = new Person();
		List<SpraakInfoTo> list = createTkatResponse(singletonList("HOHO"));
		Spraakkode spraakkode = spraakKodeMapper.getSpraakKode(mottaker, null, list);
		assertThat(spraakkode, is(Spraakkode.NB));
	}


	@Test
	public void spraakIkkesatt() throws IngenGyldigEnumVerdiForSpraakKodeException {
		Mottaker mottaker = new Person();
		List<SpraakInfoTo> list = null;
		Spraakkode spraakkode = spraakKodeMapper.getSpraakKode(mottaker, null, list);
		assertThat(spraakkode, is(Spraakkode.NB));
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
