package no.nav.regoppslag.consumer.pdl.pdlresponse;

import lombok.Data;

@Data
public class UtenlandskAdresse {
    private String adressenavnNummer;
    private String bygningEtasjeLeilighet;
    private String postboksNummerNavn;
    private String postkode;
    private String bySted;
    private String regionDistriktOmraade;
    private String landkode;
}
