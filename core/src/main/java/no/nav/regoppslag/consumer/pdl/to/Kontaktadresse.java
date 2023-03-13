package no.nav.regoppslag.consumer.pdl.to;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Kontaktadresse implements Comparable<Kontaktadresse>, AdresseGyldigKilde {
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
	public boolean isGyldigPdlKilde() {
		if(metadata == null) {
			return false;
		}
		return metadata.isKildePdl();
	}

	@Override
	public boolean isGyldigFregKilde() {
		if(gyldigFraOgMed == null && metadata == null) {
			return false;
		}
		if(gyldigFraOgMed == null) {
			return metadata.isKildeFreg();
		}
		return gyldigFraOgMed.isBefore(LocalDateTime.now()) && metadata.isKildeFreg();
	}

	@Override
	public int compareTo(Kontaktadresse o) {
		if(gyldigFraOgMed == null || o.getGyldigFraOgMed() == null) {
			return 0;
		}

		return gyldigFraOgMed.compareTo(o.getGyldigFraOgMed());
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
