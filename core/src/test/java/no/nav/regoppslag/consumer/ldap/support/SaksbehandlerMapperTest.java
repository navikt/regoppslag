package no.nav.regoppslag.consumer.ldap.support;

import no.nav.dok.brevdata.felles.v1.navfelles.NavAnsatt;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SaksbehandlerMapperTest {
	private final String SAKSBEHANDLER_NAVN = "Sverre Saksbehandler";

	private SaksbehandlerMapper saksbehandlerMapper = new SaksbehandlerMapper();

	@Test
	public void mapSaksbehandler() {
		NavAnsatt navAnsatt = new NavAnsatt();
		navAnsatt = saksbehandlerMapper.map(SAKSBEHANDLER_NAVN, navAnsatt);
		MatcherAssert.assertThat(navAnsatt.getNavn(), is(SAKSBEHANDLER_NAVN));
	}

	@Test
	public void mapNullSaksbehandler() {
		NavAnsatt navAnsatt = new NavAnsatt();
		navAnsatt = saksbehandlerMapper.map(null, navAnsatt);
		Assertions.assertNull(navAnsatt.getNavn());
	}

}
