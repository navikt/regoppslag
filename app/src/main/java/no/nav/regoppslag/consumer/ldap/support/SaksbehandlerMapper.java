package no.nav.regoppslag.consumer.ldap.support;

import no.nav.dok.metaforcemal.jaxb2.gen.Saksbehandler;
import org.springframework.stereotype.Component;

/**
 * @author Ketill Fenne, Visma Consulting AS
 */
@Component
public class SaksbehandlerMapper {
	public Saksbehandler map(String saksbehandlerNavn, Saksbehandler saksbehandler) {
		saksbehandler.setNavn(saksbehandlerNavn);
		return saksbehandler;
	}
}
