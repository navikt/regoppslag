package no.nav.regoppslag.consumer.pdl.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
@AllArgsConstructor
public class Oppholdsadresse implements GyldigKilde {
	LocalDateTime gyldigFraOgMed;
	LocalDateTime gyldigTilOgMed;
	String coAdressenavn;
	String oppholdAnnetSted;
	Vegadresse vegadresse;
	Matrikkeladresse matrikkeladresse;
	UtenlandskAdresse utenlandskAdresse;
	Metadata metadata;
}
