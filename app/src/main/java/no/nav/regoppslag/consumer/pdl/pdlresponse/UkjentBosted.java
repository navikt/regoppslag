package no.nav.regoppslag.consumer.pdl.pdlresponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UkjentBosted {
    private String bostedskommune;
}
