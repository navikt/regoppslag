package no.nav.regoppslag.consumer.pdl.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.Set;

@Value
@Builder
@AllArgsConstructor
public class PdlMottakerInfo {
	String identifikasjonsnummer;
	LocalDate doedsdato;
	String navn;
	String kortNavn;
	PostadresseTo postadresse;
	Set<String> adressebeskyttelseType;
}
