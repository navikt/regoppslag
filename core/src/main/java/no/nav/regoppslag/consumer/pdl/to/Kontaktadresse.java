package no.nav.regoppslag.consumer.pdl.to;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

import static java.util.Objects.nonNull;
import static no.nav.regoppslag.util.AdresseUtils.getDatoForSisteEndring;

@Data
@Builder
public class Kontaktadresse implements AdresseGyldigKilde {

	private LocalDateTime gyldigFraOgMed;
	private LocalDateTime gyldigTilOgMed;
	private String type;
	private String coAdressenavn;
	private PostadresseIFrittFormat postadresseIFrittFormat;
	private Vegadresse vegadresse;
	private UtenlandskAdresseIFrittFormat utenlandskAdresseIFrittFormat;
	private Postboksadresse postboksadresse;
	private UtenlandskAdresse UtenlandskAdresse;
	private Metadata metadata;

	@Override
	public boolean isGyldigFregKilde() {
		if (gyldigFraOgMed == null && metadata == null) {
			return false;
		}

		if (gyldigFraOgMed == null) {
			return metadata.isKildeFreg();
		}

		return gyldigFraOgMed.isBefore(LocalDateTime.now()) && metadata.isKildeFreg();
	}


	public LocalDateTime getGyldigFraOgMed(){
		return  nonNull(gyldigFraOgMed) ? gyldigFraOgMed : getDatoForSisteEndring(getMetadata().getEndringer());
	}

	@Data
	@Builder
	public static class PostadresseIFrittFormat {
		private String adresselinje1;
		private String adresselinje2;
		private String adresselinje3;
		private String postnummer;
	}

	@Data
	@Builder
	public static class UtenlandskAdresseIFrittFormat {
		private String adresselinje1;
		private String adresselinje2;
		private String adresselinje3;
		private String postkode;
		private String byEllerStedsnavn;
		private String landkode;
	}

	@Data
	@Builder
	public static class Postboksadresse {
		private String postbokseier;
		private String postboks;
		private String postnummer;
	}
}
