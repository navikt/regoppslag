package no.nav.regoppslag.consumer.pdl.to;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Vegadresse {
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
