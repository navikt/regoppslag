package no.nav.regoppslag.consumer.pdl.to;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Oppholdsadresse {
	private LocalDateTime gyldigFraOgMed;
	private LocalDateTime gyldigTilOgMed;
	private String coAdressenavn;
	private String oppholdAnnetSted;
	private Vegadresse vegadresse;
	private Matrikkeladresse matrikkeladresse;
	private UtenlandskAdresse utenlandskAdresse;
	private Metadata metadata;

	public boolean isGyldigPdlKilde() {
		if(metadata == null) {
			return false;
		}
		return metadata.isKildePdl();
	}

	public boolean isGyldigFregKilde() {
		if(metadata == null) {
			return false;
		}
		return metadata.isKildeFreg();
	}
}
