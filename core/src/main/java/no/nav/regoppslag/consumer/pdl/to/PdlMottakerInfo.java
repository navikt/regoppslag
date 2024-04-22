package no.nav.regoppslag.consumer.pdl.to;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
public class PdlMottakerInfo {
	private String identifikasjonsnummer;
	private LocalDate doedsdato;
	private String navn;
	private String kortNavn;
	private PostadresseTo postadresse;
	private Set<String> adressebeskyttelseType;
}