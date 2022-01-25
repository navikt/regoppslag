package no.nav.regoppslag.rreg003;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@AllArgsConstructor
@Setter
@NoArgsConstructor
public class PostadresseRequest {
	
	@Schema(example = "889640782", description = "Fnr eller org nr som brukes som oppslagsnøkkel mot PDL eller Ereg.", required = true)
	private String ident;
	@Schema(example = "FOR", description = "Tema som hjemmel til å hente adresseinformasjon for identifikator. Eksempel: \"FOR\" (Foreldrepenger).", required = false, allowableValues = "DAG, FOR, PEN, FRI ....")
	private String tema;
}
