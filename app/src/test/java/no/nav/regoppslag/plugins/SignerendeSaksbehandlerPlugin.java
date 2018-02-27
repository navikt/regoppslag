package no.nav.regoppslag.plugins;

import no.nav.regoppslag.ldap.LdapAdeoUserLookup;
import no.nav.regoppslag.xmlenricher.ElementEnricherPlugin;
import no.nav.regoppslag.xmlenricher.exceptions.InvalidElementException;
import no.nav.regoppslag.xmlenricher.exceptions.MissingKeyValueException;
import no.nav.regoppslag.xmlenricher.exceptions.RegistryServiceFunctionalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.inject.Inject;

/**
 * @author Hans Petter Simonsen - Miles
 * Simple plugin for saksbehandler
 */
public class SignerendeSaksbehandlerPlugin implements ElementEnricherPlugin {
	public static final String ELEMENT_NS = "http://nav.no/dok/pesysbrev/felles/v1/PesysFelles";
	public static final String ELEMENT_LOCALNAME = "signerendeSaksbehandler";
	Logger LOG = LoggerFactory.getLogger(SignerendeSaksbehandlerPlugin.class);

	@Override
	public Node processElement(Node content) throws InvalidElementException, MissingKeyValueException, RegistryServiceFunctionalException {
		validateElementType(content);
		Element element = (Element) content;
		Node navn = element.getElementsByTagNameNS("http://nav.no/dok/pesysbrev/felles/v1/Saksbehandler","navn").item(0);
		navn.getFirstChild().setNodeValue("Flittige Frida");

		return element;
	}

	private void validateElementType(Node element) throws InvalidElementException {
		if (!ELEMENT_NS.equals(element.getNamespaceURI())
				|| !ELEMENT_LOCALNAME.equals(element.getLocalName())) {
			throw new InvalidElementException("Unexpected element. Expected {" + ELEMENT_NS + "}" + ELEMENT_LOCALNAME
					+ ". Found {" + element.getNamespaceURI() + "}" + element.getLocalName());
		}
	}
}
