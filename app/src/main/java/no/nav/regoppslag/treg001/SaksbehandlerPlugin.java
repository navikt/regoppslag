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

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.navfelles.NavAnsatt;
import no.nav.regoppslag.consumer.ldap.LdapAdeoUserLookup;
import no.nav.regoppslag.consumer.ldap.support.SaksbehandlerMapper;
import no.nav.regoppslag.exceptions.MarshallerException;
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
import javax.xml.parsers.ParserConfigurationException;
import java.util.Map;

@Slf4j
@Component
public class SaksbehandlerPlugin extends JaxbHelper<NavAnsatt> implements ElementEnricherPlugin {
	public static final String ELEMENT_NS = "http://nav.no/dok/brevdata/felles/v1/NAVFelles";
	public static final String ELEMENT_LOCALNAME = "navAnsatt";
	public static final String UGYLDIG_INPUT = "SaksbehandlerPlugin - Ugyldig input";
	public static final String PLUGIN_NAME = "SaksbehandlerPlugin";

	public SaksbehandlerPlugin() {
		super(NavAnsatt.class);
	}

	@Inject
	private LdapAdeoUserLookup ldapAdeoUserLookup;

	@Inject
	private SaksbehandlerMapper saksbehandlerMapper;

	@Override
	public Node processElement(Node content, Map<String, Object> valueMap) throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		requestCounter.labels(SERVICE_CODE_TREG001, PLUGIN_NAME, PLUGIN, getConsumerId(), RECEIVED).inc();

		validateElementType(content);

		try {
			NavAnsatt navAnsatt = unmarshal(content);

			log.info(String.format("Henter saksbehandler info. AnsattId=%s", navAnsatt.getAnsattId()));

			//Skal elementet berikes?
			if (navAnsatt.isBerik()) {
				validateSaksbehandler(navAnsatt);

				requestCounter.labels(SERVICE_CODE_TREG001, HENT_FULLT_NAVN, CACHE_COUNTER, getConsumerId(), CACHE_TOTAL)
						.inc();
				String saksbehandlerNavn = ldapAdeoUserLookup.hentFulltNavn(navAnsatt.getAnsattId());

				navAnsatt = saksbehandlerMapper.map(saksbehandlerNavn, navAnsatt);
			}

			Document newNode = convertObjectToDocument(navAnsatt);
			Element documentElement = newNode.getDocumentElement();

			log.info(String.format("Saksbehandler er beriket med data.  AnsattId=%s", navAnsatt.getAnsattId()));

			return newNode.renameNode(documentElement, content.getNamespaceURI(), content.getLocalName());
		} catch (ParserConfigurationException | MarshallerException e) {
			throw new RegOppslagTechnicalException(String.format("Feil i %s: %s", PLUGIN_NAME, e.getMessage()), e, UGYLDIG_INPUT);
		}
	}

	private void validateSaksbehandler(NavAnsatt navAnsatt) throws RegOppslagFunctionalException {
		if (StringUtils.isEmpty(navAnsatt.getAnsattId())) {
			throw new RegOppslagFunctionalException(String.format("Feil i %s: Saksbehandlerdata mangler ansattId", PLUGIN_NAME), UGYLDIG_INPUT);
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
