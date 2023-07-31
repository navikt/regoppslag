package no.nav.regoppslag.consumer.pdl.to;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

import static java.util.Objects.nonNull;
import static no.nav.regoppslag.util.AdresseUtils.getDatoForSisteEndring;

@Data
@Builder
public class Oppholdsadresse implements AdresseGyldigKilde {
	private LocalDateTime gyldigFraOgMed;
	private LocalDateTime gyldigTilOgMed;
	private String coAdressenavn;
	private String oppholdAnnetSted;
	private Vegadresse vegadresse;
	private Matrikkeladresse matrikkeladresse;
	private UtenlandskAdresse utenlandskAdresse;
	private Metadata metadata;


	public LocalDateTime getGyldigFraOgMed(){
		return  nonNull(gyldigFraOgMed) ? gyldigFraOgMed : getDatoForSisteEndring(getMetadata().getEndringer());
	}
}
