package no.nav.regoppslag.consumer.pdl.pdlresponse;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import static java.util.Objects.nonNull;

@Data
@Builder
public class HentPerson {
    private Adressebeskyttelse adressebeskyttelse;
    private PersonNavn navn;
    private Foedsel foedsel;
    private Doedsfall doedsfall;
    private Sikkerhetstiltak sikkerhetstiltak;
    private Folkeregisteridentifikator folkeregisteridentifikator;
    private Kontaktadresse kontaktadresse;
    private Bostedsadresse bostedsadresse;
    private Oppholdsadresse oppholdsadresse;
    private DeltBosted deltBosted;
    private KontaktinformasjonForDoedsbo kontaktinformasjonForDoedsbo;
    private Folkeregisterpersonstatus folkeregisterpersonstatus;

    @Data
    public static class Adressebeskyttelse {
        private Gradering gradering;
    }

    public enum Gradering {
        STRENGT_FORTROLIG_UTLAND, STRENGT_FORTROLIG,
        FORTROLIG, UGRADERT
    }

    @Data
    public static class PersonNavn {
        private String fornavn;
        private String mellomnavn;
        private String etternavn;
        private String forkortetNavn;
    }

    @Data
    public static class Doedsfall {
        private LocalDate doedsdato;
    }

    @Data
    public static class Foedsel {
        private int foedselsaar;
        private LocalDate foedselsdato;
    }

    @Data
    public static class Sikkerhetstiltak {
        private String tiltakstype;
        private String beskrivelse;
    }

    @Data
    public static class Folkeregisteridentifikator {
        private String identifikasjonsnummer;
        private String type;
        private String status;
    }

    @Data
    public static class Folkeregisterpersonstatus {
        private String status;  //midlertidig,doed, bosatt
        private String forenkletStatus;
    }

    public String getFulltnavn() {
        return (nonNull(navn) || StringUtils.isBlank(navn.getFornavn())) ? navn.getFornavn() + " " +
                (StringUtils.isBlank(navn.getMellomnavn()) ? "" : navn.getMellomnavn() + " ") +
                navn.getEtternavn() : navn.getForkortetNavn();
    }
}
