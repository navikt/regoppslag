package no.nav.regoppslag.service;

import com.neovisionaries.i18n.CountryCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.neovisionaries.i18n.CountryCode.UNDEFINED;
import static com.neovisionaries.i18n.CountryCode.findByName;
import static com.neovisionaries.i18n.CountryCode.getByAlpha3Code;
import static com.neovisionaries.i18n.CountryCode.getByCode;
import static no.nav.regoppslag.util.SafeLoggingUtil.removeUnsafeChars;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Source: https://unstats.un.org/unsd/methodology/m49/
 */
@Component
@Slf4j
public class LandkodeService {

	private static final String KOSOVO = "Kosovo, Republic of";
	private static final String KOSOVO_LANDKODE_NAV_REGISTRENE = "XXK";
	private static final String NORGE = "Norge";
	private static final String NORWAY = "Norway";

	public static String finnLandnavn(String landkode) {
		/*
		 * I en periode lå Kosovo lagret på landkode XXK, men ble senere oppdatert til XKX.
		 * Det er fremdeles rester av XXK rundt om som stopper opp.
		 */
		if (KOSOVO_LANDKODE_NAV_REGISTRENE.equalsIgnoreCase(landkode)) {
			return KOSOVO;
		}

		if (getByCode(landkode) == null || getByCode(landkode).equals(UNDEFINED)) {
			log.warn("Finner ikke land for landkode={}. Sjekk om com.neovisionaries:nv-i18n avhengigheten må oppgraderes til nyere versjon", removeUnsafeChars(landkode));
			return null;
		}

		String landNavn = getByCode(landkode).getName();
		if (NORWAY.equalsIgnoreCase(landNavn)) {
			return NORGE;
		}
		return landNavn;
	}

	public static String finnLandkode(String landnavn) {
		if (isBlank(landnavn)) {
			return null;
		}

		if (landnavn.equalsIgnoreCase(NORGE)) {
			landnavn = NORWAY;
		}

		if (findByName(landnavn).isEmpty()) {
			return null;
		}

		List<CountryCode> countryCodeList = findByName(landnavn);
		return countryCodeList.getFirst().getAlpha2();
	}

	public static String finnLandkodeAlpha2FraAlpha3(String landkodeAlpha3) {
		if (isBlank(landkodeAlpha3)) {
			return null;
		}

		CountryCode countryCode = getByAlpha3Code(landkodeAlpha3);
		if (countryCode == null) {
			return null;
		}
		return countryCode.name();
	}

}
