package no.nav.regoppslag.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.Appender;
import no.nav.regoppslag.util.LogbackCapturingAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
public class LandkodeServiceTest {

	@Mock
	private Appender mockAppender;
	private static final String NORGE = "Norge";
	private static final String NO = "NO";
	private static final String NOR = "NOR";
	private static final String FINNES_IKKE = "FINNES IKKE";
	private static final String KOSOVO = "Kosovo, Republic of";
	private static final String KOSOVO_LANDKODE_FEIL = "XXK";
	private static final String KOSOVO_LANDKODE_RIKTIG = "XKX";

	private LandkodeService landkodeService = new LandkodeService();

	@BeforeEach
	public void setUp() throws IOException {
	}

	@AfterEach
	public void tearDown() {
		final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		logger.detachAppender(mockAppender);
	}


	@Test
	public void testFinnLandkode() throws Exception {
		String landKode = landkodeService.finnLandkode(NORGE);
		assertThat(landKode, is(NO));
	}

	@Test
	public void testFinnLandNavn() throws Exception {
		String landNavn = landkodeService.finnLandnavn(NO);
		assertThat(landNavn, is(NORGE));
	}

	@Test
	public void testFinnLandNavnKode3() throws Exception {
		String landNavn = landkodeService.finnLandnavn(NOR);
		assertThat(landNavn, is(NORGE));
	}

	@Test
	public void testFinnLandNavnNullLandkode() throws Exception {
		String landNavn = landkodeService.finnLandnavn(null);
		Assertions.assertNull(landNavn);
	}

	@Test
	public void testFinnLandNavnKosovo() throws Exception {
		String landNavn = landkodeService.finnLandnavn(KOSOVO_LANDKODE_FEIL);
		assertThat(landNavn, is(KOSOVO));
		landNavn = landkodeService.finnLandnavn(KOSOVO_LANDKODE_RIKTIG);
		assertThat(landNavn, is(KOSOVO));
	}

	@Test
	public void testFinnUkjentLandNavn() throws Exception {
		LogbackCapturingAppender capture = LogbackCapturingAppender.Factory.weaveInto(LandkodeService.LOG);
		String landNavn = landkodeService.finnLandnavn(FINNES_IKKE);
		LogbackCapturingAppender.Factory.cleanUp();

		Assertions.assertNull(landNavn);
		assertThat(capture.getCapturedLogMessage(), is("Finner ikke land for landkode: FINNES IKKE, sjekk om com.neovisionaries:nv-i18n avhengigheten må oppgraderes til nyere versjon"));
		assertThat(capture.getCapturedLogLevel(), is(Level.WARN));
	}
}

