package no.nav.regoppslag.consumer.pdl.pdlresponse;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class Kontaktadresse {
    private LocalDate gyldigFraOgMed;
    private LocalDate gyldigTilOgMed;
    private String type;
    private String coAdressenavn;
    private PostadresseIFrittFormat postadresseIFrittFormat;
    private UtenlandskAdresseIFrittFormat utenlandskAdresseIFrittFormat;
    private Postboksadresse postboksadresse;
    private UtenlandskAdresse UtenlandskAdresse;

    @Data
    static class PostadresseIFrittFormat {
        private String adresselinje1;
        private String adresselinje2;
        private String adresselinje3;
        private String postnummer;
    }


    @Data
    static class UtenlandskAdresseIFrittFormat{
        private String adresselinje1;
        private String adresselinje2;
        private String adresselinje3;
        private String postkode;
        private String byEllerStedsnavn;
        private String landkode;
    }

    @Data
    static class Postboksadresse {
        private String postbokseier;
        private String postboks;
        private String postnummer;
    }
}
