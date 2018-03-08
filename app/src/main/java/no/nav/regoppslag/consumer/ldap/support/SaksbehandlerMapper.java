package no.nav.regoppslag.consumer.ldap.support;

import no.nav.dok.metaforcemal.jaxb2.gen.Saksbehandler;

/**
 * @author Ketill Fenne, Visma Consulting AS
 */
public class SaksbehandlerMapper {
	public Saksbehandler map(String saksbehandlerNavn, Saksbehandler saksbehandler) {
		saksbehandler.setNavn(saksbehandlerNavn);
		return saksbehandler;
	}
}
