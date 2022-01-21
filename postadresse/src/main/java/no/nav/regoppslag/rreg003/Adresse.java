package no.nav.regoppslag.rreg003;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Adresse {
	@Schema(example = "NorskPostadresse", description = "NorskPostadresse/UtenlandskPostadresse")
	private PostadresseType type;

	@Schema(example = "Postboks 5 St Olavs Plass")
	private String adresselinje1;
	@Schema(example = "null")
	private String adresselinje2;
	@Schema(example = "null")
	private String adresselinje3;
	@Schema(example = "0130")
	private String postnummer;
	@Schema(example = "OSLO")
	private String poststed;
	@Schema(example = "NO")
	private String landkode;
	@Schema(example = "Norge")
	private String land;
}