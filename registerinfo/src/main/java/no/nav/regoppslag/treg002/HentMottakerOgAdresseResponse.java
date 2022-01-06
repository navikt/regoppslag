package no.nav.regoppslag.treg002;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HentMottakerOgAdresseResponse {
	
	@ApiModelProperty(example = "889640782", notes = "Fødselsnummer/Orgnummer. Samme som input")
	private String identifikator;
	@ApiModelProperty(example = "ARBEIDS- OG VELFERDSETATEN", notes = "Navn på personen/organisasjonen")
	private String navn;
	private Adresse adresse;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Adresse {
		@ApiModelProperty(example = "Postboks 5 St Olavs Plass")
		private String adresselinje1;
		@ApiModelProperty(example = "null")
		private String adresselinje2;
		@ApiModelProperty(example = "null")
		private String adresselinje3;
		@ApiModelProperty(example = "0130")
		private String postnummer;
		@ApiModelProperty(example = "OSLO")
		private String poststed;
		@ApiModelProperty(example = "NO")
		private String landkode;
	}
}
