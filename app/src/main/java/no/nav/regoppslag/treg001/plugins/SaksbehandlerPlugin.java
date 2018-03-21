package no.nav.regoppslag.treg001.plugins;

import static no.nav.regoppslag.consumer.ldap.LdapAdeoUserLookup.HENT_FULLT_NAVN;
import static no.nav.regoppslag.metrics.PrometheusLabels.SERVICE_CODE_TREG001;
import static no.nav.regoppslag.metrics.PrometheusMetrics.cacheCounter;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestCounter;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.dok.metaforcemal.jaxb2.gen.NavAnsatt;
import no.nav.regoppslag.consumer.ldap.LdapAdeoUserLookup;
import no.nav.regoppslag.consumer.ldap.support.SaksbehandlerMapper;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.xmlenricher.ElementEnricherPlugin;
import no.nav.regoppslag.xmlenricher.exceptions.InvalidElementException;
import no.nav.regoppslag.xmlenricher.util.JaxbHelper;
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

@Slf4j
@Component
@Scope("prototype")
public class SaksbehandlerPlugin extends JaxbHelper<NavAnsatt> implements ElementEnricherPlugin {

	public SaksbehandlerPlugin() {
		super(NavAnsatt.class);
	}

	@Inject
	private LdapAdeoUserLookup ldapAdeoUserLookup;

	@Inject
	private SaksbehandlerMapper saksbehandlerMapper;

	@Override
	public Node processElement(Node content, String dokumentTypeId, NamespacePrefixMapper prefixMapper) throws RegOppslagFunctionalException, RegOppslagTechnicalException, InvalidElementException {
		if (prefixMapper != null) {
			setNamespacePrefixMapper(prefixMapper);
		}
		try {
			log.info("Henter saksbehandler info");
			requestCounter.labels(SERVICE_CODE_TREG001, "SaksbehandlerPlugin");
			
			NavAnsatt navAnsatt = unmarshal(content);
			
			validateSaksbehandler(navAnsatt);
			
			
			cacheCounter.labels("hentFulltNavn:cacheTry", HENT_FULLT_NAVN).inc();
			String saksbehandlerNavn = ldapAdeoUserLookup.hentFulltNavn(navAnsatt.getAnsattId());
			
 			if (saksbehandlerNavn==null){
				throw new RegOppslagFunctionalException(String.format("Feil i SaksbehandlerPlugin: Fant ikke saksbehandlernavn. AnsattId=%s",navAnsatt.getAnsattId()));
			}
			navAnsatt = saksbehandlerMapper.map(saksbehandlerNavn, navAnsatt);
			
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setNamespaceAware(true);

			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document document = builder.newDocument();

			Node node = marshal(navAnsatt, document);
			Document newNode = (Document) node;
			Element documentElement = newNode.getDocumentElement();
			
			log.info("Saksbehandler er beriket med data");
			
			return newNode.renameNode(documentElement, content.getNamespaceURI(), content.getLocalName());
		} catch (JAXBException | ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void validateSaksbehandler(NavAnsatt navAnsatt) throws RegOppslagFunctionalException {
		if (navAnsatt.getAnsattId()==null){
			throw new RegOppslagFunctionalException(String.format("Feil i SaksbehandlerPlugin: Saksbehandlerdata mangler ansattId"));
		}
	}
}
