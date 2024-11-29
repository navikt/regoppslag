package no.nav.regoppslag.treg001.support;

import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.Person;
import no.nav.dok.brevdata.felles.v1.simpletypes.Spraakkode;
import no.nav.dokmet.api.tkat020.SpraakInfoTo;
import no.nav.regoppslag.exceptions.IngenGyldigEnumVerdiForSpraakKodeException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static no.nav.dok.brevdata.felles.v1.simpletypes.Spraakkode.EN;
import static no.nav.dok.brevdata.felles.v1.simpletypes.Spraakkode.NB;
import static no.nav.dok.brevdata.felles.v1.simpletypes.Spraakkode.NN;
import static no.nav.regoppslag.treg001.util.CreateStubs.createTkatResponse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SpraakKodeMapperTest {

	private final SpraakKodeMapper spraakKodeMapper = new SpraakKodeMapper();

	@Test
	public void spraakPaaMalOgMottakerMatcher() throws IngenGyldigEnumVerdiForSpraakKodeException {
		Mottaker mottaker = new Person();
		mottaker.setSpraakkode(NB);
		List<SpraakInfoTo> list = createTkatResponse(Arrays.asList("NB", "EN", "NN"));
		Spraakkode spraakkode = spraakKodeMapper.getSpraakKode(mottaker, NB.name(), list);
		assertThat(spraakkode, is(NB));

		spraakkode = spraakKodeMapper.getSpraakKode(mottaker, "NO", list);
		assertThat(spraakkode, is(NB));

		mottaker.setSpraakkode(NN);
		spraakkode = spraakKodeMapper.getSpraakKode(mottaker, NN.name(), list);
		assertThat(spraakkode, is(NN));

		mottaker.setSpraakkode(EN);
		spraakkode = spraakKodeMapper.getSpraakKode(mottaker, EN.name(), list);
		assertThat(spraakkode, is(EN));
	}

	@Test
	public void skalPrioritereMottakerSpraak() throws IngenGyldigEnumVerdiForSpraakKodeException {
		Mottaker mottaker = new Person();
		mottaker.setSpraakkode(EN);
		List<SpraakInfoTo> list = createTkatResponse(Arrays.asList("NO", "EN", "NN"));
		Spraakkode spraakkode = spraakKodeMapper.getSpraakKode(mottaker, NB.name(), list);
		assertThat(spraakkode, is(NB));

		mottaker.setSpraakkode(EN);
		Spraakkode spraakkode2 = spraakKodeMapper.getSpraakKode(mottaker, null, list);
		assertThat(spraakkode2, is(EN));
	}

	@Test
	public void skalSetteSpraakKodeNBNaarMottakerSpraakErDansk() throws IngenGyldigEnumVerdiForSpraakKodeException {
		Mottaker mottaker = new Person();
		List<SpraakInfoTo> list = createTkatResponse(Arrays.asList("NO", "EN", "NN"));

		Spraakkode spraakkode = spraakKodeMapper.getSpraakKode(mottaker, "DA", list);

		assertThat(spraakkode, is(NB));
	}

	@Test
	public void skalSetteSpraakKodeNBNaarPersonErUtenlandskMenMalManglerEngelskSpraak() throws IngenGyldigEnumVerdiForSpraakKodeException {
		Mottaker mottaker = new Person();
		List<SpraakInfoTo> list = createTkatResponse(Arrays.asList("NO", "NN"));

		Spraakkode spraakkode = spraakKodeMapper.getSpraakKode(mottaker, "TR", list);

		assertThat(spraakkode, is(NB));
	}

	@Test
	public void skalSetteSpraakKodeNBNaarMottakerSpraakErSvensk() throws IngenGyldigEnumVerdiForSpraakKodeException {
		Mottaker mottaker = new Person();
		List<SpraakInfoTo> list = createTkatResponse(Arrays.asList("NO", "EN", "NN"));

		Spraakkode spraakkode = spraakKodeMapper.getSpraakKode(mottaker, "SV", list);

		assertThat(spraakkode, is(NB));
	}

	@Test
	public void skalSetteSpraakKodeENNaarMottakerSpraakErIkkeSkandinavisk() throws IngenGyldigEnumVerdiForSpraakKodeException {
		Mottaker mottaker = new Person();
		List<SpraakInfoTo> list = createTkatResponse(Arrays.asList("NO", "EN", "NN"));

		Spraakkode spraakkode = spraakKodeMapper.getSpraakKode(mottaker, "TR", list);

		assertThat(spraakkode, is(EN));
	}

	@Test
	public void spraakPaaMalOgMottakerMatcherSprakNO() throws IngenGyldigEnumVerdiForSpraakKodeException {
		// NO og NB skal behandles likt
		Mottaker mottaker = new Person();
		List<SpraakInfoTo> list = createTkatResponse(Arrays.asList("NO", "EN", "NN"));

		Spraakkode spraakkode = spraakKodeMapper.getSpraakKode(mottaker, NB.name(), list);

		assertThat(spraakkode, is(NB));
	}

	@Test
	public void spraakPaaMalOgMottakerMatcherIkkeOgNB() throws IngenGyldigEnumVerdiForSpraakKodeException {
		Mottaker mottaker = new Person();
		List<SpraakInfoTo> list = createTkatResponse(Arrays.asList("NN", "EN"));

		Spraakkode spraakkode = spraakKodeMapper.getSpraakKode(mottaker, NB.name(), list);

		assertThat(spraakkode, is(NN));
	}

	@Test
	public void spraakPaaMalOgMottakerMatcherIkkeOgNN() throws IngenGyldigEnumVerdiForSpraakKodeException {
		Mottaker mottaker = new Person();
		List<SpraakInfoTo> list = createTkatResponse(Arrays.asList("NB", "EN"));

		Spraakkode spraakkode = spraakKodeMapper.getSpraakKode(mottaker, NN.name(), list);

		assertThat(spraakkode, is(NB));
	}

	@Test
	public void spraakPaaMalOgMottakerMatcherIkkeOgEN() throws IngenGyldigEnumVerdiForSpraakKodeException {
		Mottaker mottaker = new Person();
		List<SpraakInfoTo> list = createTkatResponse(singletonList("EN"));

		Spraakkode spraakkode = spraakKodeMapper.getSpraakKode(mottaker, NN.name(), list);

		assertThat(spraakkode, is(EN));
	}

	@Test
	public void spraakPaaMalOgMottakerMatcherIkkeOgUkjentSpraak() throws IngenGyldigEnumVerdiForSpraakKodeException {
		Mottaker mottaker = new Person();
		List<SpraakInfoTo> list = createTkatResponse(Arrays.asList("NB", "EN"));

		Spraakkode spraakkode = spraakKodeMapper.getSpraakKode(mottaker, "TR", list);

		assertThat(spraakkode, is(EN));
	}

	@Test
	public void spraakPaaMalErTomBrukerHarSattMaalform() throws IngenGyldigEnumVerdiForSpraakKodeException {
		Mottaker mottaker = new Person();
		mottaker.setSpraakkode(NN);

		Spraakkode spraakkode = spraakKodeMapper.getSpraakKode(mottaker, NN.name(), null);

		assertThat(spraakkode, is(NB));
	}

	@Test
	public void spraakPaaMalOgMottakerSpraakNULL_NB() throws IngenGyldigEnumVerdiForSpraakKodeException {
		Mottaker mottaker = new Person();
		List<SpraakInfoTo> list = createTkatResponse(Arrays.asList("EN", "NB"));

		Spraakkode spraakkode = spraakKodeMapper.getSpraakKode(mottaker, null, list);

		assertThat(spraakkode, is(NB));
	}

	@Test
	public void spraakPaaMalOgMottakerSpraakNULL_EN() throws IngenGyldigEnumVerdiForSpraakKodeException {
		Mottaker mottaker = new Person();
		List<SpraakInfoTo> list = createTkatResponse(singletonList("EN"));

		Spraakkode spraakkode = spraakKodeMapper.getSpraakKode(mottaker, null, list);

		assertThat(spraakkode, is(EN));
	}

	@Test
	public void spraakPaaMalOgMottakerSpraakNULL_NN() throws IngenGyldigEnumVerdiForSpraakKodeException {
		Mottaker mottaker = new Person();
		List<SpraakInfoTo> list = createTkatResponse(Arrays.asList("EN", "NN"));

		Spraakkode spraakkode = spraakKodeMapper.getSpraakKode(mottaker, null, list);

		assertThat(spraakkode, is(NN));
	}

	@Test
	public void spraakPaaMalOgMottakerSpraakNULL_UGYLDIGSPRAAK() throws IngenGyldigEnumVerdiForSpraakKodeException {
		Mottaker mottaker = new Person();
		List<SpraakInfoTo> list = createTkatResponse(singletonList("HOHO"));

		Spraakkode spraakkode = spraakKodeMapper.getSpraakKode(mottaker, null, list);

		assertThat(spraakkode, is(NB));
	}

	@Test
	public void spraakIkkesatt() throws IngenGyldigEnumVerdiForSpraakKodeException {
		Spraakkode spraakkode = spraakKodeMapper.getSpraakKode(new Person(), null, null);

		assertThat(spraakkode, is(NB));
	}

}
