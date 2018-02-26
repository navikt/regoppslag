package no.nav.regoppslag.plugins;

import no.nav.dok.metaforcemal.jaxb2.gen.Mottaker;
import no.nav.dok.metaforcemal.jaxb2.gen.NorskPostadresse;
import no.nav.regoppslag.xmlenricher.exceptions.InvalidElementException;
import no.nav.regoppslag.xmlenricher.exceptions.MissingKeyValueException;
import no.nav.regoppslag.xmlenricher.exceptions.RegistryServiceFunctionalException;
import no.nav.regoppslag.xmlenricher.ElementEnricherPlugin;
import no.nav.regoppslag.xmlenricher.util.JaxbHelper;
import no.nav.regoppslag.xmlenricher.util.NamespacePrefixMapperHelper;
import no.nav.regoppslag.xmlenricher.util.UniversalNamespaceCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * @author Hans Petter Simonsen - Miles
 */
public class MottakerPlugin1 extends JaxbHelper<Mottaker> implements ElementEnricherPlugin {
	Logger LOG = LoggerFactory.getLogger(MottakerPlugin1.class);
	public static final String ELEMENT_NS = "http://nav.no/dok/pesysbrev/felles/v1/PesysFelles";
	public static final String ELEMENT_LOCALNAME = "mottaker";

	public MottakerPlugin1() {
		super(Mottaker.class);
	}

	@Override
//	public Node processElement(Node content, NamespaceContext namespaceContext) throws InvalidElementException, MissingKeyValueException, RegistryServiceFunctionalException {
	public Node processElement(Node content) throws InvalidElementException, MissingKeyValueException, RegistryServiceFunctionalException {
//		if (namespaceContext instanceof UniversalNamespaceCache) {
//			setNamespacePrefixMapper(new NamespacePrefixMapperHelper((UniversalNamespaceCache)namespaceContext));
//		}
		validateElementType(content);
		try {
			Mottaker mottaker = unmarshal(content);
			LOG.info("Looking up mottaker with id {}", mottaker.getId());
			mottaker.setNavn("Test Testesen");
			NorskPostadresse addresse = new NorskPostadresse();
			addresse.setAdresselinje1("Heimegata 2");
			addresse.setPostnummer("1234");
			addresse.setPoststed("Gokk");
			mottaker.setAdresse(addresse);

			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setNamespaceAware(true);

			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document document = builder.newDocument();

			Node node = marshal(mottaker, document);
			Document newNode = (Document)node;
			Element documentElement = newNode.getDocumentElement();
			Node renameNode = newNode.renameNode(documentElement, "http://nav.no/dok/pesysbrev/felles/v1/PesysFelles", "f:mottaker");

			return renameNode;
		} catch (JAXBException|ParserConfigurationException  e) {
			throw new RuntimeException(e);
		}
	}

	private void validateElementType(Node element) throws InvalidElementException {
		if (!ELEMENT_NS.equals(element.getNamespaceURI())
				|| !ELEMENT_LOCALNAME.equals(element.getLocalName())) {
			throw new InvalidElementException("Unexpected element. Expected {" + ELEMENT_NS + "}" + ELEMENT_LOCALNAME
					+ ". Found {" + element.getNamespaceURI() + "}" + element.getLocalName());
		}
	}
}
