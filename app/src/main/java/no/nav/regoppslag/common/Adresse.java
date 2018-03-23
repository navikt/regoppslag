package no.nav.regoppslag.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Setter
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Adresse {
	
	private String adresselinje1;
	private String adresselinje2;
	private String adresselinje3;
	private String postnummer;
	private String poststed;
	private String landkode;
}
