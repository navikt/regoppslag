package no.nav.regoppslag.consumer.pdl.to;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;

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
    private Metadata metadata;

    public LocalDateTime getGyldigFraOgMed(){
        return  nonNull(gyldigFraOgMed) ? gyldigFraOgMed : getDatoForSisteEndring(getMetadata().getEndringer());
    }

    private LocalDateTime getDatoForSisteEndring(List<Endring> endringer) {
        endringer.sort(comparing(Endring::getRegistrert));
        int antallEndringer = endringer.size();
        return antallEndringer > 0 ? endringer.get(antallEndringer - 1).getRegistrert() : null;

    }
}
