package no.nav.regoppslag.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static no.nav.regoppslag.service.LandkodeService.finnLandkode;
import static no.nav.regoppslag.service.LandkodeService.finnLandkodeAlpha2FraAlpha3;
import static no.nav.regoppslag.service.LandkodeService.finnLandnavn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;

public class LandkodeServiceTest {

	@ParameterizedTest
	@CsvSource({
			"' Norge ', NO",
			"Norway, NO",
			"norway, NO",
			"' Türkiye ', TR",
			"'Kosovo, Republic of', XK"
	})
	void skalFinneAlpha2LandkodeFraLandnavn(String landnavn, String forventetLandkode) {
		assertThat(finnLandkode(landnavn), is(forventetLandkode));
	}

	@ParameterizedTest
	@CsvSource({
			"NO, Norge",
			"NOR, Norge",
			"TR, Türkiye",
			"TUR, Türkiye",
			"SE, Sweden",
			"' se ', Sweden"
	})
	void skalFinneLandnavnFraAlpha2EllerAlpha3(String landkode, String forventetLandnavn) {
		assertThat(finnLandnavn(landkode), is(forventetLandnavn));
	}

	@ParameterizedTest
	@ValueSource(strings = {"FINNES IKKE", "", " "})
	@NullSource
	void skalReturnereLandnavnNullForUgyldigLandkode(String landkode) {
		assertNull(finnLandnavn(landkode));
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"", "FINNES IKKE"})
	void skalReturnereLandkodeNullForUgyldigLandnavn(String landnavn) {
		assertNull(finnLandkode(landnavn));
	}

	@ParameterizedTest
	@ValueSource(strings = {"XK", "XKX", "XXK", "xkx"})
	void skalFinneLandnavnForKosovo(String landkode) {
		assertThat(finnLandnavn(landkode), is("Kosovo, Republic of"));
	}

	@ParameterizedTest
	@CsvSource({
			"NOR, NO",
			"TUR, TR",
			"XKX, XK",
			"XXK, XK",
			"xkx, XK"
	})
	void skalFinneAlpha2FraAlpha3(String alpha3, String forventetAlpha2) {
		assertThat(finnLandkodeAlpha2FraAlpha3(alpha3), is(forventetAlpha2));
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"", "FINNES IKKE", "XK"})
	void skalReturnereNullForUgyldigAlpha3(String alpha3) {
		assertNull(finnLandkodeAlpha2FraAlpha3(alpha3));
	}
}
