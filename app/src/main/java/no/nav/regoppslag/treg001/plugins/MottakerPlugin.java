package no.nav.regoppslag.treg001.plugins;

import no.nav.dok.metaforcemal.jaxb2.gen.Mottaker;
import no.nav.regoppslag.consumer.organisasjonv4.OrganisasjonV4Consumer;
import no.nav.regoppslag.consumer.organisasjonv4.support.OrganisasjonV4Mapper;
import no.nav.regoppslag.consumer.personv3.PersonV3Consumer;
import no.nav.regoppslag.consumer.personv3.support.PersonV3Mapper;
import no.nav.regoppslag.xmlenricher.ElementEnricherPlugin;
import no.nav.regoppslag.xmlenricher.exceptions.InvalidElementException;
import no.nav.regoppslag.xmlenricher.exceptions.MissingKeyValueException;
import no.nav.regoppslag.xmlenricher.exceptions.RegistryServiceFunctionalException;
import no.nav.regoppslag.xmlenricher.util.JaxbHelper;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Person;
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

/**
 * @author Hans Petter Simonsen - Miles
 */
public class MottakerPlugin extends JaxbHelper<Mottaker> implements ElementEnricherPlugin {
	Logger LOG = LoggerFactory.getLogger(MottakerPlugin.class);
	public static final String ELEMENT_NS = "http://nav.no/dok/pesysbrev/felles/v1/PesysFelles";
	public static final String ELEMENT_LOCALNAME = "mottaker";

	private PersonV3Consumer personV3Consumer;

	private PersonV3Mapper personV3Mapper;

	private OrganisasjonV4Consumer organisasjonV4Consumer;

	private OrganisasjonV4Mapper organisasjonV4Mapper;

	public MottakerPlugin() {
		super(Mottaker.class);
	}

	@Inject
	public MottakerPlugin(PersonV3Consumer personV3Consumer, PersonV3Mapper personV3Mapper, OrganisasjonV4Consumer organisasjonV4Consumer, OrganisasjonV4Mapper organisasjonV4Mapper) {
		super(Mottaker.class);
		this.personV3Consumer = personV3Consumer;
		this.personV3Mapper = personV3Mapper;
		this.organisasjonV4Consumer = organisasjonV4Consumer;
		this.organisasjonV4Mapper = organisasjonV4Mapper;
	}


	@Override
	public Node processElement(Node content) throws InvalidElementException, MissingKeyValueException, RegistryServiceFunctionalException {
		validateElementType(content);
		try {
			Mottaker mottaker = unmarshal(content);

			if ( "PERSON".equals(mottaker.getTypeKode().name())) {
				Bruker person = personV3Consumer.hentPerson(mottaker.getId());
				personV3Mapper.map(person, mottaker);
			} else {
				Organisasjon organisasjon = organisasjonV4Consumer.hentOrganisasjon(mottaker.getId());
				organisasjonV4Mapper.map(organisasjon, mottaker);
			}

			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setNamespaceAware(true);

			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document document = builder.newDocument();

			Node node = marshal(mottaker, document);
			Document newNode = (Document) node;
			Element documentElement = newNode.getDocumentElement();
			Node renameNode = newNode.renameNode(documentElement, "http://nav.no/dok/pesysbrev/felles/v1/PesysFelles", "f:mottaker");

			return renameNode;
		} catch (JAXBException | ParserConfigurationException e) {
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