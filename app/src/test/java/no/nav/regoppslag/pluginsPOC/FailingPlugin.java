package no.nav.regoppslag.pluginsPOC;

import no.nav.regoppslag.xmlenricher.ElementEnricherPlugin;
import no.nav.regoppslag.xmlenricher.exceptions.InvalidElementException;
import no.nav.regoppslag.xmlenricher.exceptions.MissingKeyValueException;
import no.nav.regoppslag.xmlenricher.exceptions.RegistryServiceFunctionalException;
import org.w3c.dom.Node;

/**
 * @author Hans Petter Simonsen - Miles
 */
public class FailingPlugin implements ElementEnricherPlugin {
	@Override
	public Node processElement(Node content) throws InvalidElementException, MissingKeyValueException, RegistryServiceFunctionalException {
		throw new RegistryServiceFunctionalException("something went wrong calling registryservice");
	}
}
