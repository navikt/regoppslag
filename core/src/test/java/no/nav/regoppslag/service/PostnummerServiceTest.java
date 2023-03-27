package no.nav.regoppslag.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PostnummerServiceTest {

	private PostnummerService postnummerService;

	@BeforeEach
	public void setUp() throws IOException {
		postnummerService = new PostnummerService();
	}

	@Test
	public void shouldFinnPoststed() {
		String poststed = postnummerService.finnPoststed("1400");

		assertThat(poststed, is("SKI"));
	}

	@Test
	public void shouldReturnNullWhenNullPostnummer() {
		String landNavn = postnummerService.finnPoststed(null);

		assertNull(landNavn);
	}

	@Test
	public void shouldReturnNullWhenUkjentPostnummer() {
		String poststed = postnummerService.finnPoststed("111111");

		assertNull(poststed);
	}

	// workaround for å få gjennom brev som skal til endrede poststeder
	@Test
	void shouldFinnWorkaroundPoststeder() {
		String haroy = postnummerService.finnPoststed("6485");
		assertThat(haroy, is("HARØY"));
		String oslo = postnummerService.finnPoststed("0025");
		assertThat(oslo, is("OSLO"));
	}
}

