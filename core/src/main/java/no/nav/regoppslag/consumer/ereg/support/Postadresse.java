package no.nav.regoppslag.consumer.ereg.support;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Postadresse {
	protected Bruksperiode bruksperiode;
	protected Gyldighetsperiode gyldighetsperiode;
	protected String adresselinje1;
	protected String adresselinje2;
	protected String adresselinje3;
	protected String kommunenummer;
	protected String landkode;
	protected String postnummer;
	protected String poststed;
}
