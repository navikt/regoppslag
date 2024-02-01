package no.nav.regoppslag.treg001;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.navfelles.NavAnsatt;
import no.nav.regoppslag.consumer.azure.MsGraphConsumer;
import no.nav.regoppslag.consumer.ldap.support.SaksbehandlerMapper;
import no.nav.regoppslag.exceptions.MarshallerException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;
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

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
@Component
public class SaksbehandlerPlugin extends JaxbHelper<NavAnsatt> implements ElementEnricherPlugin {

	public static final String ELEMENT_NS = "http://nav.no/dok/brevdata/felles/v1/NAVFelles";
	public static final String ELEMENT_LOCALNAME = "navAnsatt";
	public static final String PLUGIN_NAME = "SaksbehandlerPlugin";

	public SaksbehandlerPlugin() {
		super(NavAnsatt.class);
	}

	@Autowired
	private MsGraphConsumer msGraphConsumer;

	@Autowired
	private SaksbehandlerMapper saksbehandlerMapper;

	@Override
	public Node processElement(Node content, Map<String, Object> valueMap) {
		validateElementType(content);

		try {
			NavAnsatt navAnsatt = unmarshal(content);

			log.info("Henter saksbehandler info. AnsattId={}", navAnsatt.getAnsattId());

			//Skal elementet berikes?
			if (navAnsatt.isBerik()) {
				validateSaksbehandler(navAnsatt);
				String saksbehandlerNavn = msGraphConsumer.hentFulltNavn(navAnsatt.getAnsattId());
				navAnsatt = saksbehandlerMapper.map(saksbehandlerNavn, navAnsatt);
			}

			Document newNode = convertObjectToDocument(navAnsatt);
			Element documentElement = newNode.getDocumentElement();

			log.info("Saksbehandler er beriket med data.  AnsattId={}", navAnsatt.getAnsattId());

			return newNode.renameNode(documentElement, content.getNamespaceURI(), content.getLocalName());
		} catch (ParserConfigurationException | MarshallerException e) {
			throw new RegOppslagTechnicalException(format("Feil i %s med feilmelding=%s", PLUGIN_NAME, e.getMessage()), e);
		} finally {
			clearSecurityContext();
		}
	}

	private void validateSaksbehandler(NavAnsatt navAnsatt) {
		if (StringUtils.isEmpty(navAnsatt.getAnsattId())) {
			throw new RegoppslagIllegalArgumentException(format("Feil i %s med feilmelding=Saksbehandlerdata mangler ansattId", PLUGIN_NAME), BAD_REQUEST);
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
