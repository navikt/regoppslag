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
@Schema(description = "Adresseinformasjon med kilde og type")
public class Adresse {
	@Schema(example = "Bostedsadresse", description = "Kilde for adressen: Oppholdsadresse, Kontaktadresse eller KontaktinformasjonForDødsbo")
	private AdresseKildeCode adresseKilde;
	@Schema(example = "NorskPostadresse", description = "Type postadresse: NorskPostadresse eller UtenlandskPostadresse")
	private PostadresseType type;

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
	@Schema(example = "Norge", description = "Navn på land")
	private String land;
}