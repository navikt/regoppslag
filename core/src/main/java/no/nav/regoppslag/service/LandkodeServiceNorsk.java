package no.nav.regoppslag.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class LandkodeServiceNorsk {

	private static final String FILENAME = "/kodeverk/landkoderISO2.txt";

	private final Map<String, String> landkodeTable;

	public LandkodeServiceNorsk() throws IOException {
		landkodeTable = new HashMap<>();
		init();
	}

	void init() throws IOException {
		try (InputStream in = LandkodeServiceNorsk.class.getResourceAsStream(FILENAME);
			 BufferedReader br = new BufferedReader(new InputStreamReader(in))) {

			String line;
			String csvSplitBy = "\t";

			while ((line = br.readLine()) != null) {
				String[] kodeArray = line.split(csvSplitBy);

				landkodeTable.put(kodeArray[0], kodeArray[1]);
			}
			log.info("Har importert kodeverk fra " + FILENAME);
		}
	}

	public String finnLand(String landKode) {
		if (landkodeTable.get(landKode) == null) {
			log.warn("Finner ikke landsnavn for landskode: {}, sjekk om ny landkoderISO2.txt må lastes ned.", landKode);
			return null;
		} else {
			return landkodeTable.get(landKode);
		}
	}

}
