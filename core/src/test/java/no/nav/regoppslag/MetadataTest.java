package no.nav.regoppslag;

import no.nav.regoppslag.consumer.pdl.to.Endring;
import no.nav.regoppslag.consumer.pdl.to.Metadata;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MetadataTest {

	@Test
	public void shouldGetLatestEndring() {
		LocalDateTime nyesteDato = LocalDateTime.now();
		Metadata metadata = Metadata.builder()
				.endringer(List.of(
						createEndring(nyesteDato.minusDays(5)),
						createEndring(nyesteDato),
						createEndring(nyesteDato.minusMinutes(3)),
						createEndring(nyesteDato.minusDays(10))))
				.build();
		LocalDateTime datoForSisteEndring = metadata.getDatoForSisteEndring();

		assertThat(datoForSisteEndring).isEqualTo(nyesteDato);
	}

	@Test
	public void shouldGetNullWhenEndringerIsNull() {
		Metadata metadata = Metadata.builder().build();
		LocalDateTime nullDate = metadata.getDatoForSisteEndring();
		assertThat(nullDate).isNull();
	}

	@Test
	public void shouldGetNullWhenNoEndringer() {
		Metadata metadata = Metadata.builder().endringer(Collections.emptyList()).build();
		LocalDateTime nullDate = metadata.getDatoForSisteEndring();
		assertThat(nullDate).isNull();
	}

	private Endring createEndring(LocalDateTime opprettetDato) {
		return Endring.builder()
				.registrert(opprettetDato)
				.build();
	}
}