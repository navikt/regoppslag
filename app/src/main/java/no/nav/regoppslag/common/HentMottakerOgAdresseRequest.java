package no.nav.regoppslag.common;

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
	
	private String identifikator;
	private String type;
}
