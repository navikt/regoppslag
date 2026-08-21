package no.nav.regoppslag.pdl;

import no.nav.regoppslag.consumer.pdl.to.Kontaktadresse;
import no.nav.regoppslag.consumer.pdl.to.PostadresseTo;
import no.nav.regoppslag.consumer.pdl.to.UtenlandskAdresse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

import static no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode.KONTAKTADRESSE;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_UTLAND;
import static no.nav.regoppslag.pdl.UtenlandskAdresseMapper.mapUtenlandskAdresse;
import static no.nav.regoppslag.pdl.UtenlandskAdresseMapper.mapUtenlandskPostadresse;
import static no.nav.regoppslag.util.TestDataUtil.ADRESSE_NAVN_NUMMER;
import static no.nav.regoppslag.util.TestDataUtil.BYGNING_ETASJE_LEILIGHET;
import static no.nav.regoppslag.util.TestDataUtil.BYSTED_BERLIN;
import static no.nav.regoppslag.util.TestDataUtil.CO_ADRESSENAVN;
import static no.nav.regoppslag.util.TestDataUtil.POSTNUMMER;
import static no.nav.regoppslag.util.TestDataUtil.POSTNUMMER_OG_BYSTED_BERLIN;
import static no.nav.regoppslag.util.TestDataUtil.TUCSON_CITY;
import static no.nav.regoppslag.util.TestDataUtil.TYSKLAND_LANDKODE;
import static no.nav.regoppslag.util.TestDataUtil.USA_LANDKODE;
import static no.nav.regoppslag.util.TestDataUtil.USA_POSTKODE;
import static no.nav.regoppslag.util.TestDataUtil.UTENLANDSK_POSTBOKS;
import static org.assertj.core.api.Assertions.assertThat;

class UtenlandskAdresseMapperTest {

	@Test
	void shouldMappeUtenlandskKontaktadresse() {
		UtenlandskAdresse adresse = UtenlandskAdresse.builder()
				.adressenavnNummer(null)
				.bygningEtasjeLeilighet(BYGNING_ETASJE_LEILIGHET)
				.postboksNummerNavn(null)
				.postkode(USA_POSTKODE)
				.bySted(TUCSON_CITY)
				.regionDistriktOmraade("")
				.landkode(USA_LANDKODE)
				.build();

		Kontaktadresse kontaktadresse = Kontaktadresse.builder()
				.utenlandskAdresse(adresse)
				.build();

		Optional<PostadresseTo> resultat = mapUtenlandskPostadresse(kontaktadresse);

		assertThat(resultat)
				.isPresent()
				.get()
				.extracting(
						PostadresseTo::getAdresselinje1,
						PostadresseTo::getAdresselinje2,
						PostadresseTo::getAdresselinje3,
						PostadresseTo::getPostnummer,
						PostadresseTo::getPoststed,
						PostadresseTo::getAdresseType,
						PostadresseTo::getAdressekilde
				)
				.containsExactly(
						BYGNING_ETASJE_LEILIGHET,
						TUCSON_CITY + " " + USA_POSTKODE,
						null,
						null,
						null,
						POSTADRESSE_UTLAND,
						KONTAKTADRESSE
				);
	}

	@ParameterizedTest
	@MethodSource("adresselinjer")
	void shouldMappePotensielleAdresselinjerIRekkefolge(
			String coAdressenavn,
			String adressenavnNummer,
			String bygningEtasjeLeilighet,
			String postkode,
			String bySted,
			String forventetAdresselinje1,
			String forventetAdresselinje2,
			String forventetAdresselinje3
	) {
		UtenlandskAdresse adresse = UtenlandskAdresse.builder()
				.adressenavnNummer(adressenavnNummer)
				.bygningEtasjeLeilighet(bygningEtasjeLeilighet)
				.postboksNummerNavn(null)
				.postkode(postkode)
				.bySted(bySted)
				.regionDistriktOmraade(null)
				.landkode(TYSKLAND_LANDKODE)
				.build();

		PostadresseTo postadresse = mapUtenlandskAdresse(adresse, coAdressenavn, KONTAKTADRESSE);

		assertThat(postadresse)
				.extracting(
						PostadresseTo::getAdresselinje1,
						PostadresseTo::getAdresselinje2,
						PostadresseTo::getAdresselinje3
				)
				.containsExactly(
						forventetAdresselinje1,
						forventetAdresselinje2,
						forventetAdresselinje3
				);
	}

