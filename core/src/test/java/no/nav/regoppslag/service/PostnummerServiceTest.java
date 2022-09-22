package no.nav.regoppslag.service;

import ch.qos.logback.classic.Level;
import no.nav.regoppslag.util.LogbackCapturingAppender;
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
	public void shouldFinnLandNavnNullPoststed() {
		String landNavn = postnummerService.finnPoststed(null);
		assertNull(landNavn);
	}

	@Test
	public void shouldFinnUkjentPoststed() {
		LogbackCapturingAppender capture = LogbackCapturingAppender.Factory.weaveInto(PostnummerService.LOG);
		String poststed = postnummerService.finnPoststed("FINNES IKKE");
		LogbackCapturingAppender.Factory.cleanUp();

		assertNull(poststed);
		assertThat(capture.getCapturedLogMessage(), is("Finner ikke poststed for postnummer: FINNES IKKE, sjekk om ny postnummer.txt må lastes ned."));
		assertThat(capture.getCapturedLogLevel(), is(Level.WARN));
	}
}

