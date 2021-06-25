package no.nav.regoppslag.consumer.pdl.pdlresponse;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@SuperBuilder
public class DeltBosted extends PostAdresse{

    private LocalDate startdatoForKontrakt;
    private LocalDate sluttdatoForKontrakt;
    private String coAdressenavn;
    private UkjentBosted postboksadresse;
}
