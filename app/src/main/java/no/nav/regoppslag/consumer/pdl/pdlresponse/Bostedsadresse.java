package no.nav.regoppslag.consumer.pdl.pdlresponse;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class Bostedsadresse {
    private LocalDate angittFlyttedato;
    private LocalDateTime gyldigFraOgMed;
    private LocalDateTime gyldigTilOgMed;
    private String coAdressenavn;
    private Vegadresse vegadresse;
    private Matrikkeladresse matrikkeladresse;
    private UtenlandskAdresse utenlandskAdresse;
    private UkjentBosted ukjentBosted;
}
