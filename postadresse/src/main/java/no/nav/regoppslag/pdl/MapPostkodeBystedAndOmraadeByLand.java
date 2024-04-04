package no.nav.regoppslag.pdl;

import no.nav.regoppslag.consumer.pdl.to.UtenlandskAdresse;

public interface MapPostkodeBystedAndOmraadeByLand {
	String mapUSAandKanadaPostkodeBystedAndOmraade(UtenlandskAdresse utenlandskAdresse);

	String mapDefaultPostkodeBystedAndOmraade(UtenlandskAdresse utenlandskAdresse);
}
