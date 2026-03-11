package no.nav.regoppslag.consumer.pdl.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
@AllArgsConstructor
public class Kontaktadresse implements GyldigKilde {

	LocalDateTime gyldigFraOgMed;
	LocalDateTime gyldigTilOgMed;
	String type;
	String coAdressenavn;
	PostadresseIFrittFormat postadresseIFrittFormat;
	Vegadresse vegadresse;
	UtenlandskAdresseIFrittFormat utenlandskAdresseIFrittFormat;
	Postboksadresse postboksadresse;
	UtenlandskAdresse utenlandskAdresse;
	Metadata metadata;

	@Override
	public boolean isGyldigFregKilde(LocalDateTime atTime) {
		if (gyldigFraOgMed == null && metadata == null) {
			return false;
		}

		if (gyldigFraOgMed == null) {
			return metadata.isKildeFreg();
		}

		return gyldigFraOgMed.isBefore(LocalDateTime.now()) && metadata.isKildeFreg() && isNotExpired(atTime);
	}

	@Value
	@Builder
	@AllArgsConstructor
	public static class PostadresseIFrittFormat {
		String adresselinje1;
		String adresselinje2;
		String adresselinje3;
		String postnummer;
	}

	@Value
	@Builder
	@AllArgsConstructor
	public static class UtenlandskAdresseIFrittFormat {
		String adresselinje1;
		String adresselinje2;
		String adresselinje3;
		String postkode;
		String byEllerStedsnavn;
		String landkode;
	}

	@Value
	@Builder
	@AllArgsConstructor
	public static class Postboksadresse {
		String postbokseier;
		String postboks;
		String postnummer;
	}
}
