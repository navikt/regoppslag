package no.nav.regoppslag.consumer.pdl;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
public class MottakerPostInfo {

    private String identifikasjonsnummer;
    private LocalDate doedsdato;
    private LocalDate foedselsdato;
    private String mottakerNavn;
    private PostadresseTo postadresse;

}