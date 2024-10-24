package no.nav.regoppslag.pdl;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;

import static com.neovisionaries.i18n.CountryCode.XK;
import static no.nav.regoppslag.domain.DomainConstants.KOSOVO_LANDKODE_NAV_REGISTRENE;
import static no.nav.regoppslag.domain.DomainConstants.UNKNOWN_LANDKODE;
import static no.nav.regoppslag.service.LandkodeService.finnLandkodeAlpha2FraAlpha3;
import static no.nav.regoppslag.util.SafeLoggingUtil.removeUnsafeChars;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
public class MapPDLUtils {

	public static <T> T requireNonNull(T obj, String message) {
		if (obj == null)
			throw new RegoppslagIllegalArgumentException(message, BAD_REQUEST);
		return obj;
	}

	public static String getAlpha2Landkode(String alpha3Landkode) {
		String alpha2Landkode = KOSOVO_LANDKODE_NAV_REGISTRENE.equalsIgnoreCase(alpha3Landkode) ? XK.name() : finnLandkodeAlpha2FraAlpha3(alpha3Landkode);

		if (alpha2Landkode == null) {
			log.info("Mottaker har ingen gyldig landkode registert. alpha3Landkode={}. Setter landkode={}.", removeUnsafeChars(alpha3Landkode), UNKNOWN_LANDKODE);
			return UNKNOWN_LANDKODE;
		}

		return alpha2Landkode;
	}

	public static String prependWithCareOfIfMissing(String coAdressenavn) {
		if (isBlank(coAdressenavn)) {
			return null;
		}

		if (startsWithCareOfPrefix(coAdressenavn)) {
			return coAdressenavn;
		} else {
			return String.join(" ", "C/O", coAdressenavn);
		}
	}

	private static boolean startsWithCareOfPrefix(String coAdressenavn) {
		var lowerCaseCoAdressenavn = coAdressenavn.toLowerCase();

		return lowerCaseCoAdressenavn.startsWith("c/o")
				|| lowerCaseCoAdressenavn.startsWith("℅")
				|| lowerCaseCoAdressenavn.startsWith("v/")
				// Spesialhåndtering for navn som starter med co eller ved (conrad, vedantika etc.)
				|| lowerCaseCoAdressenavn.startsWith("co ")
				|| lowerCaseCoAdressenavn.startsWith("ved ");
	}
}
