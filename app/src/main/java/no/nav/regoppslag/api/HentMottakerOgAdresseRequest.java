package no.nav.regoppslag.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Getter
@Builder
@AllArgsConstructor
@Setter
@NoArgsConstructor
public class HentMottakerOgAdresseRequest {
	
	@ApiModelProperty(example = "889640782", notes = "Fnr eller org nr som brukes som oppslagsnøkkel mot TPS_WS eller Ereg.", required = true)
	private String identifikator;
	@ApiModelProperty(example = "ORGANISASJON", notes = "Sier om identifikatoren er et fnr eller et orgnr. Gyldige verdier er PERSON og ORGANISASJON", required = true, allowableValues = "PERSON, ORGANISASJON")
	private String type;
}
