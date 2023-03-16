package no.nav.regoppslag.consumer.norg2.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stedsadresse  {
	private String type;
	private String postnummer;
	private String poststed;
	private String gatenavn;
	private String husnummer;
	private String husbokstav;
	private String adresseTilleggsnavn;
}