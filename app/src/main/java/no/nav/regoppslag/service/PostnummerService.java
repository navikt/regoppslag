package no.nav.regoppslag.service;

import com.google.common.io.Resources;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
/**
 * Source: https://www.bring.no/radgivning/sende-noe/adressetjenester/postnummer
 */
@Service
@Scope("singleton")
public class PostnummerService {

	private static final Logger LOG = LoggerFactory.getLogger(PostnummerService.class);
	private final static URL FILENAME = Resources.getResource("postnummerregister.txt");

	private final Map<String, PostData> postalCodeTable;

	public PostnummerService() {
		postalCodeTable = new HashMap<>();
	}

	@PostConstruct
	public void init() throws Exception {
		String line;
		String csvSplitBy = "\t";

		File filename = new File(FILENAME.getFile());
		BufferedReader br = new BufferedReader(new FileReader(filename));
		while ((line = br.readLine()) != null) {
			String[] postArray = line.split(csvSplitBy);

			PostData data = new PostData(postArray[0], postArray[1]);
			postalCodeTable.put(data.getPostnmmer(), data);
		}
		LOG.info("Har importert postnummer fra " + FILENAME);
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
