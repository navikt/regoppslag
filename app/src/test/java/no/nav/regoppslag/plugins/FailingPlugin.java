package no.nav.regoppslag.plugins;

import no.nav.regoppslag.xmlenricher.exceptions.InvalidElementException;
import no.nav.regoppslag.xmlenricher.exceptions.MissingKeyValueException;
import no.nav.regoppslag.xmlenricher.exceptions.RegistryServiceFunctionalException;
import no.nav.regoppslag.xmlenricher.ElementEnricherPlugin;
import org.w3c.dom.Node;

import javax.xml.namespace.NamespaceContext;

/**
 * @author Hans Petter Simonsen - Miles
 */
public class FailingPlugin implements ElementEnricherPlugin {
	@Override
	public Node processElement(Node content, NamespaceContext namespaceContext) throws InvalidElementException, MissingKeyValueException, RegistryServiceFunctionalException {
		throw new RegistryServiceFunctionalException("something went wrong calling registryservice");
	}
}
