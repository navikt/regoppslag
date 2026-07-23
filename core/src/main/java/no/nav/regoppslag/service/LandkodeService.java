package no.nav.regoppslag.service;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toUnmodifiableMap;
import static no.nav.regoppslag.util.SafeLoggingUtil.removeUnsafeChars;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
public final class LandkodeService {

	private static final String KOSOVO = "Kosovo, Republic of";
	private static final String KOSOVO_ALPHA2 = "XK";
	private static final String KOSOVO_ALPHA3 = "XKX";
	private static final String KOSOVO_LEGACY_ALPHA3 = "XXK";
	private static final String NORGE = "Norge";
	private static final String NORWAY = "Norway";

	private static final Map<String, Land> LAND_PER_ALPHA2;
	private static final Map<String, Land> LAND_PER_ALPHA3;
	private static final Map<String, Land> LAND_PER_NAVN;

	static {
		LAND_PER_ALPHA2 = opprettLandPerAlpha2();
		LAND_PER_ALPHA3 = LAND_PER_ALPHA2.values().stream()
				.collect(toUnmodifiableMap(Land::alpha3, Function.identity()));
		LAND_PER_NAVN = LAND_PER_ALPHA2.values().stream()
				.collect(toUnmodifiableMap(land -> normalisertNavn(land.navn()), Function.identity()));
	}

	private LandkodeService() {
	}

	public static String finnLandnavn(String landkode) {
		if (isBlank(landkode)) {
			return null;
		}

		String normalisertKode = normalisertKode(landkode);

		if (erKosovoKode(normalisertKode)) {
			return KOSOVO;
		}

		Land land = normalisertKode.length() == 2
				? LAND_PER_ALPHA2.get(normalisertKode)
				: LAND_PER_ALPHA3.get(normalisertKode);

		if (land == null) {
			log.warn("Finner ikke land for landkode={}", removeUnsafeChars(landkode));
			return null;
		}

		return NORWAY.equals(land.navn()) ? NORGE : land.navn();
	}

	public static String finnLandkode(String landnavn) {
		if (isBlank(landnavn)) {
			return null;
		}

		String normalisertNavn = normalisertNavn(landnavn);

		if (normalisertNavn(KOSOVO).equals(normalisertNavn)) {
			return KOSOVO_ALPHA2;
		}

		if (normalisertNavn(NORGE).equals(normalisertNavn)) {
			normalisertNavn = normalisertNavn(NORWAY);
		}

		Land land = LAND_PER_NAVN.get(normalisertNavn);
		return land == null ? null : land.alpha2();
	}

	public static String finnLandkodeAlpha2FraAlpha3(String landkodeAlpha3) {
		if (isBlank(landkodeAlpha3)) {
			return null;
		}

		String normalisertKode = normalisertKode(landkodeAlpha3);

		if (KOSOVO_ALPHA3.equals(normalisertKode) || KOSOVO_LEGACY_ALPHA3.equals(normalisertKode)) {
			return KOSOVO_ALPHA2;
		}

		Land land = LAND_PER_ALPHA3.get(normalisertKode);
		return land == null ? null : land.alpha2();
	}

	private static Map<String, Land> opprettLandPerAlpha2() {
		return Arrays.stream(Locale.getISOCountries())
				.map(LandkodeService::opprettLand)
				.collect(toUnmodifiableMap(Land::alpha2, Function.identity()));
	}

	private static Land opprettLand(String alpha2) {
		Locale locale = Locale.of("", alpha2);
		return new Land(alpha2, locale.getISO3Country(), locale.getDisplayCountry(Locale.ENGLISH));
	}

	private static boolean erKosovoKode(String kode) {
		return KOSOVO_ALPHA2.equals(kode)
			   || KOSOVO_ALPHA3.equals(kode)
			   || KOSOVO_LEGACY_ALPHA3.equals(kode);
	}

	private static String normalisertKode(String kode) {
		return kode.trim().toUpperCase(Locale.ROOT);
	}

	private static String normalisertNavn(String navn) {
		return navn.trim().toLowerCase(Locale.ROOT);
	}

	private record Land(String alpha2, String alpha3, String navn) {
	}
}
