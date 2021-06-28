package no.nav.regoppslag.consumer.pdl.pdlresponse;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
public class Oppholdsadresse {
    private LocalDateTime gyldigFraOgMed;
    private LocalDateTime gyldigTilOgMed;
    private String coAdressenavn;
    private String oppholdAnnetSted;
    private UkjentBosted ukjentBosted;
    private Vegadresse vegadresse;
    private Matrikkeladresse matrikkeladresse;
    private UtenlandskAdresse utenlandskAdresse;
}
