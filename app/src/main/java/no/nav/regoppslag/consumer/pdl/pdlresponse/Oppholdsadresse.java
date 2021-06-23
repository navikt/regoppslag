package no.nav.regoppslag.consumer.pdl.pdlresponse;

import lombok.Builder;
import lombok.Data;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Matrikkeladresse;

import java.time.LocalDateTime;

@Data
@Builder
public class Oppholdsadresse {
    private LocalDateTime gyldigFraOgMed;
    private LocalDateTime gyldigTilOgMed;
    private String coAdressenavn;
    private UtenlandskAdresse utenlandskAdresse;
    private Vegadresse vegadresse;
    private Matrikkeladresse matrikkeladresse;
    private String oppholdAnnetSted;
}
