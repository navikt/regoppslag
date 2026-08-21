package no.nav.regoppslag.pdl;

import no.nav.regoppslag.consumer.pdl.to.Bostedsadresse;
import no.nav.regoppslag.consumer.pdl.to.Endring;
import no.nav.regoppslag.consumer.pdl.to.Kontaktadresse;
import no.nav.regoppslag.consumer.pdl.to.Metadata;
import no.nav.regoppslag.consumer.pdl.to.PostadresseTo;
import no.nav.regoppslag.consumer.pdl.to.Vegadresse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static no.nav.regoppslag.consumer.pdl.to.Endring.EndringsType.OPPRETT;
import static no.nav.regoppslag.consumer.pdl.to.InformasjonKilde.FREG;
import static no.nav.regoppslag.consumer.pdl.to.InformasjonKilde.PDL;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_INNLAND;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_UTLAND;
import static no.nav.regoppslag.pdl.Adressevelger.skalPrioritereUtenlandskBostedsadresse;
import static no.nav.regoppslag.pdl.Adressevelger.velgBostedsadresse;
import static no.nav.regoppslag.pdl.Adressevelger.velgAdresseEtterKildeOgGyldighetsperiode;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTNUMMER;
import static org.assertj.core.api.Assertions.assertThat;

class AdressevelgerTest {

	private static final LocalDateTime TIDSPUNKT = LocalDateTime.of(2026, 8, 19, 12, 0);
	private static final LocalDateTime ELDRE_TIDSPUNKT = TIDSPUNKT.minusDays(1);
	private static final LocalDateTime FREMTIDIG_TIDSPUNKT = TIDSPUNKT.plusDays(1);

	@Test
	void shouldReturnereTomOptionalWhenAdresselistenMangler() {
		assertThat(velgAdresseEtterKildeOgGyldighetsperiode(null, TIDSPUNKT)).isEmpty();
	}

	@Test
	void shouldVelgeForsteGyldigePDLKontaktadresse() {
		Kontaktadresse forste = kontaktadresse(PDL.name(), "Forste", ELDRE_TIDSPUNKT);
		Kontaktadresse andre = kontaktadresse(PDL.name(), "Andre", TIDSPUNKT);

		Optional<Kontaktadresse> resultat = velgAdresseEtterKildeOgGyldighetsperiode(List.of(forste, andre), TIDSPUNKT);

		assertThat(resultat).containsSame(forste);
	}

	@Test
	void shouldPrioriterePDLKontaktadresseOverNyereFREGKontaktadresse() {
		Kontaktadresse freg = kontaktadresse(FREG.name(), "FREG", TIDSPUNKT);
		Kontaktadresse pdl = kontaktadresse(PDL.name(), "PDL", ELDRE_TIDSPUNKT);

		Optional<Kontaktadresse> resultat = velgAdresseEtterKildeOgGyldighetsperiode(List.of(freg, pdl), TIDSPUNKT);

		assertThat(resultat).containsSame(pdl);
	}

	@Test
	void shouldVelgeNyesteFREGKontaktadresseWhenPDLKontaktadresseMangler() {
		Kontaktadresse eldre = kontaktadresse(FREG.name(), "Eldre", ELDRE_TIDSPUNKT);
		Kontaktadresse nyere = kontaktadresse(FREG.name(), "Nyere", TIDSPUNKT);

		Optional<Kontaktadresse> resultat = velgAdresseEtterKildeOgGyldighetsperiode(List.of(eldre, nyere), TIDSPUNKT);

		assertThat(resultat).containsSame(nyere);
	}

	@Test
	void shouldReturnereTomOptionalWhenBostedsadresserMangler() {
		assertThat(velgBostedsadresse(null, TIDSPUNKT)).isEmpty();
	}

	@Test
	void shouldIgnorereNullOgUtlopteBostedsadresser() {
		Bostedsadresse utlopt = bostedsadresse(ELDRE_TIDSPUNKT, ELDRE_TIDSPUNKT, null);

		assertThat(velgBostedsadresse(java.util.Arrays.asList(null, utlopt), TIDSPUNKT)).isEmpty();
	}

	@Test
	void shouldVelgeBostedsadresseMedNyesteGyldigFraOgMed() {
		Bostedsadresse eldre = bostedsadresse(ELDRE_TIDSPUNKT, null, null);
		Bostedsadresse nyere = bostedsadresse(TIDSPUNKT, null, null);

		assertThat(velgBostedsadresse(List.of(eldre, nyere), TIDSPUNKT)).containsSame(nyere);
	}

