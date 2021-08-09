package no.nav.regoppslag.consumer.pdl.to;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class PdlMottakerInfo {

    private String identifikasjonsnummer;
    private LocalDate doedsdato;
    private LocalDate foedselsdato;
    private String navn;
    private String kortNavn;
    private PostadresseTo postadresse;


}