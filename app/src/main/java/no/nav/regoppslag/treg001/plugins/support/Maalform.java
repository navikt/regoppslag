package no.nav.regoppslag.treg001.plugins.support;

import no.nav.dok.metaforcemal.jaxb2.gen.Mottaker;
import no.nav.dok.metaforcemal.jaxb2.gen.Spraakkode;
import no.nav.dokkat.api.tkat020.v3.SpraakInfoTo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Maalform {
	public void setMaalform(Mottaker mottaker, List<SpraakInfoTo> spraakInfoMal) {
		//TODO finner ikke samisk i ENUM per nå....
		if (mottaker.getSpraakkode() != null) {
			//Dersom malen inneholder mottakers prefererte språk, ingen endring
			if (!malInneholderSpraak(spraakInfoMal, mottaker.getSpraakkode().value())) {
				//Malen finnes ikke på mottakers prefererte språk
				if (mottaker.getSpraakkode() == Spraakkode.NN && malInneholderSpraak(spraakInfoMal, "NB")) {
					//Har bruker satt nynorsk, men malen finnes på bokmål
					mottaker.setSpraakkode(Spraakkode.NB);
				} else if (malInneholderSpraak(spraakInfoMal, "EN")) {
					//Malen finnes på engelsk
					mottaker.setSpraakkode(Spraakkode.EN);
				} else {
					//når alt annet feiler
					mottaker.setSpraakkode(Spraakkode.NB);
				}
			}
		} else { //Bruker har ikke satt språk
			if (malInneholderSpraak(spraakInfoMal, "NB")) {
				mottaker.setSpraakkode(Spraakkode.NB);
			} else if (malInneholderSpraak(spraakInfoMal, "NN")) {
				mottaker.setSpraakkode(Spraakkode.NN);
			} else if (malInneholderSpraak(spraakInfoMal, "EN")) {
				mottaker.setSpraakkode(Spraakkode.EN);
			} else {
				mottaker.setSpraakkode(Spraakkode.NB);
			}
		}

		//Dersom bruker ikke har satt målform og malen inneholder bokmål. settes bokmål

		//Dersom bruker har satt nynorsk eller samisk, men malen finnes på BM, sette BM

		//Finnes malen på engelsk setter vi den til engelsk

		//siste utvei, sett bokmål og logg advarsel

	}

	private boolean malInneholderSpraak(final List<SpraakInfoTo> list, final String spraak) {
		if ("NB".equalsIgnoreCase(spraak)) {
			// NO og NB skal begge bahandles som Bokmål
			return (list.stream().filter(o -> o.getSpraaklag().equals("NO")).findFirst().isPresent() || (list.stream().filter(o -> o.getSpraaklag().equals("NB")).findFirst().isPresent()));
		} else {
			return (list.stream().filter(o -> o.getSpraaklag().equalsIgnoreCase(spraak)).findFirst().isPresent());
		}
	}
}
