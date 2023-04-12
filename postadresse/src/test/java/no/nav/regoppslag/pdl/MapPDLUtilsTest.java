package no.nav.regoppslag.pdl;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static java.lang.String.format;
import static no.nav.regoppslag.pdl.MapPDLUtils.prependWithCareOfIfMissing;
import static org.assertj.core.api.Assertions.assertThat;

class MapPDLUtilsTest {

	@ParameterizedTest
	@ValueSource(strings = {
			"Max Mekker",
			// Pass på navn som starter med med co/ved etc.
			"Conrad",
			"conrad",
			"vedum, trygve"
	})
	void shouldAddCareOfPrefixIfMissing(String coAdressenavn) {
		var formattedCoAdressenavn = prependWithCareOfIfMissing(coAdressenavn);

		assertThat(formattedCoAdressenavn).isEqualTo(format("C/O %s", coAdressenavn));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"C/O Max Mekker",
			"c/O Max Mekker",
			"C/o Max Mekker",
			"c/o Max Mekker",
			"co Max Mekker",
			"CO Max Mekker",
			"v/ Max Mekker",
			"ved Max Mekker",
			"℅ Max Mekker"
	})
	void shouldNotAddCareOfPrefixIfAlreadyIncluded(String inputCoAdressenavn) {
		var formattedCoAdressenavn = prependWithCareOfIfMissing(inputCoAdressenavn);

		assertThat(formattedCoAdressenavn).isEqualTo(inputCoAdressenavn);
	}

	@ParameterizedTest
	@ValueSource(strings = {" "})
	@NullSource
	void shouldNotAddCareOfPrefixWhenBlankOrNull(String inputCoAdressenavn) {
		var formattedCoAdressenavn = prependWithCareOfIfMissing(inputCoAdressenavn);

		assertThat(formattedCoAdressenavn).isNull();
	}
}