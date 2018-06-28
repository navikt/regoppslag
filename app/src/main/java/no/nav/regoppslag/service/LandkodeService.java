package no.nav.regoppslag.service;

import com.neovisionaries.i18n.CountryCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Source: https://unstats.un.org/unsd/methodology/m49/
 */
@Component
public class LandkodeService {

	public static final Logger LOG = LoggerFactory.getLogger(LandkodeService.class);

	public String finnLandnavn(String landkode) {
		if (CountryCode.getByCode(landkode) == null || CountryCode.getByCode(landkode).equals(CountryCode.UNDEFINED)) {
			LOG.warn("Finner ikke land for landkode: " + landkode + ", sjekk om com.neovisionaries:nv-i18n avhengigheten må oppgraderes til nyere versjon");
			return null;
		} else {
			String landNavn = CountryCode.getByCode(landkode).getName();
			if ("Norway".equalsIgnoreCase(landNavn)) {
				return "Norge";
			}
			return landNavn;
		}
	}

	public String finnLandkode(String landnavn) {

		if (landnavn.equalsIgnoreCase("Norge")) {
			landnavn = "Norway";
		}
		if (landnavn == null || CountryCode.findByName(landnavn).size() == 0) {
			return null;
		}

		List<CountryCode> countryCodeList = CountryCode.findByName(landnavn);
		return countryCodeList.get(0).getAlpha2();
	}

}
