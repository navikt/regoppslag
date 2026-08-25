package no.nav.regoppslag.pdl;

import no.nav.regoppslag.consumer.pdl.to.UtenlandskAdresse;
import org.junit.jupiter.api.Test;

import static java.lang.String.join;
import static no.nav.regoppslag.pdl.UtenlandskPoststedFormatter.formatPostkodeBystedOgOmraade;
import static no.nav.regoppslag.pdl.UtenlandskPoststedFormatter.formatUSAogKanadaPostkodeBystedOgOmraade;
import static no.nav.regoppslag.util.TestDataUtil.ADRESSE_NAVN_NUMMER;
import static no.nav.regoppslag.util.TestDataUtil.ARIZONA_STATE;
import static no.nav.regoppslag.util.TestDataUtil.BERLIN_REGION;
import static no.nav.regoppslag.util.TestDataUtil.BYSTED_BERLIN;
import static no.nav.regoppslag.util.TestDataUtil.POSTNUMMER;
import static no.nav.regoppslag.util.TestDataUtil.TUCSON_CITY;
import static no.nav.regoppslag.util.TestDataUtil.TYSKLAND_LANDKODE;
import static no.nav.regoppslag.util.TestDataUtil.USA_LANDKODE;
import static no.nav.regoppslag.util.TestDataUtil.USA_POSTKODE;
import static org.assertj.core.api.Assertions.assertThat;


class UtenlandskPoststedFormatterTest {

	@Test
	void shouldFormatereUSAOgKanadaMedPostkodeStedOgOmrade() {
		UtenlandskAdresse utenlandskAdresse = usaAdresse().bySted(TUCSON_CITY)
				.postkode(USA_POSTKODE)
				.regionDistriktOmraade(ARIZONA_STATE)
				.build();

		String adresselinje3 = formatUSAogKanadaPostkodeBystedOgOmraade(utenlandskAdresse);

		assertThat(adresselinje3).isEqualTo(join(" ", TUCSON_CITY, ARIZONA_STATE, USA_POSTKODE));
	}

	@Test
	void shouldFormatereUSAOgKanadaMedPostkodeOgOmrade() {
		UtenlandskAdresse utenlandskAdresse = usaAdresse()
				.postkode(USA_POSTKODE)
				.regionDistriktOmraade(ARIZONA_STATE)
				.build();

		String adresselinje3 = formatUSAogKanadaPostkodeBystedOgOmraade(utenlandskAdresse);

		assertThat(adresselinje3).isEqualTo(join(" ", ARIZONA_STATE, USA_POSTKODE));
	}

	@Test
	void shouldFormatereUSAOgKanadaMedStedOgOmrade() {
		UtenlandskAdresse utenlandskAdresse = usaAdresse().bySted(TUCSON_CITY)
				.regionDistriktOmraade(ARIZONA_STATE)
				.build();

		String adresselinje3 = formatUSAogKanadaPostkodeBystedOgOmraade(utenlandskAdresse);

		assertThat(adresselinje3).isEqualTo(join(" ", TUCSON_CITY, ARIZONA_STATE));
	}

	@Test
	void shouldFormatereMedPostkodeStedOgOmrade() {
		UtenlandskAdresse utenlandskAdresse = utenlandskAdresse()
				.postkode(POSTNUMMER)
				.bySted(BYSTED_BERLIN)
				.regionDistriktOmraade(BERLIN_REGION)
				.build();

		String adresselinje3 = formatPostkodeBystedOgOmraade(utenlandskAdresse);

		assertThat(adresselinje3).isEqualTo("%s %s, %s".formatted(POSTNUMMER, BYSTED_BERLIN, BERLIN_REGION));
	}

	@Test
	void shouldFormatereMedStedOgOmrade() {
		UtenlandskAdresse utenlandskAdresse = utenlandskAdresse()
				.bySted(BYSTED_BERLIN)
				.regionDistriktOmraade(BERLIN_REGION)
				.build();

		String adresselinje3 = formatPostkodeBystedOgOmraade(utenlandskAdresse);

		assertThat(adresselinje3).isEqualTo("%s, %s".formatted(BYSTED_BERLIN, BERLIN_REGION));
	}

	@Test
	void shouldFormatereMedPostkodeOgSted() {
		UtenlandskAdresse utenlandskAdresse = utenlandskAdresse()
				.postkode(POSTNUMMER)
				.bySted(BYSTED_BERLIN)
				.build();

		String adresselinje3 = formatPostkodeBystedOgOmraade(utenlandskAdresse);

		assertThat(adresselinje3).isEqualTo("%s %s".formatted(POSTNUMMER, BYSTED_BERLIN));
	}

	private static UtenlandskAdresse.UtenlandskAdresseBuilder usaAdresse() {
		return UtenlandskAdresse.builder()
				.adressenavnNummer(ADRESSE_NAVN_NUMMER)
				.landkode(USA_LANDKODE);
	}

	private static UtenlandskAdresse.UtenlandskAdresseBuilder utenlandskAdresse() {
		return UtenlandskAdresse.builder()
				.adressenavnNummer(ADRESSE_NAVN_NUMMER)
				.landkode(TYSKLAND_LANDKODE);
	}

}