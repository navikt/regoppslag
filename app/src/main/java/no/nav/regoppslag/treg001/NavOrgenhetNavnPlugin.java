package no.nav.regoppslag.treg001;

import static no.nav.regoppslag.consumer.norg2.OrganisasjonEnhetKontaktinformasjonV1Consumer.HENT_ENHET_NAVN;
import static no.nav.regoppslag.metrics.PrometheusLabels.CACHE_TOTAL;
import static no.nav.regoppslag.metrics.PrometheusLabels.LABEL_CACHE_COUNTER;
import static no.nav.regoppslag.metrics.PrometheusLabels.PLUGIN;
import static no.nav.regoppslag.metrics.PrometheusLabels.RECEIVED;
import static no.nav.regoppslag.metrics.PrometheusLabels.SERVICE_CODE_TREG001;
import static no.nav.regoppslag.metrics.PrometheusMetrics.getConsumerId;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestCounter;
import static no.nav.regoppslag.xmlenricher.util.ValueMapKeys.PREFIXMAPPER;
import static no.nav.regoppslag.xmlenricher.util.ValueMapKeys.SECURITYCONTEXT;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.dok.metaforcemal.jaxb2.gen.NavEnhet;
import no.nav.regoppslag.consumer.norg2.OrganisasjonEnhetKontaktinformasjonV1Consumer;
import no.nav.regoppslag.consumer.norg2.support.Norg2Mapper;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.xmlenricher.ElementEnricherPlugin;
import no.nav.regoppslag.xmlenricher.util.JaxbHelper;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.Organisasjonsenhet;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Map;

@Component
@Slf4j
public class NavOrgenhetNavnPlugin extends JaxbHelper<NavEnhet> implements ElementEnricherPlugin {
	public static final String ELEMENT_NS = "http://nav.no/dok/pesysbrev/felles/v1/Saksbehandler";
	public static final String ELEMENT_LOCALNAME = "navEnhet";
	
	private OrganisasjonEnhetKontaktinformasjonV1Consumer norg2Consumer;
	private Norg2Mapper norg2Mapper;
	
	public NavOrgenhetNavnPlugin() {
		super(NavEnhet.class);
	}
	
	@Inject
	public NavOrgenhetNavnPlugin(OrganisasjonEnhetKontaktinformasjonV1Consumer norg2Consumer, Norg2Mapper norg2Mapper) {
		super(NavEnhet.class);
		this.norg2Consumer = norg2Consumer;
		this.norg2Mapper = norg2Mapper;
	}
	
	@Override
	public Node processElement(Node content, Map<String, Object> valueMap) throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		NamespacePrefixMapper prefixMapper = (NamespacePrefixMapper) valueMap.get(PREFIXMAPPER.name());
		SecurityContext securityContext = (SecurityContext) valueMap.get(SECURITYCONTEXT.name());
		
		if (prefixMapper != null) {
			setNamespacePrefixMapper(prefixMapper);
		}
		
		SecurityContextHolder.getContext().setAuthentication(securityContext.getAuthentication());
		
		validateElementType(content);
		try {
			requestCounter.labels(SERVICE_CODE_TREG001, "NavOrgenhetNavnPlugin", PLUGIN, getConsumerId(), RECEIVED).inc();
			
			NavEnhet navEnhet = unmarshal(content);
			log.info(String.format("Henter NavOrgenhetNavn. EnhetsId=%s, ConsumerId=%s", navEnhet.getEnhetsId(), getConsumerId()));
			
			validateEnhet(navEnhet);
			
			requestCounter.labels(SERVICE_CODE_TREG001, HENT_ENHET_NAVN, LABEL_CACHE_COUNTER, getConsumerId(), CACHE_TOTAL)
					.inc();
			Organisasjonsenhet wsEnhet = norg2Consumer.hentKontaktinformasjonForEnhet(navEnhet.getEnhetsId());
			
			if (wsEnhet == null) {
				throw new RegOppslagFunctionalException(String.format("Feil i NavOrgenhetNavnPlugin:  Kunne ikke finne enhet. enhetId=%s, ConsumerId=%s", navEnhet
						.getEnhetsId(), getConsumerId()));
			}
			
			norg2Mapper.mapEnhetNavn(wsEnhet, navEnhet);
			
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setNamespaceAware(true);
			
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document document = builder.newDocument();
			
			Node node = marshal(navEnhet, document);
			Document newNode = (Document) node;
			Element documentElement = newNode.getDocumentElement();
			
			log.info(String.format("NavOrgenhetNavn er beriket med data. EnhetsId=%s, ConsumerId=%s", navEnhet.getEnhetsId(), getConsumerId()));
			return newNode.renameNode(documentElement, content.getNamespaceURI(), content.getLocalName());
			
		} catch (JAXBException | ParserConfigurationException e) {
			throw new RegOppslagFunctionalException("NavOrgenhetNavn: Feil ved parsing av XML", e);
		}
	}
	
	private void validateEnhet(NavEnhet navEnhet) throws RegOppslagFunctionalException {
		if (StringUtils.isEmpty(navEnhet.getEnhetsId())) {
			throw new RegOppslagFunctionalException(String.format("Feil i NavOrgenhetNavn: mangler enhetId."));
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
