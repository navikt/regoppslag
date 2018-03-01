package no.nav.regoppslag.treg001.plugins;

import no.nav.dok.metaforcemal.jaxb2.gen.NavEnhet;
import no.nav.regoppslag.consumer.ldap.LdapAdeoUserLookup;
import no.nav.regoppslag.consumer.ldap.support.SaksbehandlerMapper;
import no.nav.regoppslag.consumer.norg2.OrganisasjonEnhetKontaktinformasjonV2Consumer;
import no.nav.regoppslag.consumer.norg2.support.Norg2Mapper;
import no.nav.regoppslag.xmlenricher.ElementEnricherPlugin;
import no.nav.regoppslag.xmlenricher.exceptions.InvalidElementException;
import no.nav.regoppslag.xmlenricher.exceptions.MissingKeyValueException;
import no.nav.regoppslag.xmlenricher.exceptions.RegistryServiceFunctionalException;
import no.nav.regoppslag.xmlenricher.util.JaxbHelper;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v2.informasjon.WSOrganisasjonsenhet;
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
import java.nio.file.WatchService;

public class NavOrgenhetPlugin extends JaxbHelper<NavEnhet> implements ElementEnricherPlugin {
	Logger LOG = LoggerFactory.getLogger(NavOrgenhetPlugin.class);
	public static final String ELEMENT_NS = "http://nav.no/dok/pesysbrev/felles/v1/PesysFelles";
	public static final String ELEMENT_LOCALNAME = "kontaktinformasjon";

	private OrganisasjonEnhetKontaktinformasjonV2Consumer norg2Consumer;

	private Norg2Mapper norg2Mapper;


	public NavOrgenhetPlugin() {
		super(NavEnhet.class);
	}

	@Inject
	public NavOrgenhetPlugin(OrganisasjonEnhetKontaktinformasjonV2Consumer norg2Consumer, Norg2Mapper norg2Mapper) {
		super(NavEnhet.class);
		this.norg2Consumer = norg2Consumer;
		this.norg2Mapper = norg2Mapper;
	}


	@Override
	public Node processElement(Node content) throws InvalidElementException, MissingKeyValueException, RegistryServiceFunctionalException {
		validateElementType(content);
		try {
			NavEnhet navEnhet = unmarshal(content);

			WSOrganisasjonsenhet wsEnhet = norg2Consumer.hentKontaktinformasjonForEnhet(navEnhet.getEnhetsId());

			navEnhet = norg2Mapper.map(wsEnhet, navEnhet);

			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setNamespaceAware(true);

			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document document = builder.newDocument();

			Node node = marshal(navEnhet, document);
			Document newNode = (Document) node;
			Element documentElement = newNode.getDocumentElement();
			Node renameNode = newNode.renameNode(documentElement, "http://nav.no/dok/pesysbrev/felles/v1/PesysFelles", "f:kontaktinformasjon");

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
	}}
