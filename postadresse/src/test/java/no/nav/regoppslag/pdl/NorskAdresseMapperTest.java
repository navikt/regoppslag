package no.nav.regoppslag.pdl;

import no.nav.regoppslag.consumer.pdl.to.Kontaktadresse;
import no.nav.regoppslag.consumer.pdl.to.Kontaktadresse.PostadresseIFrittFormat;
import no.nav.regoppslag.consumer.pdl.to.Kontaktadresse.PostadresseIFrittFormat.PostadresseIFrittFormatBuilder;
import no.nav.regoppslag.consumer.pdl.to.Kontaktadresse.Postboksadresse.PostboksadresseBuilder;
import no.nav.regoppslag.consumer.pdl.to.Matrikkeladresse;
import no.nav.regoppslag.consumer.pdl.to.PostadresseTo;
import no.nav.regoppslag.consumer.pdl.to.Vegadresse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;
import java.util.stream.Stream;

import static no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode.BOSTEDSADRESSE;
import static no.nav.regoppslag.pdl.NorskAdresseMapper.mapMatrikkeladresse;
import static no.nav.regoppslag.pdl.NorskAdresseMapper.mapPostadresse;
import static no.nav.regoppslag.pdl.NorskAdresseMapper.mapVegadresse;
import static no.nav.regoppslag.util.TestDataUtil.ADRESSELINJE1;
import static no.nav.regoppslag.util.TestDataUtil.GATENAVN;
import static no.nav.regoppslag.util.TestDataUtil.HUSBOKSTAV;
import static no.nav.regoppslag.util.TestDataUtil.HUSNR;
import static no.nav.regoppslag.util.TestDataUtil.POSTNR;
import static no.nav.regoppslag.util.TestDataUtil.POSTSTED;
import static org.assertj.core.api.Assertions.assertThat;

class NorskAdresseMapperTest {

	private static final String ADRESSELINJE_1 = "Adresselinje 1";
	private static final String ADRESSELINJE_2 = "Adresselinje 2";
	private static final String ADRESSELINJE_3 = "Adresselinje 3";
	private static final String POSTBOKS_NUMMER = "123";
	private static final String POSTBOKS = "Postboks" + " " + POSTBOKS_NUMMER;
	private static final String ADRESSENAVN_MED_ALTERNATIV_CO_PREFIKS = "CO Adressenavn";
	private static final String POSTBOKSEIER = "Postbokseier";
	private static final String POSTBOKSEIER_MED_CO_PREFIKS = "C/O " + POSTBOKSEIER;

	private static final Vegadresse VEGADRESSE = Vegadresse.builder()
			.adressenavn(GATENAVN)
			.husnummer(String.valueOf(HUSNR))
			.husbokstav(HUSBOKSTAV)
			.postnummer(POSTNR)
			.build();

	@ParameterizedTest
	@MethodSource
	void shouldMappeVegadresse(String coAdressenavn,
	                         String forventetAdresselinje1,
	                         String forventetAdresselinje2) {

		PostadresseTo postadresse = mapVegadresse(VEGADRESSE, coAdressenavn, BOSTEDSADRESSE);

		assertThat(postadresse)
				.extracting(
						PostadresseTo::getAdresselinje1,
						PostadresseTo::getAdresselinje2,
						PostadresseTo::getPostnummer,
						PostadresseTo::getPoststed,
						PostadresseTo::getAdressekilde
				)
				.containsExactly(forventetAdresselinje1, forventetAdresselinje2, POSTNR, POSTSTED, BOSTEDSADRESSE);
	}

	private static Stream<Arguments> shouldMappeVegadresse() {
		return Stream.of(
				Arguments.of(null, ADRESSELINJE1, null), //Uten c/o: forventet vegadresse på linje 1.
				Arguments.of(ADRESSENAVN_MED_ALTERNATIV_CO_PREFIKS, ADRESSENAVN_MED_ALTERNATIV_CO_PREFIKS, ADRESSELINJE1) //Med c/o: forventet c/o på linje 1 og vegadresse på linje 2.
		);
	}

	@Test
	void shouldBeholdeAlleAdresselinjerForFrittFormatUtenCoAdressenavn() {
		Kontaktadresse kontaktadresse = Kontaktadresse.builder()
				.postadresseIFrittFormat(postadresseFrittFormat().build())
				.build();

		Optional<PostadresseTo> resultat = mapPostadresse(kontaktadresse);

		assertThat(resultat)
				.isPresent()
				.get()
				.extracting(
						PostadresseTo::getAdresselinje1,
						PostadresseTo::getAdresselinje2,
						PostadresseTo::getAdresselinje3,
						PostadresseTo::getPostnummer,
						PostadresseTo::getPoststed
				)
				.containsExactly(ADRESSELINJE_1, ADRESSELINJE_2, ADRESSELINJE_3, POSTNR, POSTSTED);
	}

