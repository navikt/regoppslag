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
@Schema(description = "Respons med mottakerinformasjon og adresse")
public class HentMottakerOgAdresseResponse {
	
	@Schema(example = "889640782", description = "Fødselsnummer/Orgnummer. Samme som input")
	private String identifikator;
	@Schema(example = "ARBEIDS- OG VELFERDSETATEN", description = "Navn på personen/organisasjonen")
	private String navn;
	@Schema(description = "Adresseinformasjon for mottaker")
	private Treg002Adresse adresse;

	@Schema(description = "Adresseinformasjon med norsk eller utenlandsk postadresse")
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Treg002Adresse {
		@Schema(example = "Postboks 5 St Olavs Plass", description = "Første adresselinje")
		private String adresselinje1;
		@Schema(example = "adresselinje 2", nullable = true, description = "Andre adresselinje")
		private String adresselinje2;
		@Schema(example = "adresselinje 3", nullable = true, description = "Tredje adresselinje")
		private String adresselinje3;
		@Schema(example = "0130", description = "Postnummer")
		private String postnummer;
		@Schema(example = "OSLO", description = "Poststed")
		private String poststed;
		@Schema(example = "NO", description = "Landkode (ISO 3166-1 alpha-2)")
		private String landkode;
	}

}
