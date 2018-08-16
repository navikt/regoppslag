package no.nav.regoppslag.treg001.support;

import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.simpletypes.Spraakkode;
import no.nav.dokkat.api.tkat020.v3.SpraakInfoTo;

import java.util.List;

public class Maalform {

	public Spraakkode getMaalform(Mottaker mottaker, List<SpraakInfoTo> spraakInfoMal) {
		if (mottaker.getSpraakkode() == null) {
			return getMaalFormNaarBrukerIkkeHarSattMaalform(spraakInfoMal);
		} else { //Bruker har ikke satt språk
			return getMaalFormNaarBrukerHarSattMaalform(mottaker, spraakInfoMal);
		}
	}

	private Spraakkode getMaalFormNaarBrukerHarSattMaalform(Mottaker mottaker, List<SpraakInfoTo> spraakInfoMal) {
		if (spraakInfoMal == null) {
			return Spraakkode.NB;
		} else {
			//Dersom malen inneholder mottakers prefererte språk, ingen endring
			if (!malInneholderSpraak(spraakInfoMal, mottaker.getSpraakkode().value())) {
				//Malen finnes ikke på mottakers prefererte språk
				if ((Spraakkode.NN).equals(mottaker.getSpraakkode()) && malInneholderSpraak(spraakInfoMal, "NB")) {
					//Har bruker satt nynorsk, men malen finnes på bokmål
					return Spraakkode.NB;
				} else if (malInneholderSpraak(spraakInfoMal, "NN")) {
					//Malen finnes på nynorsk
					return Spraakkode.NN;
				} else if (malInneholderSpraak(spraakInfoMal, "EN")) {
					//Malen finnes på engelsk
					return Spraakkode.EN;
				} else {
					//når alt annet feiler
					return Spraakkode.NB;
				}
			}

			return mottaker.getSpraakkode();
		}

	}

	private Spraakkode getMaalFormNaarBrukerIkkeHarSattMaalform(List<SpraakInfoTo> spraakInfoMal) {
		if (spraakInfoMal == null) {
			return Spraakkode.NB;
		} else {
			if (malInneholderSpraak(spraakInfoMal, "NB")) {
				return Spraakkode.NB;
			} else if (malInneholderSpraak(spraakInfoMal, "NN")) {
				return Spraakkode.NN;
			} else if (malInneholderSpraak(spraakInfoMal, "EN")) {
				return Spraakkode.EN;
			} else {
				return Spraakkode.NB;
			}
		}
	}
	
	
	private boolean malInneholderSpraak(final List<SpraakInfoTo> spraakInfoTos, final String spraak) {
		if ("NB".equalsIgnoreCase(spraak)) {
			// NO og NB skal begge bahandles som Bokmål
			return (spraakInfoTos.stream().anyMatch(o -> "NO".equals(o.getSpraaklag())) || (spraakInfoTos.stream()
					.anyMatch(o -> ("NB").equals(o.getSpraaklag()))));
		} else {
			return (spraakInfoTos.stream().anyMatch(o -> o.getSpraaklag().equalsIgnoreCase(spraak)));
		}
	}
}
