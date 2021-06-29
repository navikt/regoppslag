package no.nav.regoppslag.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
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
/**
 * Source: https://www.bring.no/radgivning/sende-noe/adressetjenester/postnummer
 */
@Component
public class PostnummerService {

	public static final Logger LOG = LoggerFactory.getLogger(PostnummerService.class);
	private static final String FILENAME = "/kodeverk/postnummerregister.txt";

	private final Map<String, PostData> postalCodeTable = new HashMap<>();

	@Inject
	public PostnummerService() {
	}

	@PostConstruct
	public void init() throws IOException {
		
		InputStream in = getClass().getResourceAsStream(FILENAME);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		String line;
		String csvSplitBy = "\t";

		while ((line = br.readLine()) != null) {
			String[] postArray = line.split(csvSplitBy);

			PostData data = new PostData(postArray[0], postArray[1]);
			postalCodeTable.put(data.getPostnmmer(), data);
		}
		LOG.info("Har importert kodeverk fra " + FILENAME);
	}

	@Setter
	@Getter
	@AllArgsConstructor
	static class PostData {

		private String postnmmer;
		private String poststed;
	}

	@SneakyThrows
	public String finnPoststed (String postnr) {
		init();
		if (postalCodeTable.get(postnr) == null) {
			LOG.warn("Finner ikke poststed for postnummer: " + postnr + ", sjekk om ny postnummer.txt må lastes ned.");
			return null;
		} else {
			return postalCodeTable.get(postnr).getPoststed();
		}
	}
}
