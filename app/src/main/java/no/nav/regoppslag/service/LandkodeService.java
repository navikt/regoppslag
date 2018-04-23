package no.nav.regoppslag.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Source: https://unstats.un.org/unsd/methodology/m49/
 */
@Component
public class LandkodeService {

	public static final Logger LOG = LoggerFactory.getLogger(LandkodeService.class);
	private static final String FILENAME = "/kodeverk/countries.txt";

	private final Map<String, LandData> landkodeTable;
	private final Map<String, String> landTable;

	public LandkodeService() {
		landkodeTable = new HashMap<>();
		landTable = new HashMap<>();
	}

	@PostConstruct
	public void init() throws IOException {
		InputStream in = getClass().getResourceAsStream(FILENAME);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line;
		String csvSplitBy = "\t";

		while ((line = br.readLine()) != null) {
			String[] postArray = line.split(csvSplitBy);
			LandData data = new LandData(postArray[2], postArray[0].toUpperCase());
			landkodeTable.put(data.getLandkode(), data);
			landTable.put(data.getNavn(), data.getLandkode());
		}
		LOG.info("Har importert landkoder fra " + FILENAME);
	}

	public String finnLandnavn(String landkode) {
		if (landkodeTable.get(landkode) == null) {
			LOG.warn("Finner ikke land for landkode: " + landkode + ", sjekk om ny landkoder.txt må lastes ned/endres.");
			return null;
		} else {
			return landkodeTable.get(landkode).getNavn();
		}
	}

	public String finnLandkode(String landnavn) {
		return landTable.get(landnavn);
	}

	@Setter
	@Getter
	@AllArgsConstructor
	static class LandData {
		private String landkode;
		private String navn;
	}

}
