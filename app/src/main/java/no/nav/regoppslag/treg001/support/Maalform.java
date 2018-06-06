package no.nav.regoppslag.treg001.support;

import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.simpletypes.Spraakkode;
import no.nav.dokkat.api.tkat020.v3.SpraakInfoTo;

import java.util.List;

public class Maalform {
	public void setMaalform(Mottaker mottaker, List<SpraakInfoTo> spraakInfoMal) {
		if (mottaker.getSpraakkode() == null) {
			setMaalFormNaarBrukerIkkeHarSattMaalform(mottaker, spraakInfoMal);
		} else { //Bruker har ikke satt språk
			setMaalFormNaarBrukerHarSattMaalform(mottaker, spraakInfoMal);
		}
	}

	private void setMaalFormNaarBrukerHarSattMaalform(Mottaker mottaker, List<SpraakInfoTo> spraakInfoMal) {
		if (spraakInfoMal == null) {
			mottaker.setSpraakkode(Spraakkode.NB);
		} else {
			//Dersom malen inneholder mottakers prefererte språk, ingen endring
			if (!malInneholderSpraak(spraakInfoMal, mottaker.getSpraakkode().value())) {
				//Malen finnes ikke på mottakers prefererte språk
				if ((Spraakkode.NN).equals(mottaker.getSpraakkode()) && malInneholderSpraak(spraakInfoMal, "NB")) {
					//Har bruker satt nynorsk, men malen finnes på bokmål
					mottaker.setSpraakkode(Spraakkode.NB);
				} else if (malInneholderSpraak(spraakInfoMal, "NN")) {
					//Malen finnes på nynorsk
					mottaker.setSpraakkode(Spraakkode.NN);
				} else if (malInneholderSpraak(spraakInfoMal, "EN")) {
					//Malen finnes på engelsk
					mottaker.setSpraakkode(Spraakkode.EN);
				} else {
					//når alt annet feiler
					mottaker.setSpraakkode(Spraakkode.NB);
				}
			}
		}
	}

	private void setMaalFormNaarBrukerIkkeHarSattMaalform(Mottaker mottaker, List<SpraakInfoTo> spraakInfoMal) {
		if (spraakInfoMal == null) {
			mottaker.setSpraakkode(Spraakkode.NB);
		} else {
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
