package no.nav.regoppslag.consumer.norg2.to;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnhetNavn {
	private String enhetNr;
	private String navn;
	private String status;
}
