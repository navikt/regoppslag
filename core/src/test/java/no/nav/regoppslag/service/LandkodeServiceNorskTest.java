package no.nav.regoppslag.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.core.Appender;
import no.nav.regoppslag.util.LogbackCapturingAppender;
import org.junit.Ignore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.LoggingEvent;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
public class LandkodeServiceNorskTest {

	@Mock
	private Appender mockAppender;

	@Captor
	private ArgumentCaptor<LoggingEvent> captorLoggingEvent;

	private LandkodeServiceNorsk landkodeServiceNorsk;

	@BeforeEach
	public void setUp() throws IOException {
		landkodeServiceNorsk = new LandkodeServiceNorsk();
	}

	@AfterEach
	public void tearDown() {
		final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
	}


	@Test
	public void testFinnLand() {
		String landNavn = landkodeServiceNorsk.finnLand("NO");
		assertThat(landNavn, is("NORGE"));
	}

	@Test
	public void testFinnLandNull() {
		String landNavn = landkodeServiceNorsk.finnLand(null);
		assertNull(landNavn);
	}

	@Test
	public void testFinnLandUkjentLandskode() {
		String landNavn = landkodeServiceNorsk.finnLand("???");
		assertThat(landNavn, is("UOPPGITT/UKJENT"));
	}
}
