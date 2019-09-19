package no.nav.regoppslag.service;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.junit.Assert.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import no.nav.regoppslag.util.LogbackCapturingAppender;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class PostnummerServiceTest {

	@Mock
	private Appender mockAppender;

	@Captor
	private ArgumentCaptor<LoggingEvent> captorLoggingEvent;

	private PostnummerService postnumnmerService = new PostnummerService();

	@Before
	public void setUp() throws IOException{
		postnumnmerService.init();
	}

	@After
	public void tearDown() {
		final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		logger.detachAppender(mockAppender);
	}


	@Test
	public void testFinnPoststed() throws Exception {
		String poststed = postnumnmerService.finnPoststed("1400");
		assertThat(poststed, is("SKI"));
	}

	@Test
	public void testFinnLandNavnNullPoststed() throws Exception {
		String landNavn = postnumnmerService.finnPoststed(null);
		assertThat(landNavn, isEmptyOrNullString());
	}

	@Test
	public void testFinnUkjentPoststed() throws Exception {
		LogbackCapturingAppender capture = LogbackCapturingAppender.Factory.weaveInto(PostnummerService.LOG);
		String landNavn = postnumnmerService.finnPoststed("FINNES IKKE");
		LogbackCapturingAppender.Factory.cleanUp();

		assertThat(landNavn, isEmptyOrNullString());
		assertThat(capture.getCapturedLogMessage(), is("Finner ikke poststed for postnummer: FINNES IKKE, sjekk om ny postnummer.txt må lastes ned."));
		assertThat(capture.getCapturedLogLevel(), is(Level.WARN));
	}
}

