package no.nav.regoppslag.consumer.dkif;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
	@AllArgsConstructor
	@NoArgsConstructor
	public static class DigitalKontaktinfo {
		private String spraak;
	}
}
