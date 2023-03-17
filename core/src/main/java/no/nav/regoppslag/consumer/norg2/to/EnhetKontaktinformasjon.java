package no.nav.regoppslag.consumer.norg2.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnhetKontaktinformasjon {
	private int id;
	private String enhetNr;
	private String telefonnummer;
	private String telefonnummerKommentar;
	private String faksnummer;
	private Epost epost;
	private Adresse postadresse;
	private Stedsadresse besoeksadresse;
	private String spesielleOpplysninger;

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Epost {
		private String adresse;
		private String kommentar;
		private boolean kunIntern;
	}
}
