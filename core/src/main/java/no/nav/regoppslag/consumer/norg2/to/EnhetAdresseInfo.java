package no.nav.regoppslag.consumer.norg2.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnhetAdresseInfo {
	private String enhetId;
	private String enhetsNavn;
	private String kontaktTelefonnummer;
	private Postadresse adresse;

	@Data
	@Builder
	public static class Postadresse {
		private String adresselinje1;
		private String adresselinje2;
		private String adresselinje3;
		private String postnummer;
		private String poststed;
		private String land;
	}
}
