package no.nav.regoppslag.util;

import no.nav.regoppslag.consumer.pdl.to.Endring;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Comparator.comparing;

public class AdresseUtils {


	public static LocalDateTime getDatoForSisteEndring(List<Endring> endringer) {
		endringer.sort(comparing(Endring::getRegistrert));
		int antallEndringer = endringer.size();
		return antallEndringer > 0 ? endringer.get(antallEndringer - 1).getRegistrert() : null;

	}
}
