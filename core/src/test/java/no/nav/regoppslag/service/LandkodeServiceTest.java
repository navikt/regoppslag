package no.nav.regoppslag.service;

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

import static ch.qos.logback.classic.Level.WARN;
import static no.nav.regoppslag.service.LandkodeService.LOG;
import static no.nav.regoppslag.service.LandkodeService.finnLandkode;
import static no.nav.regoppslag.service.LandkodeService.finnLandnavn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

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

	@BeforeEach
	public void setUp() throws IOException {
	}

	@AfterEach
	public void tearDown() {
		final Logger logger = (Logger) LoggerFactory.getLogger(ROOT_LOGGER_NAME);
		logger.detachAppender(mockAppender);
	}


	@Test
	public void testFinnLandkode() {
		String landKode = finnLandkode(NORGE);
		assertThat(landKode, is(NO));
	}

	@Test
	public void testFinnLandNavn() {
		String landNavn = finnLandnavn(NO);
		assertThat(landNavn, is(NORGE));
	}

	@Test
	public void testFinnLandNavnKode3() {
		String landNavn = finnLandnavn(NOR);
		assertThat(landNavn, is(NORGE));
	}

	@Test
	public void testFinnLandNavnNullLandkode() {
		String landNavn = finnLandnavn(null);
		assertNull(landNavn);
	}

	@Test
	public void testFinnLandNavnKosovo() {
		String landNavn = finnLandnavn(KOSOVO_LANDKODE_FEIL);
		assertThat(landNavn, is(KOSOVO));
		landNavn = finnLandnavn(KOSOVO_LANDKODE_RIKTIG);
		assertThat(landNavn, is(KOSOVO));
	}

	@Test
	public void testFinnUkjentLandNavn() {
		LogbackCapturingAppender capture = LogbackCapturingAppender.Factory.weaveInto(LOG);
		String landNavn = finnLandnavn(FINNES_IKKE);
		LogbackCapturingAppender.Factory.cleanUp();

		assertNull(landNavn);
		assertThat(capture.getCapturedLogMessage(), is("Finner ikke land for landkode: FINNES IKKE, sjekk om com.neovisionaries:nv-i18n avhengigheten må oppgraderes til nyere versjon"));
		assertThat(capture.getCapturedLogLevel(), is(WARN));
	}
}

