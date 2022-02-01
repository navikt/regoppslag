package no.nav.regoppslag.rreg003;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PostadresseType {
	NORSKPOSTADRESSE("NorskPostadresse"),
	UTENLANDSKPOSTADRESSE("UtenlandskPostadresse");

	private final String navn;
}
