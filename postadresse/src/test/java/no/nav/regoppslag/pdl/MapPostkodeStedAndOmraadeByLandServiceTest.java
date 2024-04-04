package no.nav.regoppslag.pdl;

import no.nav.regoppslag.consumer.pdl.to.UtenlandskAdresse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static java.lang.String.format;
import static java.lang.String.join;
import static no.nav.regoppslag.util.TestDataUtil.ADRESSE_NAVN_NUMMER;
import static no.nav.regoppslag.util.TestDataUtil.ARIZONA_STATE;
import static no.nav.regoppslag.util.TestDataUtil.BERLIN_REGION;
import static no.nav.regoppslag.util.TestDataUtil.BYSTED_BERLIN;
import static no.nav.regoppslag.util.TestDataUtil.POSTNUMMER;
import static no.nav.regoppslag.util.TestDataUtil.TUCSON_CITY;
import static no.nav.regoppslag.util.TestDataUtil.TYSKLAND_LANDKODE;
import static no.nav.regoppslag.util.TestDataUtil.USA_LANDKODE;
import static no.nav.regoppslag.util.TestDataUtil.USA_POSTKODE;


class MapPostkodeStedAndOmraadeByLandServiceTest {

	private MapPostkodeBystedAndOmraadeByLand mapPostkodeStedAndOmraadeByLand = new MapPostkodeStedAndOmraadeByLandService();

	@Test
	void shouldGetUsaAdresselinje3FromPostkodeStedAndOmraad() {
		UtenlandskAdresse utenlandskAdresse = createUsaAdresse().bySted(TUCSON_CITY)
				.postkode(USA_POSTKODE)
				.regionDistriktOmraade(ARIZONA_STATE)
				.build();

		String adresselinje3 = mapPostkodeStedAndOmraadeByLand.mapUSAandKanadaPostkodeBystedAndOmraade(utenlandskAdresse);

		Assertions.assertThat(adresselinje3).isEqualTo(join(" ", TUCSON_CITY, ARIZONA_STATE, USA_POSTKODE));
	}

	@Test
	void shouldGetUsaAdresselinje3FromPostkodeAndOmraad() {
		UtenlandskAdresse utenlandskAdresse = createUsaAdresse()
				.postkode(USA_POSTKODE)
				.regionDistriktOmraade(ARIZONA_STATE)
				.build();

		String adresselinje3 = mapPostkodeStedAndOmraadeByLand.mapUSAandKanadaPostkodeBystedAndOmraade(utenlandskAdresse);

		Assertions.assertThat(adresselinje3).isEqualTo(join(" ", ARIZONA_STATE, USA_POSTKODE));
	}

	@Test
	void shouldGetUsaAdresselinje3FromBystedAndOmraad() {
		UtenlandskAdresse utenlandskAdresse = createUsaAdresse().bySted(TUCSON_CITY)
				.regionDistriktOmraade(ARIZONA_STATE)
				.build();

		String adresselinje3 = mapPostkodeStedAndOmraadeByLand.mapUSAandKanadaPostkodeBystedAndOmraade(utenlandskAdresse);

		Assertions.assertThat(adresselinje3).isEqualTo(join(" ", TUCSON_CITY, ARIZONA_STATE));
	}

	@Test
	void shouldGetDefaultUtenlandskAdresselinje3FromPostkodeStedAndOmraad() {
		UtenlandskAdresse utenlandskAdresse = createDefaultUtenlandskadresse()
				.postkode(POSTNUMMER)
				.bySted(BYSTED_BERLIN)
				.regionDistriktOmraade(BERLIN_REGION)
				.build();

		String adresselinje3 = mapPostkodeStedAndOmraadeByLand.mapDefaultPostkodeBystedAndOmraade(utenlandskAdresse);

		Assertions.assertThat(adresselinje3).isEqualTo(format("%s %s, %s", POSTNUMMER, BYSTED_BERLIN, BERLIN_REGION));
	}

	@Test
	void shouldGetDefaultUtenlandskAdresselinje3FromBystedAndOmraad() {
		UtenlandskAdresse utenlandskAdresse = createDefaultUtenlandskadresse()
				.bySted(BYSTED_BERLIN)
				.regionDistriktOmraade(BERLIN_REGION)
				.build();

		String adresselinje3 = mapPostkodeStedAndOmraadeByLand.mapDefaultPostkodeBystedAndOmraade(utenlandskAdresse);

		Assertions.assertThat(adresselinje3).isEqualTo(format("%s, %s", BYSTED_BERLIN, BERLIN_REGION));
	}

	@Test
	void shouldGetDefaultUtenlandskAdresselinje3FromPostkodeAndBysted() {
		UtenlandskAdresse utenlandskAdresse = createDefaultUtenlandskadresse()
				.postkode(POSTNUMMER)
				.bySted(BYSTED_BERLIN)
				.build();

		String adresselinje3 = mapPostkodeStedAndOmraadeByLand.mapDefaultPostkodeBystedAndOmraade(utenlandskAdresse);

		Assertions.assertThat(adresselinje3).isEqualTo(format("%s %s", POSTNUMMER, BYSTED_BERLIN));
	}

	@Test
	void shouldGetDefaultUtenlandskAdresselinje3FraPostkodeAndOmraadOgFjerneEkstraMellomromForanKomma() {
		UtenlandskAdresse utenlandskAdresse = createDefaultUtenlandskadresse()
				.postkode(POSTNUMMER)
				.regionDistriktOmraade(BERLIN_REGION)
				.build();

		String adresselinje3 = mapPostkodeStedAndOmraadeByLand.mapDefaultPostkodeBystedAndOmraade(utenlandskAdresse);

		Assertions.assertThat(adresselinje3).isEqualTo(format("%s, %s", POSTNUMMER, BERLIN_REGION));
	}

	private static UtenlandskAdresse.UtenlandskAdresseBuilder createUsaAdresse() {
		return UtenlandskAdresse.builder()
				.adressenavnNummer(ADRESSE_NAVN_NUMMER)
				.landkode(USA_LANDKODE);
	}

	private static UtenlandskAdresse.UtenlandskAdresseBuilder createDefaultUtenlandskadresse() {
		return UtenlandskAdresse.builder()
				.adressenavnNummer(ADRESSE_NAVN_NUMMER)
				.landkode(TYSKLAND_LANDKODE);
	}

}