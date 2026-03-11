package no.nav.regoppslag.consumer.pdl.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
@Builder
@AllArgsConstructor
public class Bostedsadresse implements GyldigKilde {
	LocalDate angittFlyttedato;
	LocalDateTime gyldigFraOgMed;
	LocalDateTime gyldigTilOgMed;
	String coAdressenavn;
	Vegadresse vegadresse;
	Matrikkeladresse matrikkeladresse;
	UtenlandskAdresse utenlandskAdresse;
	UkjentBosted ukjentBosted;
	Metadata metadata;

}
