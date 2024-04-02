package no.nav.regoppslag.pdl;

import no.nav.regoppslag.consumer.pdl.to.UtenlandskAdresse;

public interface MapPostkodeBystedAndOmraadeByLand {
	String mapUsaPostkodeStedAndOmraade(UtenlandskAdresse utenlandskAdresse);

	String mapDefaultPostkodeStedAndOmraade(UtenlandskAdresse utenlandskAdresse);
}
