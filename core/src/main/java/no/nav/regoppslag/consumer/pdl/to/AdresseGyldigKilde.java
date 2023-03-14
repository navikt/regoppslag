package no.nav.regoppslag.consumer.pdl.to;

import java.time.LocalDateTime;

public interface AdresseGyldigKilde extends Comparable<AdresseGyldigKilde> {

	default boolean isGyldigPdlKilde() {
		if (getMetadata() == null) {
			return false;
		}
		return getMetadata().isKildePdl();
	}

	default boolean isGyldigFregKilde() {
		if (getMetadata() == null) {
			return false;
		}
		return getMetadata().isKildeFreg();
	}

	@Override
	default int compareTo(AdresseGyldigKilde o) {
		if (getGyldigFraOgMed() == null || o.getGyldigFraOgMed() == null) {
			return 0;
		}

		return getGyldigFraOgMed().compareTo(o.getGyldigFraOgMed());
	}

	Metadata getMetadata();

	LocalDateTime getGyldigFraOgMed();
}
