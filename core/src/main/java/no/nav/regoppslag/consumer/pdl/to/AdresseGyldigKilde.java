package no.nav.regoppslag.consumer.pdl.to;

import java.time.LocalDateTime;

import static java.util.Objects.nonNull;

public interface AdresseGyldigKilde extends Comparable<AdresseGyldigKilde> {
	LocalDateTime getGyldigFraOgMed();
	LocalDateTime getGyldigTilOgMed();

	Metadata getMetadata();

	default boolean isGyldigPdlKilde(LocalDateTime atTime) {
		if (getMetadata() == null) {
			return false;
		}
		return getMetadata().isKildePdl() && isNotExpired(atTime);
	}

	default boolean isGyldigFregKilde(LocalDateTime atTime) {
		if (getMetadata() == null) {
			return false;
		}
		return getMetadata().isKildeFreg() && isNotExpired(atTime);
	}

	@Override
	default int compareTo(AdresseGyldigKilde o) {
		if (getGyldigFraOgMedOrSisteEndring() == null && o.getGyldigFraOgMedOrSisteEndring() == null) {
			return 0;
		} else if (getGyldigFraOgMedOrSisteEndring() == null) { // gyldigFraOgMed == null er alltid før alle andre tidspunkter
			return -1;
		} else if (o.getGyldigFraOgMedOrSisteEndring() == null) {
			return 1;
		}

		return getGyldigFraOgMedOrSisteEndring().compareTo(o.getGyldigFraOgMedOrSisteEndring());
	}

	default LocalDateTime getGyldigFraOgMedOrSisteEndring() {
		return nonNull(getGyldigFraOgMed()) ? getGyldigFraOgMed() : getMetadata().getDatoForSisteEndring();
	}

	default boolean isNotExpired(LocalDateTime now) {
		return getGyldigTilOgMed() == null || now.isBefore(getGyldigTilOgMed());
	}

}
