package no.nav.regoppslag.pluginsPOC;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.xmlenricher.ElementEnricherPlugin;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Hans Petter Simonsen - Miles
 * Simple plugin for saksbehandler
 */
public class SignerendeSaksbehandlerPlugin implements ElementEnricherPlugin {

	@Override
	public Node processElement(Node content, String dokumentTypeId, NamespacePrefixMapper prefixMapper) throws RegOppslagFunctionalException {
		Element element = (Element) content;
		Node navn = element.getElementsByTagNameNS("http://nav.no/dok/pesysbrev/felles/v1/NavAnsatt","navn").item(0);
		navn.getFirstChild().setNodeValue("Flittige Frida");

		return element;
	}
}
