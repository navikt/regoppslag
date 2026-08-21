package no.nav.regoppslag.consumer.pdl.to;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static no.nav.regoppslag.consumer.pdl.to.InformasjonKilde.FREG;
import static no.nav.regoppslag.consumer.pdl.to.InformasjonKilde.PDL;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_INNLAND;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_UTLAND;
import static org.assertj.core.api.Assertions.assertThat;

class KontaktadresseTest {

	private static final LocalDateTime TIDSPUNKT = LocalDateTime.of(2026, 8, 19, 12, 0);
	private static final LocalDateTime ELDRE_TIDSPUNKT = TIDSPUNKT.minusDays(1);
	private static final LocalDateTime NOW = LocalDateTime.now();

	@Test
	void shouldAvviseFREGKildeWhenGyldigFraOgMedOgMetadataMangler() {
		Kontaktadresse adresse = Kontaktadresse.builder().build();

		assertThat(adresse.isGyldigFregKilde(TIDSPUNKT)).isFalse();
	}

	@Test
	void shouldGodtaUtloptFREGKildeWhenGyldigFraOgMedMangler() {
		Kontaktadresse adresse = Kontaktadresse.builder()
				.gyldigTilOgMed(ELDRE_TIDSPUNKT)
				.metadata(Metadata.builder().master(FREG.name()).build())
				.build();

		assertThat(adresse.isGyldigFregKilde(TIDSPUNKT)).isTrue();
	}

	@Test
	void shouldAvviseIkkeFREGKildeWhenGyldigFraOgMedMangler() {
		Kontaktadresse adresse = Kontaktadresse.builder()
				.metadata(Metadata.builder().master(PDL.name()).build())
				.build();

		assertThat(adresse.isGyldigFregKilde(TIDSPUNKT)).isFalse();
	}

	@Test
	void shouldAvviseFREGKildeWhenGyldigFraOgMedErFremtidig() {
		Kontaktadresse adresse = Kontaktadresse.builder()
				.gyldigFraOgMed(NOW.plusDays(1))
				.gyldigTilOgMed(NOW.plusDays(2))
				.metadata(Metadata.builder().master(FREG.name()).build())
				.build();

		assertThat(adresse.isGyldigFregKilde(TIDSPUNKT)).isFalse();
	}

	@Test
	void shouldSjekkeUtlopsdatoMotInnsendtTidspunktWhenGyldigFraOgMedErSatt() {
		Kontaktadresse adresse = Kontaktadresse.builder()
				.gyldigFraOgMed(NOW.minusDays(1))
				.gyldigTilOgMed(TIDSPUNKT)
				.metadata(Metadata.builder().master(FREG.name()).build())
				.build();

		assertThat(adresse.isGyldigFregKilde(TIDSPUNKT)).isFalse();
	}

	@Test
	void shouldVaereInnlandWhenTypeErInnlandUavhengigAvStoreOgSmaBokstaver() {
		Kontaktadresse adresse = Kontaktadresse.builder()
				.type(POSTADRESSE_INNLAND.toLowerCase())
				.build();

		assertThat(adresse.erInnland()).isTrue();
		assertThat(adresse.erUtland()).isFalse();
	}

	@Test
	void shouldVaereUtlandWhenTypeErUtlandUavhengigAvStoreOgSmaBokstaver() {
		Kontaktadresse adresse = Kontaktadresse.builder()
				.type(POSTADRESSE_UTLAND.toLowerCase())
				.build();

		assertThat(adresse.erUtland()).isTrue();
		assertThat(adresse.erInnland()).isFalse();
	}

	@Test
	void shouldIkkeVaereInnlandEllerUtlandWhenTypeMangler() {
		Kontaktadresse adresse = Kontaktadresse.builder().build();

		assertThat(adresse.erInnland()).isFalse();
		assertThat(adresse.erUtland()).isFalse();
	}

	@Test
	void shouldIkkeVaereInnlandEllerUtlandWhenTypeErUkjent() {
		Kontaktadresse adresse = Kontaktadresse.builder()
				.type("ukjent")
				.build();

		assertThat(adresse.erInnland()).isFalse();
		assertThat(adresse.erUtland()).isFalse();
	}
}
