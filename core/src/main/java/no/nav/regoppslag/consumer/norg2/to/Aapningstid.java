package no.nav.regoppslag.consumer.norg2.to;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Aapningstid {
	private int id;
	private String dag;
	private String dato;
	private String fra;
	private String til;
	private String kommentar;
}
