package no.nav.regoppslag.treg001;

import static no.nav.regoppslag.consumer.ldap.LdapAdeoUserLookup.BRUKER_IKKE_FUNNET;
import static no.nav.regoppslag.consumer.ldap.LdapAdeoUserLookup.HENT_FULLT_NAVN;
import static no.nav.regoppslag.metrics.PrometheusLabels.CACHE_COUNTER;
import static no.nav.regoppslag.metrics.PrometheusLabels.CACHE_TOTAL;
import static no.nav.regoppslag.metrics.PrometheusLabels.PLUGIN;
import static no.nav.regoppslag.metrics.PrometheusLabels.RECEIVED;
import static no.nav.regoppslag.metrics.PrometheusLabels.SERVICE_CODE_TREG001;
import static no.nav.regoppslag.metrics.PrometheusMetrics.getConsumerId;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestCounter;
import static no.nav.regoppslag.xmlenricher.util.ValueMapKeys.PREFIXMAPPER;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.navfelles.NavAnsatt;
import no.nav.regoppslag.consumer.ldap.LdapAdeoUserLookup;
import no.nav.regoppslag.consumer.ldap.support.SaksbehandlerMapper;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.xmlenricher.ElementEnricherPlugin;
import no.nav.regoppslag.xmlenricher.util.JaxbHelper;
import org.apache.commons.lang3.StringUtils;
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

@Slf4j
@Component
public class SaksbehandlerPlugin extends JaxbHelper<NavAnsatt> implements ElementEnricherPlugin {
	public static final String ELEMENT_NS = "http://nav.no/dok/brevdata/felles/v1/NAVFelles";
	public static final String ELEMENT_LOCALNAME = "navAnsatt";
	public static final String UGYLDIG_INPUT = "SaksbehandlerPlugin - Ugyldig input";
	
	public SaksbehandlerPlugin() {
		super(NavAnsatt.class);
	}
	
	@Inject
	private LdapAdeoUserLookup ldapAdeoUserLookup;
	
	@Inject
	private SaksbehandlerMapper saksbehandlerMapper;
	
	@Override
	public Node processElement(Node content, Map<String, Object> valueMap) throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		NamespacePrefixMapper prefixMapper = (NamespacePrefixMapper) valueMap.get(PREFIXMAPPER.name());
		
		requestCounter.labels(SERVICE_CODE_TREG001, "SaksbehandlerPlugin", PLUGIN, getConsumerId(), RECEIVED).inc();
		
		if (prefixMapper != null) {
			setNamespacePrefixMapper(prefixMapper);
		}
		
		validateElementType(content);
		
		try {
			NavAnsatt navAnsatt = unmarshal(content);
			
			log.info(String.format("Henter saksbehandler info. AnsattId=%s", navAnsatt.getAnsattId()));
			
			validateSaksbehandler(navAnsatt);
			
			requestCounter.labels(SERVICE_CODE_TREG001, HENT_FULLT_NAVN, CACHE_COUNTER, getConsumerId(), CACHE_TOTAL)
					.inc();
			String saksbehandlerNavn = ldapAdeoUserLookup.hentFulltNavn(navAnsatt.getAnsattId());
			
			if (saksbehandlerNavn == null) {
				//Dette bør ikke skje
				throw new RegOppslagFunctionalException(String.format("Feil i SaksbehandlerPlugin: Fant ikke saksbehandlernavn. AnsattId=%s", navAnsatt
						.getAnsattId()), "SaksbehandlerPlugin - " + BRUKER_IKKE_FUNNET);
			}
			
			navAnsatt = saksbehandlerMapper.map(saksbehandlerNavn, navAnsatt);
			
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setNamespaceAware(true);
			
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document document = builder.newDocument();
			
			Node node = marshal(navAnsatt, document);
			Document newNode = (Document) node;
			Element documentElement = newNode.getDocumentElement();
			
			log.info(String.format("Saksbehandler er beriket med data.  AnsattId=%s", navAnsatt.getAnsattId()));
			
			return newNode.renameNode(documentElement, content.getNamespaceURI(), content.getLocalName());
		} catch (JAXBException | ParserConfigurationException e) {
			throw new RegOppslagFunctionalException("SaksbehandlerPlugin: Feil ved parsing av XML", e, UGYLDIG_INPUT);
		}
	}
	
	private void validateSaksbehandler(NavAnsatt navAnsatt) throws RegOppslagFunctionalException {
		if (StringUtils.isEmpty(navAnsatt.getAnsattId())) {
			throw new RegOppslagFunctionalException("Feil i SaksbehandlerPlugin: Saksbehandlerdata mangler ansattId", UGYLDIG_INPUT);
		}
	}
	
	private void validateElementType(Node element) throws RegOppslagFunctionalException {
		if (!ELEMENT_NS.equals(element.getNamespaceURI())
				|| !ELEMENT_LOCALNAME.equals(element.getLocalName())) {
			throw new RegOppslagFunctionalException("Unexpected element. Expected {" + ELEMENT_NS + "}" + ELEMENT_LOCALNAME
					+ ". Found {" + element.getNamespaceURI() + "}" + element.getLocalName(), UGYLDIG_INPUT);
		}
	}
	
}
