package no.nav.regoppslag.rreg003;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Getter
@Builder
@AllArgsConstructor
@Setter
@NoArgsConstructor
public class PostadresseRequest {
	
	@Schema(example = "889640782", description = "Fnr eller org nr som brukes som oppslagsnøkkel mot PDL eller Ereg.", requiredMode = REQUIRED)
	private String ident;
	@Schema(example = "B123", description = "Behandlingsnummer som hjemmel til å hente adresseinformasjon for identifikator.", requiredMode = NOT_REQUIRED)
	private String behandlingsnummer;
	@Schema(description = "Legacy: Tema er ikke lenger i bruk og blir forkastet i regoppslag", requiredMode = NOT_REQUIRED)
	private String tema;
}
