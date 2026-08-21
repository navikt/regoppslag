package no.nav.regoppslag.consumer.pdl.to;

import org.junit.jupiter.api.Test;

import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_INNLAND;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_UTLAND;
import static org.assertj.core.api.Assertions.assertThat;

class PostadresseToTest {

	@Test
	void shouldVaereInnlandWhenAdressetypeErInnlandUavhengigAvStoreOgSmaBokstaver() {
		PostadresseTo adresse = PostadresseTo.builder()
				.adresseType(POSTADRESSE_INNLAND.toLowerCase())
				.build();

		assertThat(adresse.erInnland()).isTrue();
		assertThat(adresse.erUtland()).isFalse();
	}

	@Test
	void shouldVaereUtlandWhenAdressetypeErUtlandUavhengigAvStoreOgSmaBokstaver() {
		PostadresseTo adresse = PostadresseTo.builder()
				.adresseType(POSTADRESSE_UTLAND.toLowerCase())
				.build();

		assertThat(adresse.erUtland()).isTrue();
		assertThat(adresse.erInnland()).isFalse();
	}

	@Test
	void shouldIkkeVaereInnlandEllerUtlandWhenAdressetypeMangler() {
		PostadresseTo adresse = PostadresseTo.builder().build();

		assertThat(adresse.erInnland()).isFalse();
		assertThat(adresse.erUtland()).isFalse();
	}

	@Test
	void shouldIkkeVaereInnlandEllerUtlandWhenAdressetypeErUkjent() {
		PostadresseTo adresse = PostadresseTo.builder()
				.adresseType("ukjent")
				.build();

		assertThat(adresse.erInnland()).isFalse();
		assertThat(adresse.erUtland()).isFalse();
	}

	@Test
	void shouldVaereKomplettForDistribusjonWhenInnlandsadresseHarPostnummer() {
		PostadresseTo adresse = PostadresseTo.builder()
				.adresseType(POSTADRESSE_INNLAND)
				.postnummer("0123")
				.build();

		assertThat(adresse.erKomplettForDistribusjon()).isTrue();
	}

	@Test
	void shouldIkkeVaereKomplettForDistribusjonWhenInnlandsadresseManglerPostnummer() {
		PostadresseTo adresse = PostadresseTo.builder()
				.adresseType(POSTADRESSE_INNLAND)
				.postnummer(" ")
				.build();

		assertThat(adresse.erKomplettForDistribusjon()).isFalse();
	}

	@Test
	void shouldVaereKomplettForDistribusjonWhenUtenlandsadresseHarAdresselinjeOgLandkode() {
		PostadresseTo adresse = PostadresseTo.builder()
				.adresseType(POSTADRESSE_UTLAND)
				.adresselinje1("Main Street 1")
				.landkode("US")
				.build();

		assertThat(adresse.erKomplettForDistribusjon()).isTrue();
	}

	@Test
	void shouldIkkeVaereKomplettForDistribusjonWhenUtenlandsadresseManglerAdresselinje() {
		PostadresseTo adresse = PostadresseTo.builder()
				.adresseType(POSTADRESSE_UTLAND)
				.landkode("US")
				.build();

		assertThat(adresse.erKomplettForDistribusjon()).isFalse();
	}

	@Test
	void shouldIkkeVaereKomplettForDistribusjonWhenUtenlandsadresseManglerLandkode() {
		PostadresseTo adresse = PostadresseTo.builder()
				.adresseType(POSTADRESSE_UTLAND)
				.adresselinje1("Main Street 1")
				.build();

		assertThat(adresse.erKomplettForDistribusjon()).isFalse();
	}

	@Test
	void shouldIkkeVaereKomplettForDistribusjonWhenAdressetypeErUkjent() {
		PostadresseTo adresse = PostadresseTo.builder()
				.adresseType("ukjent")
				.adresselinje1("Main Street 1")
				.postnummer("0123")
				.landkode("NO")
				.build();

		assertThat(adresse.erKomplettForDistribusjon()).isFalse();
	}
}
