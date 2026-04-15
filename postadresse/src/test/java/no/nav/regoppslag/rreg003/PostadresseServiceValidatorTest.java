package no.nav.regoppslag.rreg003;

import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static no.nav.regoppslag.rreg003.PostadresseServiceValidator.validateBehandlingsnummer;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostadresseServiceValidatorTest {

	@ParameterizedTest
	@NullAndEmptySource
	void skalGodtaNullOgTomtBehandlingsnummer(String behandlingsnummer) {
		assertThatNoException().isThrownBy(() -> validateBehandlingsnummer(behandlingsnummer));
	}

	@ParameterizedTest
	@ValueSource(strings = {"B123", "Z001"})
	void skalGodtaGyldigEnkeltBehandlingsnummer(String behandlingsnummer) {
		assertThatNoException().isThrownBy(() -> validateBehandlingsnummer(behandlingsnummer));
	}

	@ParameterizedTest
	@ValueSource(strings = {"B123,B456", "A999,Z001", "B123,B456,C789"})
	void skalGodtaGyldigKommaseparertBehandlingsnummer(String behandlingsnummer) {
		assertThatNoException().isThrownBy(() -> validateBehandlingsnummer(behandlingsnummer));
	}

	@Test
	void skalGodtaKommaseparertBehandlingsnummerMedMellomrom() {
		assertThatNoException().isThrownBy(() -> validateBehandlingsnummer("B123, B456"));
	}

	@ParameterizedTest
	@ValueSource(strings = {"B1234", "B12", "b123", "BB13", "BB123", "123B"})
	void skalAvviseUgyldigEnkeltBehandlingsnummer(String behandlingsnummer) {
		assertThatThrownBy(() -> validateBehandlingsnummer(behandlingsnummer))
				.isInstanceOf(RegoppslagIllegalArgumentException.class)
				.hasMessage("Ugyldig input Hvert behandlingsnummer må bestå av én stor bokstav med tre etterfølgende siffer. F.eks. B123.");
	}

	@ParameterizedTest
	@ValueSource(strings = {"B123,invalid", "invalid,B123", "B123,,B456", ",B123", "B123,"})
	void skalAvviseKommaSeparertListeMedUgyldigBehandlingsnummer(String behandlingsnummer) {
		assertThatThrownBy(() -> validateBehandlingsnummer(behandlingsnummer))
				.isInstanceOf(RegoppslagIllegalArgumentException.class)
				.hasMessage("Ugyldig input Hvert behandlingsnummer må bestå av én stor bokstav med tre etterfølgende siffer. F.eks. B123.");
	}
}
