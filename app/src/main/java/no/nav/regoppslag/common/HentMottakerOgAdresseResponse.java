package no.nav.regoppslag.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
public class HentMottakerOgAdresseResponse {
	
	private String identifikator;
	private String navn;
	private Adresse adresse;
}
