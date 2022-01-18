package no.nav.regoppslag.rreg003;

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
public class PostadresseResponse {

	//@ApiModelProperty(example = "889640782", notes = "Fødselsnummer/Orgnummer. Samme som input")
	//private String identifikator;
	@ApiModelProperty(example = "ARBEIDS- OG VELFERDSETATEN", notes = "Navn på personen/organisasjonen")
	private String navn;
	private Adresse adresse;


}
