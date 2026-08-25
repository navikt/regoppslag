package no.nav.regoppslag.pdl;

import no.nav.regoppslag.consumer.pdl.to.Bostedsadresse;
import no.nav.regoppslag.consumer.pdl.to.Kontaktadresse;
import no.nav.regoppslag.consumer.pdl.to.Oppholdsadresse;
import no.nav.regoppslag.consumer.pdl.to.Vegadresse;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;
import org.junit.jupiter.api.Test;

import static no.nav.regoppslag.pdl.PDLAdresseValidator.ERROR_MELDING;
import static no.nav.regoppslag.pdl.PDLAdresseValidator.ERROR_MELDING_PDL;
import static no.nav.regoppslag.pdl.PDLAdresseValidator.POSTNUMMER;
import static no.nav.regoppslag.pdl.PDLAdresseValidator.validerOppholdsadresse;
import static no.nav.regoppslag.util.TestDataUtil.POSTNR;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class PDLAdresseValidatorTest {

	@Test
	void shouldGodtaVegadresseMedPostnummer() {
		Vegadresse vegadresse = Vegadresse.builder()
				.postnummer(POSTNR)
				.build();

		assertThatCode(() -> PDLAdresseValidator.validerVegadresse(vegadresse)).doesNotThrowAnyException();
	}

	@Test
	void shouldAvviseVegadresseUtenPostnummer() {
		Vegadresse vegadresse = Vegadresse.builder().build();

		assertThatExceptionOfType(RegoppslagIllegalArgumentException.class)
				.isThrownBy(() -> PDLAdresseValidator.validerVegadresse(vegadresse))
				.withMessage(ERROR_MELDING_PDL.formatted(POSTNUMMER));
	}

	@Test
	void shouldGodtaFrittFormatUtenAdresselinje1WhenCoAdressenavnMangler() {
		Kontaktadresse kontaktadresse = Kontaktadresse.builder()
				.postadresseIFrittFormat(Kontaktadresse.PostadresseIFrittFormat.builder().build())
				.build();

		assertThatCode(() -> PDLAdresseValidator.validerKontaktadresse(kontaktadresse)).doesNotThrowAnyException();
	}

	@Test
	void shouldAvviseFrittFormatUtenAdresselinje1WhenCoAdressenavnFinnes() {
		Kontaktadresse kontaktadresse = Kontaktadresse.builder()
				.coAdressenavn("CO Adressenavn")
				.postadresseIFrittFormat(Kontaktadresse.PostadresseIFrittFormat.builder().build())
				.build();

		assertThatExceptionOfType(RegoppslagIllegalArgumentException.class)
				.isThrownBy(() -> PDLAdresseValidator.validerKontaktadresse(kontaktadresse))
				.withMessage(ERROR_MELDING.formatted("adresselinje1"));
	}

	@Test
	void shouldAvviseBostedsadresseMedUgyldigVegadresse() {
		Bostedsadresse bostedsadresse = Bostedsadresse.builder()
				.vegadresse(Vegadresse.builder().build())
				.build();

		assertThatExceptionOfType(RegoppslagIllegalArgumentException.class)
				.isThrownBy(() -> PDLAdresseValidator.validerBostedsadresse(bostedsadresse))
				.withMessage(ERROR_MELDING_PDL.formatted(POSTNUMMER));
	}

	@Test
	void shouldAvviseOppholdsadresseMedUgyldigVegadresse() {
		Oppholdsadresse oppholdsadresse = Oppholdsadresse.builder()
				.vegadresse(Vegadresse.builder().build())
				.build();

		assertThatExceptionOfType(RegoppslagIllegalArgumentException.class)
				.isThrownBy(() -> validerOppholdsadresse(oppholdsadresse))
				.withMessage(ERROR_MELDING_PDL.formatted(POSTNUMMER));
	}

}
