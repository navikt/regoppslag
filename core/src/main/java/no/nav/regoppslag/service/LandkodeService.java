package no.nav.regoppslag.service;

import com.neovisionaries.i18n.CountryCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Source: https://unstats.un.org/unsd/methodology/m49/
 */
@Component
public class LandkodeService {

	public static final Logger LOG = LoggerFactory.getLogger(LandkodeService.class);
	private static final String KOSOVO = "Kosovo, Republic of";
	private static final String KOSOVO_LANDKODE_NAV_REGISTRENE = "XXK";
	private static final String NORGE = "Norge";
	private static final String NORWAY = "Norway";

	public String finnLandnavn(String landkode) {

		/*
		 * Spesialtilfelle. I en periode lå Kosovo lagret på landkode XXK, mens den senere ble oppdatert til XKX.
		 * Det er fremdeles rester av XXK rundt om som stopper opp.
		 */
		if (KOSOVO_LANDKODE_NAV_REGISTRENE.equalsIgnoreCase(landkode)) {
			return KOSOVO;
		} else if (CountryCode.getByCode(landkode) == null || CountryCode.getByCode(landkode).equals(CountryCode.UNDEFINED)) {
			LOG.warn("Finner ikke land for landkode: " + landkode + ", sjekk om com.neovisionaries:nv-i18n avhengigheten må oppgraderes til nyere versjon");
			return null;
		} else {
			String landNavn = CountryCode.getByCode(landkode).getName();
			if (NORWAY.equalsIgnoreCase(landNavn)) {
				return NORGE;
			}
			return landNavn;
		}
	}

	public String finnLandkode(String landnavn) {

		if (StringUtils.isBlank(landnavn)) {
			return null;
		}

		if (landnavn.equalsIgnoreCase(NORGE)) {
			landnavn = NORWAY;
		}

		if (CountryCode.findByName(landnavn).size() == 0) {
			return null;
		}

		List<CountryCode> countryCodeList = CountryCode.findByName(landnavn);
		return countryCodeList.get(0).getAlpha2();
	}

	public String finnLandkodeAlpha2FraAlpha3(String landkodeAlpha3) {
		 return StringUtils.isBlank(landkodeAlpha3) ? null : CountryCode.getByAlpha3Code(landkodeAlpha3).name();
	}

}
