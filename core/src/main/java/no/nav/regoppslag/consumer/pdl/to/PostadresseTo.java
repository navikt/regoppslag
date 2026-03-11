package no.nav.regoppslag.consumer.pdl.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class PostadresseTo {
	AdresseKildeCode adressekilde;
	String adresseType;
	String adresselinje1;
	String adresselinje2;
	String adresselinje3;
	String postnummer;
	String poststed;
	String landkode;
}
