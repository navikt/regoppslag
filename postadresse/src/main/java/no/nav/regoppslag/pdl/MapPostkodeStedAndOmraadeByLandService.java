package no.nav.regoppslag.pdl;

import no.nav.regoppslag.consumer.pdl.to.UtenlandskAdresse;

import static no.nav.regoppslag.pdl.UtenlandskAdresseUtil.hasBySted;
import static no.nav.regoppslag.pdl.UtenlandskAdresseUtil.hasPostKode;
import static no.nav.regoppslag.pdl.UtenlandskAdresseUtil.hasRegionDistriktOmraade;
import static no.nav.regoppslag.pdl.UtenlandskAdresseUtil.joinAdresseMedKomma;
import static no.nav.regoppslag.pdl.UtenlandskAdresseUtil.joinAdresseUtenKomma;

public class MapPostkodeStedAndOmraadeByLandService implements MapPostkodeBystedAndOmraadeByLand {
	@Override
	public String mapUSAandKanadaPostkodeBystedAndOmraade(UtenlandskAdresse utenlandskAdresse) {

		if (hasPostKode(utenlandskAdresse)) {
			if (hasBySted(utenlandskAdresse) && hasRegionDistriktOmraade(utenlandskAdresse)) {
				return joinAdresseUtenKomma(utenlandskAdresse.getBySted(), utenlandskAdresse.getRegionDistriktOmraade(), utenlandskAdresse.getPostkode());
			} else {
				if (!hasBySted(utenlandskAdresse) && hasRegionDistriktOmraade(utenlandskAdresse)) {
					return joinAdresseUtenKomma(utenlandskAdresse.getRegionDistriktOmraade(), utenlandskAdresse.getPostkode(), "");

				} else if (hasBySted(utenlandskAdresse)) {
					return joinAdresseUtenKomma(utenlandskAdresse.getBySted(), utenlandskAdresse.getPostkode(), "");
				} else {
					return utenlandskAdresse.getPostkode();
				}
			}
		} else if (hasBySted(utenlandskAdresse)) {
			if (hasRegionDistriktOmraade(utenlandskAdresse)) {
				return joinAdresseUtenKomma(utenlandskAdresse.getBySted(), utenlandskAdresse.getRegionDistriktOmraade(), "");
			} else {
				return utenlandskAdresse.getBySted();
			}
		} else {
			return utenlandskAdresse.getRegionDistriktOmraade();
		}
	}

	@Override
	public String mapDefaultPostkodeBystedAndOmraade(UtenlandskAdresse utenlandskAdresse) {
		if (hasPostKode(utenlandskAdresse)) {
			if (hasBySted(utenlandskAdresse) && hasRegionDistriktOmraade(utenlandskAdresse)) {
				return joinAdresseMedKomma(utenlandskAdresse.getPostkode(), utenlandskAdresse.getBySted(), utenlandskAdresse.getRegionDistriktOmraade());
			} else {
				if (!hasBySted(utenlandskAdresse) && hasRegionDistriktOmraade(utenlandskAdresse)) {
					return joinAdresseMedKomma(utenlandskAdresse.getPostkode(), "", utenlandskAdresse.getRegionDistriktOmraade());

				} else if (hasBySted(utenlandskAdresse)) {
					return joinAdresseUtenKomma(utenlandskAdresse.getPostkode(), utenlandskAdresse.getBySted(), "");
				} else {
					return utenlandskAdresse.getPostkode();
				}
			}
		} else if (hasBySted(utenlandskAdresse)) {
			if (hasRegionDistriktOmraade(utenlandskAdresse)) {
				return joinAdresseMedKomma("", utenlandskAdresse.getBySted(), utenlandskAdresse.getRegionDistriktOmraade());
			} else {
				return utenlandskAdresse.getBySted();
			}
		} else {
			return utenlandskAdresse.getRegionDistriktOmraade();
		}
	}
}
