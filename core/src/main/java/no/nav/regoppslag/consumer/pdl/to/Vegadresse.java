package no.nav.regoppslag.consumer.pdl.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value
@Builder
@AllArgsConstructor
public class Vegadresse {
	Long matrikkelId;
	String husnummer;
	String husbokstav;
	String bruksenhetsnummer;
	String adressenavn;
	String kommunenummer;
	String bydelsnummer;
	String tilleggsnavn;
	String postnummer;

	public String mapAdresselinjeFromVegadresse() {
		return Stream.of(getAdressenavn(), " ", getHusnummer(), getHusbokstav())
				.map(string -> string == null ? "" : string)
				.collect(Collectors.joining());
	}
}