	@Test
	void shouldPrioriterePostboksOverAdressenavnOgNummer() {
		UtenlandskAdresse adresse = UtenlandskAdresse.builder()
				.adressenavnNummer(ADRESSE_NAVN_NUMMER)
				.postboksNummerNavn(UTENLANDSK_POSTBOKS)
				.postkode(POSTNUMMER)
				.bySted(BYSTED_BERLIN)
				.landkode(TYSKLAND_LANDKODE)
				.build();

		PostadresseTo postadresse = mapUtenlandskAdresse(adresse, null, KONTAKTADRESSE);

		assertThat(postadresse)
				.extracting(
						PostadresseTo::getAdresselinje1,
						PostadresseTo::getAdresselinje2,
						PostadresseTo::getAdresselinje3
				)
				.containsExactly(UTENLANDSK_POSTBOKS, POSTNUMMER_OG_BYSTED_BERLIN, null);
	}

	@Test
	void shouldKombinereCoAdressenavnOgHovedadresseWhenAlleFirePotensielleLinjerFinnes() {
		UtenlandskAdresse adresse = UtenlandskAdresse.builder()
				.adressenavnNummer(ADRESSE_NAVN_NUMMER)
				.bygningEtasjeLeilighet(BYGNING_ETASJE_LEILIGHET)
				.postkode(POSTNUMMER)
				.bySted(BYSTED_BERLIN)
				.landkode(TYSKLAND_LANDKODE)
				.build();

		PostadresseTo postadresse = mapUtenlandskAdresse(adresse, CO_ADRESSENAVN, KONTAKTADRESSE);

		assertThat(postadresse)
				.extracting(
						PostadresseTo::getAdresselinje1,
						PostadresseTo::getAdresselinje2,
						PostadresseTo::getAdresselinje3
				)
				.containsExactly(
						"C/O " + CO_ADRESSENAVN + ", " + ADRESSE_NAVN_NUMMER,
						BYGNING_ETASJE_LEILIGHET,
						POSTNUMMER_OG_BYSTED_BERLIN
				);
	}

	private static Stream<Arguments> adresselinjer() {
		return Stream.of(
				Arguments.of(
						null,
						ADRESSE_NAVN_NUMMER,
						BYGNING_ETASJE_LEILIGHET,
						POSTNUMMER,
						BYSTED_BERLIN,
						ADRESSE_NAVN_NUMMER,
						BYGNING_ETASJE_LEILIGHET,
						POSTNUMMER_OG_BYSTED_BERLIN
				),
				Arguments.of(
						null,
						ADRESSE_NAVN_NUMMER,
						null,
						POSTNUMMER,
						BYSTED_BERLIN,
						ADRESSE_NAVN_NUMMER,
						POSTNUMMER_OG_BYSTED_BERLIN,
						null
				),
				Arguments.of(
						null,
						null,
						BYGNING_ETASJE_LEILIGHET,
						POSTNUMMER,
						BYSTED_BERLIN,
						BYGNING_ETASJE_LEILIGHET,
						POSTNUMMER_OG_BYSTED_BERLIN,
						null
				),
				Arguments.of(
						null,
						null,
						null,
						POSTNUMMER,
						BYSTED_BERLIN,
						POSTNUMMER_OG_BYSTED_BERLIN,
						null,
						null
				),
				Arguments.of(
						CO_ADRESSENAVN,
						ADRESSE_NAVN_NUMMER,
						null,
						POSTNUMMER,
						BYSTED_BERLIN,
						"C/O " + CO_ADRESSENAVN,
						ADRESSE_NAVN_NUMMER,
						POSTNUMMER_OG_BYSTED_BERLIN
				),
				Arguments.of(
						CO_ADRESSENAVN,
						null,
						null,
						POSTNUMMER,
						BYSTED_BERLIN,
						"C/O " + CO_ADRESSENAVN,
						POSTNUMMER_OG_BYSTED_BERLIN,
						null
				),
				Arguments.of(
						CO_ADRESSENAVN,
						ADRESSE_NAVN_NUMMER,
						null,
						null,
						null,
						"C/O " + CO_ADRESSENAVN,
						ADRESSE_NAVN_NUMMER,
						null
				),
				Arguments.of(
						CO_ADRESSENAVN,
						null,
						BYGNING_ETASJE_LEILIGHET,
						null,
						null,
						"C/O " + CO_ADRESSENAVN,
						BYGNING_ETASJE_LEILIGHET,
						null
				),
				Arguments.of(
						CO_ADRESSENAVN,
						ADRESSE_NAVN_NUMMER,
						BYGNING_ETASJE_LEILIGHET,
						null,
						null,
						"C/O " + CO_ADRESSENAVN,
						ADRESSE_NAVN_NUMMER,
						BYGNING_ETASJE_LEILIGHET
				),
				Arguments.of(
						CO_ADRESSENAVN,
						null,
						BYGNING_ETASJE_LEILIGHET,
						POSTNUMMER,
						BYSTED_BERLIN,
						"C/O " + CO_ADRESSENAVN,
						BYGNING_ETASJE_LEILIGHET,
						POSTNUMMER_OG_BYSTED_BERLIN
				)
		);
	}

}
