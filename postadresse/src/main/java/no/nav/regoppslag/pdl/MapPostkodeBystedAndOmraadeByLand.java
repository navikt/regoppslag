package no.nav.regoppslag.pdl;

import no.nav.regoppslag.consumer.pdl.to.UtenlandskAdresse;

public interface MapPostkodeBystedAndOmraadeByLand {
	String mapUSAandCanadaPostkodeBystedAndOmraade(UtenlandskAdresse utenlandskAdresse);

	String mapDefaultPostkodeStedAndOmraade(UtenlandskAdresse utenlandskAdresse);
}
