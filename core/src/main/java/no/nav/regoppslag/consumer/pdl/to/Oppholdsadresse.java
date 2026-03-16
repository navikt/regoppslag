package no.nav.regoppslag.consumer.pdl.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class Oppholdsadresse implements GyldigKilde {
	private LocalDateTime gyldigFraOgMed;
	private LocalDateTime gyldigTilOgMed;
	private String coAdressenavn;
	private String oppholdAnnetSted;
	private Vegadresse vegadresse;
	private Matrikkeladresse matrikkeladresse;
	private UtenlandskAdresse utenlandskAdresse;
	private Metadata metadata;
}
