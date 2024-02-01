package no.nav.regoppslag.treg002;

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
public class HentMottakerOgAdresseRequest {
	
	@Schema(example = "889640782", description = "Fnr eller org nr som brukes som oppslagsnøkkel mot TPS_WS eller Ereg.", requiredMode = REQUIRED)
	private String identifikator;
	@Schema(example = "ORGANISASJON", description = "Sier om identifikatoren er et fnr eller et orgnr. Gyldige verdier er PERSON og ORGANISASJON", requiredMode = REQUIRED, allowableValues = "PERSON, ORGANISASJON")
	private String type;
	@Schema(description = "Legacy: Tema er ikke lenger i bruk og blir forkastet i regoppslag", requiredMode = NOT_REQUIRED)
	private String tema;
}
