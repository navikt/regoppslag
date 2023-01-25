package no.nav.regoppslag.rreg003;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostadresseResponse {

	@Schema(example = "ARBEIDS- OG VELFERDSETATEN", description = "Navn på personen/organisasjonen")
	private String navn;

	private Adresse adresse;

}