	@Test
	void shouldBrukeSisteEndringWhenBostedsadresseManglerGyldigFraOgMed() {
		Bostedsadresse eldre = bostedsadresse(null, null, ELDRE_TIDSPUNKT);
		Bostedsadresse nyere = bostedsadresse(null, null, TIDSPUNKT);

		assertThat(velgBostedsadresse(List.of(eldre, nyere), TIDSPUNKT)).containsSame(nyere);
	}

	@Test
	void shouldTillateFremtidigGyldigFraOgMedWhenBostedsadresseVelges() {
		Bostedsadresse gjeldende = bostedsadresse(TIDSPUNKT, null, null);
		Bostedsadresse fremtidig = bostedsadresse(FREMTIDIG_TIDSPUNKT, null, null);

		assertThat(velgBostedsadresse(List.of(gjeldende, fremtidig), TIDSPUNKT)).containsSame(fremtidig);
	}

	@Test
	void shouldPrioritereUtenlandskBostedsadresseWhenDenErNyereEnnValgtAdresse() {
		Bostedsadresse bostedsadresse = bostedsadresse(TIDSPUNKT, null, null);
		Kontaktadresse valgtAdresse = kontaktadresse(PDL.name(), "Valgt", ELDRE_TIDSPUNKT);

		assertThat(skalPrioritereUtenlandskBostedsadresse(
				bostedsadresse,
				PostadresseTo.builder().adresseType(POSTADRESSE_UTLAND).build(),
				valgtAdresse
		)).isTrue();
	}

	@Test
	void shouldIkkePrioritereBostedsadresseWhenDenErInnland() {
		Bostedsadresse bostedsadresse = bostedsadresse(TIDSPUNKT, null, null);
		Kontaktadresse valgtAdresse = kontaktadresse(PDL.name(), "Valgt", ELDRE_TIDSPUNKT);

		assertThat(skalPrioritereUtenlandskBostedsadresse(
				bostedsadresse,
				PostadresseTo.builder().adresseType(POSTADRESSE_INNLAND).build(),
				valgtAdresse
		)).isFalse();
	}

	@Test
	void shouldIkkePrioritereUtenlandskBostedsadresseWhenDenIkkeErNyere() {
		Bostedsadresse bostedsadresse = bostedsadresse(ELDRE_TIDSPUNKT, null, null);
		Kontaktadresse valgtAdresse = kontaktadresse(PDL.name(), "Valgt", TIDSPUNKT);

		assertThat(skalPrioritereUtenlandskBostedsadresse(
				bostedsadresse,
				PostadresseTo.builder().adresseType(POSTADRESSE_UTLAND).build(),
				valgtAdresse
		)).isFalse();
	}

	@Test
	void shouldIkkePrioritereUtenlandskBostedsadresseWhenEnDatoMangler() {
		Bostedsadresse bostedsadresse = bostedsadresse(null, null, null);
		Kontaktadresse valgtAdresse = kontaktadresse(PDL.name(), "Valgt", TIDSPUNKT);

		assertThat(skalPrioritereUtenlandskBostedsadresse(
				bostedsadresse,
				PostadresseTo.builder().adresseType(POSTADRESSE_UTLAND).build(),
				valgtAdresse
		)).isFalse();
	}

	private static Kontaktadresse kontaktadresse(String master, String adressenavn, LocalDateTime gyldigFraOgMed) {
		return Kontaktadresse.builder()
				.gyldigFraOgMed(gyldigFraOgMed)
				.gyldigTilOgMed(TIDSPUNKT.plusYears(1))
				.type(POSTADRESSE_INNLAND)
				.vegadresse(Vegadresse.builder()
						.adressenavn(adressenavn)
						.husnummer("1")
						.postnummer(POSTNUMMER)
						.build())
				.metadata(Metadata.builder().master(master).build())
				.build();
	}

	private static Bostedsadresse bostedsadresse(LocalDateTime gyldigFraOgMed,
												 LocalDateTime gyldigTilOgMed,
												 LocalDateTime sisteEndring) {
		List<Endring> endringer = sisteEndring == null ? null : List.of(Endring.builder()
				.registrert(sisteEndring)
				.type(OPPRETT)
				.build());

		return Bostedsadresse.builder()
				.gyldigFraOgMed(gyldigFraOgMed)
				.gyldigTilOgMed(gyldigTilOgMed)
				.metadata(Metadata.builder().master(PDL.name()).endringer(endringer).build())
				.build();
	}
}
