package no.nav.regoppslag.consumer.digdirkrr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
public class DkifResponse {

	private Map<String, String> feil;
	private Map<String, DigitalKontaktinfo> kontaktinfo;

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class DigitalKontaktinfo {
		private String spraak;
	}
}
