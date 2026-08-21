package no.nav.regoppslag.consumer.pdl.to;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static no.nav.regoppslag.consumer.pdl.to.Endring.EndringsType.OPPRETT;
import static no.nav.regoppslag.consumer.pdl.to.GyldigKilde.ETTER_GYLDIG_FRA_ELLER_SISTE_ENDRING;
import static no.nav.regoppslag.consumer.pdl.to.InformasjonKilde.FREG;
import static no.nav.regoppslag.consumer.pdl.to.InformasjonKilde.PDL;
import static org.assertj.core.api.Assertions.assertThat;

class GyldigKildeTest {

	private static final LocalDateTime TIDSPUNKT = LocalDateTime.of(2026, 8, 19, 12, 0);
	private static final LocalDateTime ELDRE_TIDSPUNKT = TIDSPUNKT.minusDays(1);
	private static final LocalDateTime FREMTIDIG_TIDSPUNKT = TIDSPUNKT.plusDays(1);

	@Test
	void shouldAvviseKilderWhenMetadataMangler() {
		Bostedsadresse adresse = Bostedsadresse.builder().build();

		assertThat(adresse.isGyldigPdlKilde(TIDSPUNKT)).isFalse();
		assertThat(adresse.isGyldigFregKilde(TIDSPUNKT)).isFalse();
	}

	@Test
	void shouldGodtaPDLKildeWhenUtlopsdatoMangler() {
		Bostedsadresse adresse = adresse(null, null, PDL.name(), null);

		assertThat(adresse.isGyldigPdlKilde(TIDSPUNKT)).isTrue();
	}

	@Test
	void shouldGodtaFREGKildeWhenUtlopsdatoMangler() {
		Bostedsadresse adresse = adresse(null, null, FREG.name(), null);

		assertThat(adresse.isGyldigFregKilde(TIDSPUNKT)).isTrue();
	}

	@Test
	void shouldAvviseKildeWhenTidspunktErLikUtlopsdato() {
		Bostedsadresse pdlAdresse = adresse(null, TIDSPUNKT, PDL.name(), null);
		Bostedsadresse fregAdresse = adresse(null, TIDSPUNKT, FREG.name(), null);

		assertThat(pdlAdresse.isGyldigPdlKilde(TIDSPUNKT)).isFalse();
		assertThat(fregAdresse.isGyldigFregKilde(TIDSPUNKT)).isFalse();
	}

	@Test
	void shouldIgnorereFremtidigGyldigFraOgMedForPDLKilde() {
		Bostedsadresse adresse = adresse(FREMTIDIG_TIDSPUNKT, FREMTIDIG_TIDSPUNKT.plusDays(1), PDL.name(), null);

		assertThat(adresse.isGyldigPdlKilde(TIDSPUNKT)).isTrue();
	}

	@Test
	void shouldPrioritereGyldigFraOgMedOverSisteEndring() {
		LocalDateTime gyldigFraOgMed = ELDRE_TIDSPUNKT;
		Bostedsadresse adresse = adresse(gyldigFraOgMed, null, PDL.name(), TIDSPUNKT);

		assertThat(adresse.getGyldigFraOgMedOrSisteEndring()).isEqualTo(gyldigFraOgMed);
	}

	@Test
	void shouldBrukeSisteEndringWhenGyldigFraOgMedMangler() {
		LocalDateTime sisteEndring = TIDSPUNKT;
		Bostedsadresse adresse = adresse(null, null, PDL.name(), sisteEndring);

		assertThat(adresse.getGyldigFraOgMedOrSisteEndring()).isEqualTo(sisteEndring);
	}

	@Test
	void shouldSortereManglendeEffektivDatoForanSattEffektivDato() {
		Bostedsadresse utenDato = adresse(null, null, PDL.name(), null);
		Bostedsadresse medDato = adresse(TIDSPUNKT, null, PDL.name(), null);

		assertThat(ETTER_GYLDIG_FRA_ELLER_SISTE_ENDRING.compare(utenDato, medDato)).isNegative();
		assertThat(ETTER_GYLDIG_FRA_ELLER_SISTE_ENDRING.compare(medDato, utenDato)).isPositive();
		assertThat(ETTER_GYLDIG_FRA_ELLER_SISTE_ENDRING.compare(utenDato, adresse(null, null, FREG.name(), null))).isZero();
	}

	private static Bostedsadresse adresse(LocalDateTime gyldigFraOgMed,
										 LocalDateTime gyldigTilOgMed,
										 String master,
										 LocalDateTime sisteEndring) {

		List<Endring> endringer = sisteEndring == null ? null : List.of(Endring.builder()
				.registrert(sisteEndring)
				.type(OPPRETT)
				.build());

		return Bostedsadresse.builder()
				.gyldigFraOgMed(gyldigFraOgMed)
				.gyldigTilOgMed(gyldigTilOgMed)
				.metadata(Metadata.builder().master(master).endringer(endringer).build())
				.build();
	}
}
