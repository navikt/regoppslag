package no.nav.regoppslag.pdl;

import no.nav.regoppslag.consumer.pdl.to.UtenlandskAdresse;

import static java.lang.String.format;
import static java.lang.String.join;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class UtenlandskAdresseUtil {
	public static boolean hasPostKode(UtenlandskAdresse utenlandskAdresse) {
		return isNotBlank(utenlandskAdresse.getPostkode());
	}

	public static boolean hasBySted(UtenlandskAdresse utenlandskAdresse) {
		return isNotBlank(utenlandskAdresse.getBySted());
	}

	public static boolean hasRegionDistriktOmraade(UtenlandskAdresse utenlandskAdresse) {
		return isNotBlank(utenlandskAdresse.getRegionDistriktOmraade());
	}

	public static String joinAdresseMedKomma(String adresse1, String adresse2, String adresse3) {
		if (isBlank(adresse2)) {
			return format("%s, %s", adresse1, adresse3).strip();
		}
		return format("%s %s, %s", adresse1, adresse2, adresse3).strip();
	}

	public static String joinAdresseUtenKomma(String adresse1, String adresse2, String adresse3) {
		return join(" ", adresse1, adresse2, adresse3).strip();
	}
}
