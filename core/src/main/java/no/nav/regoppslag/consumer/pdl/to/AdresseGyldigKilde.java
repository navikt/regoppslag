package no.nav.regoppslag.consumer.pdl.to;

import java.time.LocalDateTime;

import static java.util.Objects.nonNull;

public interface AdresseGyldigKilde extends Comparable<AdresseGyldigKilde> {
	LocalDateTime getGyldigFraOgMed();
	LocalDateTime getGyldigTilOgMed();

	Metadata getMetadata();

	default boolean isGyldigPdlKilde() {
		if (getMetadata() == null) {
			return false;
		}
		return getMetadata().isKildePdl() && isNotExpired();
	}

	default boolean isGyldigFregKilde() {
		if (getMetadata() == null) {
			return false;
		}
		return getMetadata().isKildeFreg() && isNotExpired();
	}

	@Override
	default int compareTo(AdresseGyldigKilde o) {
		if (getGyldigFraOgMed() == null || o.getGyldigFraOgMed() == null) {
			return 0;
		}

		return getGyldigFraOgMedOrSisteEndring().compareTo(o.getGyldigFraOgMedOrSisteEndring());
	}

	default LocalDateTime getGyldigFraOgMedOrSisteEndring() {
		return nonNull(getGyldigFraOgMed()) ? getGyldigFraOgMed() : getMetadata().getDatoForSisteEndring();
	}

	default boolean isNotExpired() {
		return getGyldigTilOgMed() == null || LocalDateTime.now().isBefore(getGyldigTilOgMed());
	}

}
