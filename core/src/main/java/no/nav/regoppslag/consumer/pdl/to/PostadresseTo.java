package no.nav.regoppslag.consumer.pdl.to;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostadresseTo {
	private String adresseType;
	private String adresselinje1;
	private String adresselinje2;
	private String adresselinje3;
	private String postnummer;
	private String poststed;
	private String landkode;
}
