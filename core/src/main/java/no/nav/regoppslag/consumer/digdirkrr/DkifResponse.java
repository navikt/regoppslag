package no.nav.regoppslag.consumer.digdirkrr;

import java.util.Map;

public record DkifResponse(Map<String, String> feil, Map<String, DigitalKontaktinfo> kontaktinfo) {

	public record DigitalKontaktinfo(String spraak) {
	}
}
