package no.nav.regoppslag.consumer.pdl.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class UtenlandskAdresse {
    String adressenavnNummer;
    String bygningEtasjeLeilighet;
    String postboksNummerNavn;
    String postkode;
    String bySted;
    String regionDistriktOmraade;
    String landkode;
}
