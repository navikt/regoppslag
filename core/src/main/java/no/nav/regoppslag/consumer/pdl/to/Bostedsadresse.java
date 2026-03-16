package no.nav.regoppslag.consumer.pdl.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class Bostedsadresse implements GyldigKilde {
	private LocalDate angittFlyttedato;
	private LocalDateTime gyldigFraOgMed;
	private LocalDateTime gyldigTilOgMed;
	private String coAdressenavn;
	private Vegadresse vegadresse;
	private Matrikkeladresse matrikkeladresse;
	private UtenlandskAdresse utenlandskAdresse;
	private UkjentBosted ukjentBosted;
	private Metadata metadata;

}
