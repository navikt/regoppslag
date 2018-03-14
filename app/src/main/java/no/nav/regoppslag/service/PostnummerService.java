package no.nav.regoppslag.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
/**
 * Source: https://www.bring.no/radgivning/sende-noe/adressetjenester/postnummer
 */
@Service
@Scope("singleton")
public class PostnummerService {

	private static final Logger LOG = LoggerFactory.getLogger(PostnummerService.class);
	private static final String FILENAME = "/kodeverk/postnummerregister.txt";

	private final Map<String, PostData> postalCodeTable;

	public PostnummerService() {
		postalCodeTable = new HashMap<>();
	}

	@PostConstruct
	public void init() throws Exception {
		
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
	public class PostData {

		private String postnmmer;
		private String poststed;
	}

	public String finnPoststed (String postnr) {
		return postalCodeTable.get(postnr).getPoststed();
	}
}
