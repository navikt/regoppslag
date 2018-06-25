package no.nav.regoppslag.service;

import com.neovisionaries.i18n.CountryCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
			return CountryCode.getByCode(landkode).getName();
		}
	}

	public String finnLandkode(String landnavn) {

		if (landnavn == null || CountryCode.findByName(landnavn).size() == 0) {
			return null;
		}

		List<CountryCode> countryCodeList = CountryCode.findByName(landnavn);
		return countryCodeList.get(0).getAlpha2();
	}

}
