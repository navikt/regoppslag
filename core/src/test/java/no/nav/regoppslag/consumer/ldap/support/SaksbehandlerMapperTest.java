package no.nav.regoppslag.consumer.ldap.support;

import no.nav.dok.brevdata.felles.v1.navfelles.NavAnsatt;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SaksbehandlerMapperTest {

	private final SaksbehandlerMapper saksbehandlerMapper = new SaksbehandlerMapper();

	@ParameterizedTest
	@ValueSource(strings = {"Sverre Saksbehandler"})
	@NullSource
	public void mapSaksbehandler(String saksbehandler) {
		NavAnsatt navAnsatt = new NavAnsatt();
		navAnsatt = saksbehandlerMapper.map(saksbehandler, navAnsatt);
		assertThat(navAnsatt.getNavn(), is(saksbehandler));
	}

}