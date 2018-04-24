package no.nav.regoppslag.service;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.junit.Assert.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.Appender;
import no.nav.regoppslag.util.LogbackCapturingAppender;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class LandkodeServiceTest {

	@Mock
	private Appender mockAppender;

	private LandkodeService landkodeService = new LandkodeService();

	@Before
	public void setUp() throws IOException {
		landkodeService.init();
	}

	@After
	public void tearDown() {
		final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		logger.detachAppender(mockAppender);
	}


	@Test
	public void testFinnLandkode() throws Exception {
		String landKode = landkodeService.finnLandkode("NORWAY");
		assertThat(landKode, is("NO"));
	}

	@Test
	public void testFinnLandNavn() throws Exception {
		String landNavn = landkodeService.finnLandnavn("NO");
		assertThat(landNavn, is("NORWAY"));
	}

	@Test
	public void testFinnLandNavnKode3() throws Exception {
		String landNavn = landkodeService.finnLandnavn("NOR");
		assertThat(landNavn, is("NORWAY"));
	}

	@Test
	public void testFinnLandNavnNullLandkode() throws Exception {
		String landNavn = landkodeService.finnLandnavn(null);
		assertThat(landNavn, isEmptyOrNullString());
	}

	@Test
	public void testFinnUkjentLandNavn() throws Exception {
		LogbackCapturingAppender capture = LogbackCapturingAppender.Factory.weaveInto(LandkodeService.LOG);
		String landNavn = landkodeService.finnLandnavn("FINNES IKKE");
		LogbackCapturingAppender.Factory.cleanUp();

		assertThat(landNavn, isEmptyOrNullString());
		assertThat(capture.getCapturedLogMessage(), is("Finner ikke land for landkode: FINNES IKKE, sjekk om ny landkoder.txt må lastes ned/endres."));
		assertThat(capture.getCapturedLogLevel(), is(Level.WARN));
	}
}

