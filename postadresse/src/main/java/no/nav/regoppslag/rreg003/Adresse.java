package no.nav.regoppslag.rreg003;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Adresse {
	@ApiModelProperty(example = "NorskPostadresse", notes = "NorskPostadresse/UtenlandskPostadresse")
	private PostadresseType type;

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
	@ApiModelProperty(example = "Norge")
	private String land;
}