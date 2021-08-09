package no.nav.regoppslag.consumer.pdl.to;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Kontaktadresse {
	private LocalDateTime gyldigFraOgMed;
	private LocalDateTime gyldigTilOgMed;
	private String type;
	private String coAdressenavn;
	private PostadresseIFrittFormat postadresseIFrittFormat;
	private Vegadresse vegadresse;
	private UtenlandskAdresseIFrittFormat utenlandskAdresseIFrittFormat;
	private Postboksadresse postboksadresse;
	private no.nav.regoppslag.consumer.pdl.to.UtenlandskAdresse UtenlandskAdresse;
	private Metadata metadata;

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
