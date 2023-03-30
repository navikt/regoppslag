package no.nav.regoppslag.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static no.nav.regoppslag.service.LandkodeService.finnLandkode;
import static no.nav.regoppslag.service.LandkodeService.finnLandnavn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
public class LandkodeServiceTest {

	private static final String NORGE = "Norge";
	private static final String NO = "NO";
	private static final String NOR = "NOR";
	private static final String FINNES_IKKE = "FINNES IKKE";
	private static final String KOSOVO = "Kosovo, Republic of";
	private static final String KOSOVO_LANDKODE_FEIL = "XXK";
	private static final String KOSOVO_LANDKODE_RIKTIG = "XKX";

	@Test
	public void skalFinneLandkode() {
		String landKode = finnLandkode(NORGE);
		assertThat(landKode, is(NO));
	}

	@Test
	public void skalReturnereLandkodeNullForLandnavnNull() {
		String landkode = finnLandkode(null);
		assertNull(landkode);
	}

	@ParameterizedTest
	@CsvSource(value = {
			NO,
			NOR,
	})
	public void skalFinneLandnavnFraAlpha2EllerAlpha3(String landkode) {
		String landnavn = finnLandnavn(landkode);
		assertThat(landnavn, is(NORGE));
	}

	@ParameterizedTest
	@ValueSource(strings = FINNES_IKKE)
	@NullSource
	public void skalReturnereLandnavnNullForUgyldigLandkode(String landkode) {
		String landnavn = finnLandnavn(landkode);
		assertNull(landnavn);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			KOSOVO_LANDKODE_FEIL,
			KOSOVO_LANDKODE_RIKTIG
	})
	public void skalFinneLandnavnForKosovoFraNyOgGammelAlpha3Kode(String landkode) {
		String landnavn = finnLandnavn(landkode);
		assertThat(landnavn, is(KOSOVO));
	}
}

