package no.nav.regoppslag.consumer.ldap.support;

import no.nav.dok.metaforcemal.jaxb2.gen.NavAnsatt;
import org.springframework.stereotype.Component;

/**
 * @author Ketill Fenne, Visma Consulting AS
 */
@Component
public class SaksbehandlerMapper {
	public NavAnsatt map(String saksbehandlerNavn, NavAnsatt navAnsatt) {
		navAnsatt.setNavn(saksbehandlerNavn);
		return navAnsatt;
	}
}
