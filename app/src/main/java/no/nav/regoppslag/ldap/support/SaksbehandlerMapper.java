package no.nav.regoppslag.ldap.support;

import no.nav.dok.metaforcemal.jaxb2.gen.Saksbehandler;

public class SaksbehandlerMapper {
	public Saksbehandler map(String saksbehandlerNavn, Saksbehandler saksbehandler) {
		saksbehandler.setNavn(saksbehandlerNavn);
		return saksbehandler;
	}
}
