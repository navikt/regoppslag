package no.nav.regoppslag.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.core.Appender;
import no.nav.regoppslag.util.LogbackCapturingAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
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
public class PostnummerServiceTest {

	@Mock
	private Appender mockAppender;

	@Captor
	private ArgumentCaptor<LoggingEvent> captorLoggingEvent;

	private PostnummerService postnumnmerService;

	@BeforeEach
	public void setUp() throws IOException {
		postnumnmerService = new PostnummerService();
	}

	@AfterEach
	public void tearDown() {
		final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		//logger.detachAppender(mockAppender);
	}


	@Test
	public void testFinnPoststed() {
		String poststed = postnumnmerService.finnPoststed("1400");
		assertThat(poststed, is("SKI"));
	}

	@Test
	public void testFinnLandNavnNullPoststed() {
		String landNavn = postnumnmerService.finnPoststed(null);
		assertNull(landNavn);
	}

	@Test
	public void testFinnUkjentPoststed() {
		LogbackCapturingAppender capture = LogbackCapturingAppender.Factory.weaveInto(PostnummerService.LOG);
		String landNavn = postnumnmerService.finnPoststed("FINNES IKKE");
		LogbackCapturingAppender.Factory.cleanUp();

		assertNull(landNavn);
		assertThat(capture.getCapturedLogMessage(), is("Finner ikke poststed for postnummer: FINNES IKKE, sjekk om ny postnummer.txt må lastes ned."));
		assertThat(capture.getCapturedLogLevel(), is(Level.WARN));
	}
}

