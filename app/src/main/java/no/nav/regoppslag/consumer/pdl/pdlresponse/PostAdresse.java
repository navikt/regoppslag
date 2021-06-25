package no.nav.regoppslag.consumer.pdl.pdlresponse;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public abstract class PostAdresse {

    @Data
    public static class UtenlandskAdresse {
        private String adressenavnNummer;
        private String bygningEtasjeLeilighet;
        private String postboksNummerNavn;
        private String postkode;
        private String bySted;
        private String regionDistriktOmraade;
        private String landkode;
    }

    @Data
    public static class Vegadresse {
        private Long matrikkelId;
        private String husnummer;
        private String husbokstav;
        private String bruksenhetsnummer;
        private String adressenavn;
        private String kommunenummer;
        private String bydelsnummer;
        private String tilleggsnavn;
        private String postnummer;
    }

    @Data
    public class Matrikkeladresse {
        private Long matrikkelId;
        private String bruksenhetsnummer;
        private String tilleggsnavn;
        private String postnummer;
        private String kommunenummer;
    }
}
