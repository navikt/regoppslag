package no.nav.regoppslag.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class PostnummerServiceTest {

	private PostnummerService postnummerService;

	@BeforeEach
	public void setUp() throws IOException {
		postnummerService = new PostnummerService();
	}

	@Test
	public void shouldFinnPoststed() {
		String poststed = postnummerService.finnPoststed("1400");

		assertThat(poststed).isEqualTo("SKI");
	}

	@Test
	public void shouldReturnNullWhenNullPostnummer() {
		String poststed = postnummerService.finnPoststed(null);

		assertThat(poststed).isNull();
	}

	@Test
	public void shouldReturnPostnummerWhenUkjentPostnummer() {
		String poststed = postnummerService.finnPoststed("111111");

		assertThat(poststed).isEqualTo("111111");
	}
}

