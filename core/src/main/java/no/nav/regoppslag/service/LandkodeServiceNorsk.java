package no.nav.regoppslag.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class LandkodeServiceNorsk {

	public static final Logger LOG = LoggerFactory.getLogger(LandkodeServiceNorsk.class);
	private static final String FILENAME = "/kodeverk/landkoderISO2.txt";

	private final Map<String, String> landkodeTable;

	@Inject
	public LandkodeServiceNorsk() throws IOException {
		landkodeTable = new HashMap<>();
		init();
	}

	@PostConstruct
	public void init() throws IOException {

		InputStream in = getClass().getResourceAsStream(FILENAME);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		String line;
		String csvSplitBy = "\t";

		while ((line = br.readLine()) != null) {
			String[] kodeArray = line.split(csvSplitBy);

			landkodeTable.put(kodeArray[0], kodeArray[1]);
		}
		LOG.info("Har importert kodeverk fra " + FILENAME);
	}

	public String finnLand(String landKode) {
		if (landkodeTable.get(landKode) == null) {
			LOG.warn("Finner ikke landsnavn for landskode: " + landKode + ", sjekk om ny landkoderISO2.txt må lastes ned.");
			return null;
		} else {
			return landkodeTable.get(landKode);
		}
	}

}
