package no.nav.regoppslag.consumer.pdl.to;

public interface AdresseGyldigKilde {

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

	Metadata getMetadata();
}
