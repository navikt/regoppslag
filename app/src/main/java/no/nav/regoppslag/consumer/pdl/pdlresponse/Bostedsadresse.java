package no.nav.regoppslag.consumer.pdl.pdlresponse;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@SuperBuilder
public class Bostedsadresse extends PostAdresse {
    private LocalDate angittFlyttedato;
    private LocalDateTime gyldigFraOgMed;
    private LocalDateTime gyldigTilOgMed;
    private String coAdressenavn;
    private UkjentBosted ukjentBosted;
}
