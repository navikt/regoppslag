package no.nav.regoppslag.pdl;

import no.nav.regoppslag.consumer.pdl.to.Bostedsadresse;
import no.nav.regoppslag.consumer.pdl.to.GyldigKilde;
import no.nav.regoppslag.consumer.pdl.to.PostadresseTo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static no.nav.regoppslag.consumer.pdl.to.GyldigKilde.ETTER_GYLDIG_FRA_ELLER_SISTE_ENDRING;

final class Adressevelger {

	private Adressevelger() {
	}

	/**
	 * Velger første gyldige PDL-adresse. Hvis ingen finnes, velges gyldig FREG-adresse med nyeste
	 * gyldigFraOgMed, eller dato for siste endring dersom gyldigFraOgMed mangler.
	 */
	static <T extends GyldigKilde> Optional<T> velgAdresseEtterKildeOgGyldighetsperiode(List<T> adresser, LocalDateTime tidspunkt) {
		if (adresser == null) {
			return Optional.empty();
		}

		return adresser.stream()
				.filter(adresse -> adresse.isGyldigPdlKilde(tidspunkt))
				.findFirst()
				.or(() -> adresser.stream()
						.filter(adresse -> adresse.isGyldigFregKilde(tidspunkt))
						.max(ETTER_GYLDIG_FRA_ELLER_SISTE_ENDRING));
	}

	/**
	 * Velger nyeste bostedsadresse som ikke er utløpt, basert på gyldigFraOgMed eller dato for siste endring.
	 */
	static Optional<Bostedsadresse> velgBostedsadresse(List<Bostedsadresse> adresser, LocalDateTime tidspunkt) {
		if (adresser == null) {
			return Optional.empty();
		}

		return adresser.stream()
				.filter(Objects::nonNull)
				.filter(adresse -> adresse.isNotExpired(tidspunkt))
				.max(ETTER_GYLDIG_FRA_ELLER_SISTE_ENDRING);
	}

	static boolean skalPrioritereUtenlandskBostedsadresse(Bostedsadresse bostedsadresse,
														  PostadresseTo bostedsPostadresse,
														  GyldigKilde valgtAdresse) {
		if (bostedsadresse == null || bostedsPostadresse == null || valgtAdresse == null || !bostedsPostadresse.erUtland()) {
			return false;
		}

		LocalDateTime bostedsadresseDato = bostedsadresse.getGyldigFraOgMedOrSisteEndring();
		LocalDateTime valgtAdresseDato = valgtAdresse.getGyldigFraOgMedOrSisteEndring();

		return bostedsadresseDato != null && valgtAdresseDato != null && bostedsadresseDato.isAfter(valgtAdresseDato);
	}
}
