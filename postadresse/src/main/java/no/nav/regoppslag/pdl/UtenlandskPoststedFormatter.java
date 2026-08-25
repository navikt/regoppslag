package no.nav.regoppslag.pdl;

import no.nav.regoppslag.consumer.pdl.to.UtenlandskAdresse;

import static java.lang.String.format;
import static java.lang.String.join;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

final class UtenlandskPoststedFormatter {

	private UtenlandskPoststedFormatter() {
	}

	static String formatUSAogKanadaPostkodeBystedOgOmraade(UtenlandskAdresse utenlandskAdresse) {

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

	static String formatPostkodeBystedOgOmraade(UtenlandskAdresse utenlandskAdresse) {
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

	private static boolean hasPostKode(UtenlandskAdresse utenlandskAdresse) {
		return isNotBlank(utenlandskAdresse.getPostkode());
	}

	private static boolean hasBySted(UtenlandskAdresse utenlandskAdresse) {
		return isNotBlank(utenlandskAdresse.getBySted());
	}

	private static boolean hasRegionDistriktOmraade(UtenlandskAdresse utenlandskAdresse) {
		return isNotBlank(utenlandskAdresse.getRegionDistriktOmraade());
	}

	private static String joinAdresseMedKomma(String adresse1, String adresse2, String adresse3) {
		if (isBlank(adresse2)) {
			return format("%s, %s", adresse1, adresse3).strip();
		}
		return format("%s %s, %s", adresse1, adresse2, adresse3).strip();
	}

	private static String joinAdresseUtenKomma(String adresse1, String adresse2, String adresse3) {
		return join(" ", adresse1, adresse2, adresse3).strip();
	}
}
