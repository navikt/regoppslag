package no.nav.regoppslag.consumer.pdl;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class MottakerPostInfo {

    private String identifikasjonsnummer;
    private LocalDate doedsdato;
    private LocalDate foedselsdato;
    private String mottakerNavn;
    private NorskPostadresse postadresse;


    @Data
    @Builder
    public static class NorskPostadresse {
        private String adresseType;
        private String adresselinje1;
        private String adresselinje2;
        private String adresselinje3;
        private String adresselinje4;
        private String postnummer;
        private String poststed;
        private String land;
    }
}