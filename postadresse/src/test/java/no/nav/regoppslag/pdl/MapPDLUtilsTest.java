package no.nav.regoppslag.pdl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static java.lang.String.format;
import static no.nav.regoppslag.pdl.MapPDLUtils.prependCoAdressenavnWithCareOfIfMissing;
import static org.assertj.core.api.Assertions.assertThat;

class MapPDLUtilsTest {

	@Test
	void shouldAddCareOfPrefixIfMissingFromCoAdressenavn() {
		String coAdressenavn = "Max Mekker";
		var formattedCoAdressenavn = prependCoAdressenavnWithCareOfIfMissing(coAdressenavn);

		assertThat(formattedCoAdressenavn).isEqualTo(format("C/O %s", coAdressenavn));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"C/O Max Mekker",
			"c/o Max Mekker",
			"v/ Max Mekker",
			"℅ Max Mekker"
	})
	void shouldNotAddCareOfPrefixIfAlreadyIncluded(String inputCoAdressenavn) {
		var formattedCoAdressenavn = prependCoAdressenavnWithCareOfIfMissing(inputCoAdressenavn);

		assertThat(formattedCoAdressenavn).isEqualTo(inputCoAdressenavn);
	}

	@ParameterizedTest
	@ValueSource(strings = {" "})
	@NullSource
	void shouldNotAddCareOfPrefixWhenCoAdressenavnIsBlankOrNull(String inputCoAdressenavn) {
		var formattedCoAdressenavn = prependCoAdressenavnWithCareOfIfMissing(inputCoAdressenavn);

		assertThat(formattedCoAdressenavn).isNull();
	}
}