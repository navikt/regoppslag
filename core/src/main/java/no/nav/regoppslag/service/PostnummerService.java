package no.nav.regoppslag.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Kilde: https://www.bring.no/tjenester/adressetjenester/postnummer
 * Hent ned Postnummertabell som "Tab-separerte felter (ANSI)" og lim inn i postnummerregister.txt
 */
@Slf4j
@Component
public class PostnummerService {

	private static final String FILENAME = "/kodeverk/postnummerregister.txt";

	private final Map<String, PostData> postalCodeTable;

	@Autowired
	public PostnummerService() throws IOException {
		postalCodeTable = new HashMap<>();
		init();
	}

	void init() throws IOException {
		try (InputStream in = getClass().getResourceAsStream(FILENAME);
			 BufferedReader br = new BufferedReader(new InputStreamReader(in))) {

			String line;
			String csvSplitBy = "\t";

			while ((line = br.readLine()) != null) {
				String[] postArray = line.split(csvSplitBy);

				PostData data = new PostData(postArray[0], postArray[1]);
				postalCodeTable.put(data.getPostnummer(), data);
			}
			workaround();
		} finally {
			log.info("Har importert postnummer kodeverk fra fil={}, antall={}", FILENAME, postalCodeTable.size());
		}
	}

	private void workaround() {
		// Workaround for postnummer som ikke er i postnummerregister.txt
		postalCodeTable.put("0025", new PostData("0025", "OSLO"));
		postalCodeTable.put("6485", new PostData("6485", "HARØY"));
	}

	@Setter
	@Getter
	@AllArgsConstructor
	static class PostData {
		private String postnummer;
		private String poststed;
	}

	public String finnPoststed(String postnr) {
		if (postalCodeTable.get(postnr) == null) {
			log.error("Finner ikke poststed for postnummer={}. Sjekk om ny postnummerregister.txt må lastes ned eller om postnummer er gammelt og har fått en endring. Se https://www.bring.no/tjenester/adressetjenester/postnummer", postnr);
			return null;
		} else {
			return postalCodeTable.get(postnr).getPoststed();
		}
	}
}
