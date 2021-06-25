package no.nav.regoppslag.consumer.ldap.support;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;

import no.nav.dok.brevdata.felles.v1.navfelles.NavAnsatt;
import org.junit.jupiter.api.Test;

public class SaksbehandlerMapperTest {
	private final String SAKSBEHANDLER_NAVN = "Sverre Saksbehandler";

	private SaksbehandlerMapper saksbehandlerMapper = new SaksbehandlerMapper();

	@Test
	public void mapSaksbehandler() {
		NavAnsatt navAnsatt = new NavAnsatt();
		navAnsatt = saksbehandlerMapper.map(SAKSBEHANDLER_NAVN, navAnsatt);
		assertThat(navAnsatt.getNavn(),is(SAKSBEHANDLER_NAVN));
	}

	@Test
	public void mapNullSaksbehandler() {
		NavAnsatt navAnsatt = new NavAnsatt();
		navAnsatt = saksbehandlerMapper.map(null, navAnsatt);
		assertThat(navAnsatt.getNavn(),isEmptyOrNullString());
	}

}
