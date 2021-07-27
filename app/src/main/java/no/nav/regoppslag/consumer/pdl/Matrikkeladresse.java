package no.nav.regoppslag.consumer.pdl;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Matrikkeladresse {
    private Long matrikkelId;
    private String bruksenhetsnummer;
    private String tilleggsnavn;
    private String postnummer;
    private String kommunenummer;
}
