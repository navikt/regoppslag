package no.nav.regoppslag.consumer.dkif;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class DkifResponse {

	private Map<String, Melding> feil;
	private Map<String, DigitalKontaktinfo> kontaktinfo;

	@Data
	public static class Melding {
		private String melding;
	}

	@Data
	@Builder
	public static class DigitalKontaktinfo {
		private String spraak;
	}
}
