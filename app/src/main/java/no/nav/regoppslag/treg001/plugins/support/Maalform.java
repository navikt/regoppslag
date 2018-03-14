package no.nav.regoppslag.treg001.plugins.support;

import no.nav.dok.metaforcemal.jaxb2.gen.Mottaker;
import no.nav.dokkat.api.tkat020.v3.SpraakInfoTo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Maalform {

	public void setMaalform(Mottaker mottaker, List<SpraakInfoTo> spraakInfoMal) {
		//Dersom malen inneholder mottakers prefererte språk, ingen endring

		//Dersom bruker ikke har satt målform og malen inneholder bokmål. settes bokmål

		//Dersom bruker har satt nynorsk eller samisk, men malen finnes på BM, sette BM

		//Finnes malen på engelsk setter vi den til engelsk

		//siste utvei, sett bokmål og logg advarsel

	}

	private boolean malInneholderSpraak(final List<SpraakInfoTo> list, final String spraak) {
		if ("NO".equalsIgnoreCase(spraak) || "NB".equalsIgnoreCase(spraak)) {
			// NO og NB skal begge bahandles som Bokmål
			return (list.stream().filter(o -> o.getSpraaklag().equals("NO")).findFirst().isPresent() || (list.stream().filter(o -> o.getSpraaklag().equals("NB")).findFirst().isPresent()));
		} else {
			return (list.stream().filter(o -> o.getSpraaklag().equalsIgnoreCase(spraak)).findFirst().isPresent());
		}
	}
}
