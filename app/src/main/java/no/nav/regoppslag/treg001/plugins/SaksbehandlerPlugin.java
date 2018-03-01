package no.nav.regoppslag.treg001.plugins;

import no.nav.dok.metaforcemal.jaxb2.gen.Saksbehandler;
import no.nav.regoppslag.consumer.ldap.LdapAdeoUserLookup;
import no.nav.regoppslag.consumer.ldap.support.SaksbehandlerMapper;
import no.nav.regoppslag.xmlenricher.ElementEnricherPlugin;
import no.nav.regoppslag.xmlenricher.exceptions.InvalidElementException;
import no.nav.regoppslag.xmlenricher.exceptions.MissingKeyValueException;
import no.nav.regoppslag.xmlenricher.exceptions.RegistryServiceFunctionalException;
import no.nav.regoppslag.xmlenricher.util.JaxbHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class SaksbehandlerPlugin extends JaxbHelper<Saksbehandler> implements ElementEnricherPlugin {
	Logger LOG = LoggerFactory.getLogger(MottakerPlugin.class);

	public SaksbehandlerPlugin() {
		super(Saksbehandler.class);
	}

	@Inject
	private LdapAdeoUserLookup ldapAdeoUserLookup;

	@Inject
	private SaksbehandlerMapper saksbehandlerMapper;


	@Override
	public Node processElement(Node content) throws InvalidElementException, MissingKeyValueException, RegistryServiceFunctionalException {

//		validateElementType(content);
		try {
			Saksbehandler saksbehandler = unmarshal(content);

			String saksbehandlerNavn = ldapAdeoUserLookup.hentFulltNavn(saksbehandler.getAnsattId());

			saksbehandler = saksbehandlerMapper.map(saksbehandlerNavn, saksbehandler);

			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setNamespaceAware(true);

			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document document = builder.newDocument();

			Node node = marshal(saksbehandler, document);
			Document newNode = (Document) node;
			Element documentElement = newNode.getDocumentElement();
			Node renameNode = newNode.renameNode(documentElement, "http://nav.no/dok/pesysbrev/felles/v1/PesysFelles", content.getNodeName());

			return renameNode;
		} catch (JAXBException | ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

//	private void validateElementType(Node element) throws InvalidElementException {
//		if (!ELEMENT_NS.equals(element.getNamespaceURI())
//				|| !ELEMENT_LOCALNAME.equals(element.getLocalName())) {
//			throw new InvalidElementException("Unexpected element. Expected {" + ELEMENT_NS + "}" + ELEMENT_LOCALNAME
//					+ ". Found {" + element.getNamespaceURI() + "}" + element.getLocalName());
//		}
//	}
//		return (ldapConsumer.hentFulltNavn(ansattId));
//	}
}
