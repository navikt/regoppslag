package no.nav.regoppslag.treg001.plugins;

import static no.nav.regoppslag.consumer.norg2.OrganisasjonEnhetKontaktinformasjonV1Consumer.HENT_ENHET_NAVN;
import static no.nav.regoppslag.metrics.PrometheusLabels.CACHE_HIT;
import static no.nav.regoppslag.metrics.PrometheusLabels.SERVICE_CODE_TREG001;
import static no.nav.regoppslag.metrics.PrometheusMetrics.cacheCounter;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestCounter;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.dok.metaforcemal.jaxb2.gen.Besoksadresse;
import no.nav.regoppslag.consumer.norg2.OrganisasjonEnhetKontaktinformasjonV1Consumer;
import no.nav.regoppslag.consumer.norg2.support.Norg2Mapper;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.xmlenricher.ElementEnricherPlugin;
import no.nav.regoppslag.xmlenricher.util.JaxbHelper;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.Organisasjonsenhet;
import org.apache.commons.lang3.StringUtils;
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
public class NavOrgenhetBesoksadressePlugin extends JaxbHelper<Besoksadresse> implements ElementEnricherPlugin {
	Logger LOG = LoggerFactory.getLogger(NavOrgenhetBesoksadressePlugin.class);
	public static final String ELEMENT_NS = "http://nav.no/dok/pesysbrev/felles/v1/Kontaktinformasjon";
	public static final String ELEMENT_LOCALNAME = "besoksadresse";
	
	private OrganisasjonEnhetKontaktinformasjonV1Consumer norg2Consumer;
	private Norg2Mapper norg2Mapper;
	
	public NavOrgenhetBesoksadressePlugin() {
		super(Besoksadresse.class);
	}
	
	@Inject
	public NavOrgenhetBesoksadressePlugin(OrganisasjonEnhetKontaktinformasjonV1Consumer norg2Consumer, Norg2Mapper norg2Mapper) {
		super(Besoksadresse.class);
		this.norg2Consumer = norg2Consumer;
		this.norg2Mapper = norg2Mapper;
	}
	
	
	@Override
	public Node processElement(Node content, String dokumentTypeId, NamespacePrefixMapper prefixMapper) throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		if (prefixMapper != null) {
			setNamespacePrefixMapper(prefixMapper);
		}
		validateElementType(content);
		try {
			
			requestCounter.labels(SERVICE_CODE_TREG001, "plugin", "NavOrgenhetBesoksadressePlugin").inc();
			
			Besoksadresse adresse = unmarshal(content);
			
			log.info(String.format("Henter NavOrgenhet info. DokumentTypeId=%s, EnhetsId=%s", dokumentTypeId, adresse.getEnhetsId()));
			
			validateAdresse(adresse);
			
			cacheCounter.labels(HENT_ENHET_NAVN, "OrganisasjonEnhetKontaktinformasjonV1", CACHE_HIT).inc();
			Organisasjonsenhet wsEnhet = norg2Consumer.hentKontaktinformasjonForEnhet(adresse.getEnhetsId());
			
			norg2Mapper.mapBesokadresse(wsEnhet, adresse);
			
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setNamespaceAware(true);
			
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document document = builder.newDocument();
			
			Node node = marshal(adresse, document);
			Document newNode = (Document) node;
			Element documentElement = newNode.getDocumentElement();
			
			log.info(String.format("NavOrgenhet er beriket med data. DokumentTypeId=%s, EnhetsId=%s", dokumentTypeId, adresse.getEnhetsId()));
			return newNode.renameNode(documentElement, content.getNamespaceURI(), content.getLocalName());
			
		} catch (JAXBException | ParserConfigurationException e) {
			throw new RegOppslagFunctionalException("NavOrgenhetBesoksadressePlugin: Feil ved parsing av XML", e);
		}
	}
	
	private void validateAdresse(Besoksadresse adresse) throws RegOppslagFunctionalException {
		if (StringUtils.isEmpty(adresse.getEnhetsId())) {
			throw new RegOppslagFunctionalException(String.format("Feil i NavOrgenhetBesoksadressePlugin: adressedata mangler påkrevde parametere."));
		}
	}
	
	private void validateElementType(Node element) throws RegOppslagFunctionalException {
		if (!ELEMENT_NS.equals(element.getNamespaceURI())
				|| !ELEMENT_LOCALNAME.equals(element.getLocalName())) {
			throw new RegOppslagFunctionalException("Unexpected element. Expected {" + ELEMENT_NS + "}" + ELEMENT_LOCALNAME
					+ ". Found {" + element.getNamespaceURI() + "}" + element.getLocalName());
		}
	}
}
