package no.nav.regoppslag.consumer.pdl.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Matrikkeladresse {
    private Long matrikkelId;
    private String bruksenhetsnummer;
    private String tilleggsnavn;
    private String postnummer;
    private String kommunenummer;
}
