package no.nav.regoppslag.consumer.pdl;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UkjentBosted {
    private String bostedskommune;
}
