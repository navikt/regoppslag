package no.nav.regoppslag.consumer.pdl.pdlresponse;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class DeltBosted{
    private LocalDate startdatoForKontrakt;
    private LocalDate sluttdatoForKontrakt;
    private String coAdressenavn;
    private UkjentBosted ukjentBosted;
    private Vegadresse vegadresse;
    private UtenlandskAdresse utenlandskAdresse;
    private Matrikkeladresse matrikkeladresse;
}
