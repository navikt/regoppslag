package no.nav.regoppslag.treg002;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HentMottakerOgAdresseResponse {
	
	@Schema(example = "889640782", description = "Fødselsnummer/Orgnummer. Samme som input")
	private String identifikator;
	@Schema(example = "ARBEIDS- OG VELFERDSETATEN", description = "Navn på personen/organisasjonen")
	private String navn;
	private Treg002Adresse adresse;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Treg002Adresse {
		@Schema(example = "Postboks 5 St Olavs Plass")
		private String adresselinje1;
		@Schema(example = "adresselinje 2", nullable = true)
		private String adresselinje2;
		@Schema(example = "adresselinje 3", nullable = true)
		private String adresselinje3;
		@Schema(example = "0130")
		private String postnummer;
		@Schema(example = "OSLO")
		private String poststed;
		@Schema(example = "NO")
		private String landkode;
	}

}
