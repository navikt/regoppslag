package no.nav.regoppslag.consumer.map;

import lombok.Builder;
import lombok.Data;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Data
@Builder
public class Postadresse {

	private String adresseType;
	private String adresselinje1;
	private String adresselinje2;
	private String adresselinje3;
	private String adresselinje4;
	private String postnummer;
	private String poststed;
	private String land;

}
