package no.nav.regoppslag.treg002;

import io.swagger.v3.oas.annotations.media.Schema;
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
	
	@Schema(example = "889640782", description = "Fnr eller org nr som brukes som oppslagsnøkkel mot TPS_WS eller Ereg.", required = true)
	private String identifikator;
	@Schema(example = "ORGANISASJON", description = "Sier om identifikatoren er et fnr eller et orgnr. Gyldige verdier er PERSON og ORGANISASJON", required = true, allowableValues = "PERSON, ORGANISASJON")
	private String type;
	@Schema(example = "FOR", description = "Temaet som forsendelsen tilhører, for eksempel \"FOR\" (foreldrepenger).", allowableValues = "DAG, FOR, PEN, FRI ....")
	private String tema;
}
