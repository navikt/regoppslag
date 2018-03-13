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
 * Source: https://unstats.un.org/unsd/methodology/m49/
 */
@Service
@Scope("singleton")
public class LandkodeService {

	private static final Logger LOG = LoggerFactory.getLogger(LandkodeService.class);
	private final static URL FILENAME = Resources.getResource("postnummer/countries.txt");

	private final Map<String, LandData> landkodeTable;

	public LandkodeService() {
		landkodeTable = new HashMap<>();
	}

	@PostConstruct
	public void init() throws Exception {
		String line;
		String csvSplitBy = "\t";

		File filename = new File(FILENAME.getFile());
		BufferedReader br = new BufferedReader(new FileReader(filename));
		while ((line = br.readLine()) != null) {
			String[] postArray = line.split(csvSplitBy);
			LandData data = new LandData(postArray[2], postArray[0].toUpperCase());
			landkodeTable.put(data.getLandkode(), data);
		}
		LOG.info("Har importert landkoder fra " + FILENAME);
	}

	@Setter
	@Getter
	@AllArgsConstructor
	public class LandData {

		private String landkode;
		private String navn;
	}

	public String finnLandnavn (String landkode) {
		return landkodeTable.get(landkode).getNavn();
	}

}
