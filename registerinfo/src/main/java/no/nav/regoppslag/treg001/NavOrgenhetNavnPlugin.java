package no.nav.regoppslag.treg001;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.navfelles.NavEnhet;
import no.nav.regoppslag.consumer.norg2.OrganisasjonsenhetConsumer;
import no.nav.regoppslag.consumer.norg2.support.Norg2Mapper;
import no.nav.regoppslag.consumer.norg2.to.EnhetNavn;
import no.nav.regoppslag.exceptions.MarshallerException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;
import no.nav.regoppslag.treg001.xmlenricher.ElementEnricherPlugin;
import no.nav.regoppslag.treg001.xmlenricher.util.JaxbHelper;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;
import java.util.Map;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Component
@Slf4j
public class NavOrgenhetNavnPlugin extends JaxbHelper<NavEnhet> implements ElementEnricherPlugin {

	public static final String ELEMENT_LOCALNAME = "navEnhet";
	public static final String ELEMENT_LOCALNAME_BEHANDLENDEENHET = "behandlendeEnhet";
	public static final String PLUGIN_NAME = "NavOrgenhetNavnPlugin";

	private final OrganisasjonsenhetConsumer norg2Consumer;
	private final Norg2Mapper norg2Mapper;

	public NavOrgenhetNavnPlugin(OrganisasjonsenhetConsumer norg2Consumer,
								 Norg2Mapper norg2Mapper) {
		super(NavEnhet.class);
		this.norg2Consumer = norg2Consumer;
		this.norg2Mapper = norg2Mapper;
	}
	
	@Override
	public Node processElement(Node content, Map<String, Object> valueMap, String tema) {
		validateElementType(content);
		
		try {
			NavEnhet navEnhet = unmarshal(content);

			log.info("Henter NavOrgenhetNavn. EnhetsId={}", navEnhet.getEnhetsId());

			//Skal elementet berikes?
			if (navEnhet.isBerik()) {
				validateEnhet(navEnhet);
				EnhetNavn rsEnhetNavn = norg2Consumer.hentEnhetNavn(navEnhet.getEnhetsId());
				norg2Mapper.mapEnhetNavn(rsEnhetNavn, navEnhet);
			} else {
				log.info("TREG001 NavOrgEnhetPlugin: element-berik={}. Hopper over beriking av element={} med enhetsId={}.", navEnhet.isBerik(), content.getLocalName(), navEnhet.getEnhetsId());
				return content;
			}

			Document newNode = convertObjectToDocument(navEnhet);
			Element documentElement = newNode.getDocumentElement();

			log.info("NavOrgenhetNavn er beriket med data. EnhetsId={}", navEnhet.getEnhetsId());
			return newNode.renameNode(documentElement, content.getNamespaceURI(), content.getLocalName());

		} catch (ParserConfigurationException | MarshallerException e) {
			throw new RegOppslagTechnicalException(format("Feil i %s med feilmelding=%s", PLUGIN_NAME, e.getMessage()), e);
		} finally {
			clearSecurityContext();
		}
	}
	
	private void validateEnhet(NavEnhet navEnhet)  {
		if (isEmpty(navEnhet.getEnhetsId())) {
			throw new RegoppslagIllegalArgumentException(format("Feil i %s med feilmelding=EnhetdId mangler.", PLUGIN_NAME), BAD_REQUEST);
		}
	}
	
	private void validateElementType(Node element)  {
		if (!(ELEMENT_LOCALNAME.equals(element.getLocalName()) || ELEMENT_LOCALNAME_BEHANDLENDEENHET.equals(element.getLocalName()))) {
			throw new RegoppslagIllegalArgumentException("Unexpected element. Expected " + ELEMENT_LOCALNAME + ". Found " + element.getLocalName(), BAD_REQUEST);
		}
	}
}
