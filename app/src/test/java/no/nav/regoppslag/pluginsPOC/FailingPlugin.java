package no.nav.regoppslag.pluginsPOC;

import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.xmlenricher.ElementEnricherPlugin;
import no.nav.regoppslag.xmlenricher.exceptions.InvalidElementException;
import org.w3c.dom.Node;

/**
 * @author Hans Petter Simonsen - Miles
 */
public class FailingPlugin implements ElementEnricherPlugin {
	@Override
	public Node processElement(Node content, String dokumentTypeId) throws RegOppslagFunctionalException, InvalidElementException {
		throw new RegOppslagFunctionalException("something went wrong calling registryservice");
	}
}
