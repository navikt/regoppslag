package no.nav.regoppslag.consumer.pdl.to;

import java.time.LocalDateTime;
import java.util.Comparator;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static java.util.Objects.nonNull;

public interface GyldigKilde {

	/**
	 * Sammenligner først på gyldigFraOgMed, eller dato for siste endring hvis gyldigFraOgMed mangler.
	 * Objekter uten begge datoene sorteres før objekter som har en dato.
	 */
	Comparator<GyldigKilde> ETTER_GYLDIG_FRA_ELLER_SISTE_ENDRING = Comparator.comparing(
			GyldigKilde::getGyldigFraOgMedOrSisteEndring,
			nullsFirst(naturalOrder())
	);

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

	default LocalDateTime getGyldigFraOgMedOrSisteEndring() {
		return nonNull(getGyldigFraOgMed()) ? getGyldigFraOgMed() : getMetadata().getDatoForSisteEndring();
	}

	default boolean isNotExpired(LocalDateTime now) {
		return getGyldigTilOgMed() == null || now.isBefore(getGyldigTilOgMed());
	}

}
