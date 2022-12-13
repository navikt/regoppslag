package no.nav.regoppslag.rreg003;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Adresse {
	@Schema(example = "Bostedsadresse", description = "Oppholdsadresse/Kontaktadresse/KontaktinformasjonForDødsbo/")
	private AdresseKildeCode adresseKilde;
	@Schema(example = "NorskPostadresse", description = "NorskPostadresse/UtenlandskPostadresse")
	private PostadresseType type;

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
	@Schema(example = "Norge")
	private String land;
}