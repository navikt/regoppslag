package no.nav.regoppslag.consumer.pdl.to;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Metadata {

	private String opplysningsId;
	private String master;
	private Endringer endringer;
	private boolean historisk;

	@Data
	@Builder
	private static class Endringer {
		private String type;
		private String registrert;
		private String registrertAv;
		private String systemkilde;
		private String kilde;
	}
}
