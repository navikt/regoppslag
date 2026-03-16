package no.nav.regoppslag.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class PostnummerServiceTest {

	@BeforeEach
	public void setUp() throws IOException {
	}

	@Test
	public void shouldFinnPoststed() {
		String poststed = PostnummerService.finnPoststed("1400");

		assertThat(poststed).isEqualTo("SKI");
	}

	@Test
	public void shouldReturnNullWhenNullPostnummer() {
		String poststed = PostnummerService.finnPoststed(null);

		assertThat(poststed).isNull();
	}

	@Test
	public void shouldReturnPostnummerWhenUkjentPostnummer() {
		String poststed = PostnummerService.finnPoststed("111111");

		assertThat(poststed).isEqualTo("111111");
	}
}

