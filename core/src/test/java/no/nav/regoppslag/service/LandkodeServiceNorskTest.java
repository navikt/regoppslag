package no.nav.regoppslag.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
public class LandkodeServiceNorskTest {

	private LandkodeServiceNorsk landkodeServiceNorsk;

	@BeforeEach
	public void setUp() throws IOException {
		landkodeServiceNorsk = new LandkodeServiceNorsk();
	}

	@ParameterizedTest
	@CsvSource(value = {
			"NO, NORGE",
			"???, UOPPGITT/UKJENT",
			"null, null"
	}, nullValues = {"null"})
	public void testFinnLand(String landkode, String faktiskLandnavn) {
		String landNavn = landkodeServiceNorsk.finnLand(landkode);
		assertThat(landNavn, is(faktiskLandnavn));
	}
}
