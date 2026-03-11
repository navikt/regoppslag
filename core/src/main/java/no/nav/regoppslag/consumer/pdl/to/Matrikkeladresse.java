package no.nav.regoppslag.consumer.pdl.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class Matrikkeladresse {
    Long matrikkelId;
    String bruksenhetsnummer;
    String tilleggsnavn;
    String postnummer;
    String kommunenummer;
}
