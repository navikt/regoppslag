package no.nav.regoppslag.treg001.plugins;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.metaforcemal.jaxb2.gen.NavEnhet;
import no.nav.regoppslag.consumer.norg2.OrganisasjonEnhetKontaktinformasjonV1Consumer;
import no.nav.regoppslag.consumer.norg2.support.Norg2Mapper;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.xmlenricher.ElementEnricherPlugin;
import no.nav.regoppslag.xmlenricher.exceptions.InvalidElementException;
import no.nav.regoppslag.xmlenricher.util.JaxbHelper;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.Organisasjonsenhet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

@Component
@Scope("prototype")
@Slf4j
public class NavOrgenhetPlugin extends JaxbHelper<NavEnhet> implements ElementEnricherPlugin {
	Logger LOG = LoggerFactory.getLogger(NavOrgenhetPlugin.class);
	public static final String ELEMENT_NS = "http://nav.no/dok/pesysbrev/felles/v1/PesysFelles";
	public static final String ELEMENT_LOCALNAME = "kontaktinformasjon";

	private OrganisasjonEnhetKontaktinformasjonV1Consumer norg2Consumer;

	private Norg2Mapper norg2Mapper;


	public NavOrgenhetPlugin() {
		super(NavEnhet.class);
	}

	@Inject
	public NavOrgenhetPlugin(OrganisasjonEnhetKontaktinformasjonV1Consumer norg2Consumer, Norg2Mapper norg2Mapper) {
		super(NavEnhet.class);
		this.norg2Consumer = norg2Consumer;
		this.norg2Mapper = norg2Mapper;
	}


	@Override
	public Node processElement(Node content, String dokumentTypeId) throws RegOppslagFunctionalException, RegOppslagTechnicalException, InvalidElementException {
		validateElementType(content);
		try {
			
			log.info("Henter NavOrgenhet info");
			
			NavEnhet navEnhet = unmarshal(content);

			//validateNavEnhet(navEnhet);
			
			Organisasjonsenhet wsEnhet = norg2Consumer.hentKontaktinformasjonForEnhet(navEnhet.getEnhetsId());

			//TODO logikk for å sjekke hvilken mapper som skal kalles
			//norg2Mapper.mapBesokadresse (wsEnhet, navEnhet);
			norg2Mapper.mapPostadresse(wsEnhet, navEnhet);

			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setNamespaceAware(true);

			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document document = builder.newDocument();

			Node node = marshal(navEnhet, document);
			Document newNode = (Document) node;
			Element documentElement = newNode.getDocumentElement();

			log.info("NavOrgenhet er beriket med data");
			return newNode.renameNode(documentElement, content.getNamespaceURI(), content.getLocalName());
			
		} catch (JAXBException | ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void validateNavEnhet(NavEnhet navEnhet) throws RegOppslagFunctionalException {
		
		if(navEnhet.getEnhetsId()==null){
			throw new RegOppslagFunctionalException("Feil i NavOrgenhetPlugin: NavEnhetdata mangler enhetsId");
		}
	}
	
	private void validateElementType(Node element) throws InvalidElementException {
		if (!ELEMENT_NS.equals(element.getNamespaceURI())
				|| !ELEMENT_LOCALNAME.equals(element.getLocalName())) {
			throw new InvalidElementException("Unexpected element. Expected {" + ELEMENT_NS + "}" + ELEMENT_LOCALNAME
					+ ". Found {" + element.getNamespaceURI() + "}" + element.getLocalName());
		}
	}}
