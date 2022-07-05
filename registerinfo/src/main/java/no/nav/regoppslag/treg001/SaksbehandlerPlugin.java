package no.nav.regoppslag.treg001;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.navfelles.NavAnsatt;
import no.nav.regoppslag.consumer.ldap.LdapAdeoUserLookup;
import no.nav.regoppslag.consumer.ldap.support.SaksbehandlerMapper;
import no.nav.regoppslag.exceptions.MarshallerException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.regoppslag.treg001.xmlenricher.ElementEnricherPlugin;
import no.nav.regoppslag.treg001.xmlenricher.util.JaxbHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;
import java.util.Map;

import static no.nav.regoppslag.metrics.MetricLabels.SERVICE_CODE_TREG001;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

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

	@Autowired
	private LdapAdeoUserLookup ldapAdeoUserLookup;

	@Autowired
	private SaksbehandlerMapper saksbehandlerMapper;

	@Autowired
	private MicrometerMetrics metrics;

	@Override
	public Node processElement(Node content, Map<String, Object> valueMap, String tema) {
		metrics.pluginReceived(SERVICE_CODE_TREG001, PLUGIN_NAME);

		validateElementType(content);

		try {
			NavAnsatt navAnsatt = unmarshal(content);

			log.info(String.format("Henter saksbehandler info. AnsattId=%s", navAnsatt.getAnsattId()));

			//Skal elementet berikes?
			if (navAnsatt.isBerik()) {
				validateSaksbehandler(navAnsatt);
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

	private void validateSaksbehandler(NavAnsatt navAnsatt) {
		if (StringUtils.isEmpty(navAnsatt.getAnsattId())) {
			throw new RegoppslagIllegalArgumentException(String.format("Feil i %s: Saksbehandlerdata mangler ansattId", PLUGIN_NAME), BAD_REQUEST);
		}
	}

	private void validateElementType(Node element) {
		if (!ELEMENT_NS.equals(element.getNamespaceURI())
				|| !ELEMENT_LOCALNAME.equals(element.getLocalName())) {
			throw new RegoppslagIllegalArgumentException("Unexpected element. Expected {" + ELEMENT_NS + "}" + ELEMENT_LOCALNAME
					+ ". Found {" + element.getNamespaceURI() + "}" + element.getLocalName(), BAD_REQUEST);
		}
	}

}