	@Test
	void shouldSetteInnCoAdressenavnOgFjerneSisteAdresselinjeForFrittFormat() {
		Kontaktadresse kontaktadresse = Kontaktadresse.builder()
				.coAdressenavn(ADRESSENAVN_MED_ALTERNATIV_CO_PREFIKS)
				.postadresseIFrittFormat(postadresseFrittFormat().build())
				.build();

		Optional<PostadresseTo> resultat = mapPostadresse(kontaktadresse);

		assertThat(resultat)
				.isPresent()
				.get()
				.extracting(
						PostadresseTo::getAdresselinje1,
						PostadresseTo::getAdresselinje2,
						PostadresseTo::getAdresselinje3,
						PostadresseTo::getPostnummer,
						PostadresseTo::getPoststed
				)
				.containsExactly(ADRESSENAVN_MED_ALTERNATIV_CO_PREFIKS, ADRESSELINJE_1, ADRESSELINJE_2, POSTNR, POSTSTED);
	}

	@Test
	void shouldMappePostboksMedEier() {
		Kontaktadresse kontaktadresse = Kontaktadresse.builder()
				.postboksadresse(postboksadresse().build())
				.build();

		Optional<PostadresseTo> resultat = mapPostadresse(kontaktadresse);

		assertThat(resultat)
				.isPresent()
				.get()
				.extracting(
						PostadresseTo::getAdresselinje1,
						PostadresseTo::getAdresselinje2,
						PostadresseTo::getAdresselinje3,
						PostadresseTo::getPostnummer,
						PostadresseTo::getPoststed
				)
				.containsExactly(POSTBOKSEIER_MED_CO_PREFIKS, POSTBOKS, null, POSTNR, POSTSTED);
	}

	@Test
	void shouldIkkeDuplisereCoPrefiksForPostbokseier() {
		Kontaktadresse kontaktadresse = Kontaktadresse.builder()
				.postboksadresse(postboksadresse().postbokseier(POSTBOKSEIER_MED_CO_PREFIKS).build())
				.build();

		Optional<PostadresseTo> resultat = mapPostadresse(kontaktadresse);

		assertThat(resultat)
				.isPresent()
				.get()
				.extracting(PostadresseTo::getAdresselinje1)
				.isEqualTo(POSTBOKSEIER_MED_CO_PREFIKS);
	}

	@Test
	void shouldMappePostboksUtenEier() {
		Kontaktadresse kontaktadresse = Kontaktadresse.builder()
				.postboksadresse(postboksadresse().postbokseier(null).build())
				.build();

		Optional<PostadresseTo> resultat = mapPostadresse(kontaktadresse);

		assertThat(resultat)
				.isPresent()
				.get()
				.extracting(
						PostadresseTo::getAdresselinje1,
						PostadresseTo::getAdresselinje2,
						PostadresseTo::getAdresselinje3,
						PostadresseTo::getPostnummer,
						PostadresseTo::getPoststed
				)
				.containsExactly(POSTBOKS, null, null, POSTNR, POSTSTED);
	}

	@Test
	void shouldReturnereTomOptionalWhenPostboksMangler() {
		Kontaktadresse kontaktadresse = Kontaktadresse.builder()
				.postboksadresse(postboksadresse().postboks(null).build())
				.build();

		assertThat(mapPostadresse(kontaktadresse)).isEmpty();
	}

	@ParameterizedTest
	@ValueSource(strings = {"Tilleggsnavn"})
	@NullSource
	void shouldMappeMatrikkeladresse(String tilleggsnavn) {
		Matrikkeladresse matrikkeladresse = Matrikkeladresse.builder()
				.tilleggsnavn(tilleggsnavn)
				.postnummer(POSTNR)
				.build();

		PostadresseTo postadresse = mapMatrikkeladresse(matrikkeladresse, BOSTEDSADRESSE);

		assertThat(postadresse)
				.extracting(
						PostadresseTo::getAdresselinje1,
						PostadresseTo::getAdresselinje2,
						PostadresseTo::getAdresselinje3,
						PostadresseTo::getPostnummer,
						PostadresseTo::getPoststed
				)
				.containsExactly(tilleggsnavn, null, null, POSTNR, POSTSTED);
	}

	private static PostadresseIFrittFormatBuilder postadresseFrittFormat() {
		return PostadresseIFrittFormat.builder()
				.adresselinje1(ADRESSELINJE_1)
				.adresselinje2(ADRESSELINJE_2)
				.adresselinje3(ADRESSELINJE_3)
				.postnummer(POSTNR);
	}

	private static PostboksadresseBuilder postboksadresse() {
		return Kontaktadresse.Postboksadresse.builder()
				.postbokseier(POSTBOKSEIER)
				.postboks(POSTBOKS_NUMMER)
				.postnummer(POSTNR);
	}

}
