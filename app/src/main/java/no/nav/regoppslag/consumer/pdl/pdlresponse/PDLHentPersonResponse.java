package no.nav.regoppslag.consumer.pdl.pdlresponse;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class PDLHentPersonResponse {

    private List<PdlError> errors;
    private PDLHentPerson data;

    @Data
    public static class PDLHentPerson {
        private HentPerson hentPerson;
    }

    @Data
    static class HentPerson {
        private PersonNavn navn;
        private Foedsel foedsel;
        private Doedsfall doedsfall;
        private Sikkerhetstiltak sikkerhetstiltak;
        private Folkeregisteridentifikator folkeregisteridentifikator;
        private Kontaktadresse kontaktadresse;
        private Bostedsadresse bostedsadresse;
        private DeltBosted deltBosted;

    }

    @Data
    static class PersonNavn {
        private String fornavn;
        private String mellomnavn;
        private String etternavn;
        private String forkortetNavn;
    }

    @Data
    static class Doedsfall {
        private LocalDate doedsdato;
    }

    @Data
    static class Foedsel {
        private int foedselsaar;
        private LocalDate foedselsdato;
        private String foedeland;
        private String foedested;
        private String foedekommune;
    }

    @Data
    static class Sikkerhetstiltak {
        private String tiltakstype;
        private String beskrivelse;
    }

    @Data
    static class Folkeregisteridentifikator {
        private String identifikasjonsnummer;
        private String type;
        private String status;
    }

    @Data
    static class PdlError {
        private String message;
        private PdlErrorExtensionTo extensions;
    }

    @Data
    static class PdlErrorExtensionTo {
        private String code;
        private ErrorDetails details;
        private String classification;
    }

    @Data
    static class ErrorDetails {
        private String type;
        private String cause;
        private String policy;
    }
}
