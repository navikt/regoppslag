package no.nav.regoppslag.consumer.pdl.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@Builder
@AllArgsConstructor
public class Vegadresse {
	private Long matrikkelId;
	private String husnummer;
	private String husbokstav;
	private String bruksenhetsnummer;
	private String adressenavn;
	private String kommunenummer;
	private String bydelsnummer;
	private String tilleggsnavn;
	private String postnummer;

	public String mapAdresselinjeFromVegadresse() {
		return Stream.of(getAdressenavn(), " ", getHusnummer(), getHusbokstav())
				.map(string -> string == null ? "" : string)
				.collect(Collectors.joining());
	}
}
