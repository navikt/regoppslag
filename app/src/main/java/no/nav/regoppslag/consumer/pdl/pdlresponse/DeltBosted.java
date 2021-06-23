package no.nav.regoppslag.consumer.pdl.pdlresponse;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DeltBosted {

    private LocalDate startdatoForKontrakt;
    private LocalDate sluttdatoForKontrakt;
    private String coAdressenavn;
    private Vegadresse vegadresse;
    private Matrikkeladresse matrikkeladresse;
    private UtenlandskAdresse utenlandskAdresse;
    private UkjentBosted postboksadresse;
}
