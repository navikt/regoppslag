package no.nav.regoppslag.treg001;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.navfelles.NavEnhet;
import no.nav.regoppslag.consumer.norg2.OrganisasjonEnhetKontaktinformasjonV1Consumer;
import no.nav.regoppslag.consumer.norg2.support.Norg2Mapper;
import no.nav.regoppslag.exceptions.MarshallerException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.regoppslag.treg001.xmlenricher.ElementEnricherPlugin;
import no.nav.regoppslag.treg001.xmlenricher.util.JaxbHelper;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.Organisasjonsenhet;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Map;

import static java.lang.String.*;
import static java.lang.String.format;
import static no.nav.regoppslag.metrics.MetricLabels.SERVICE_CODE_TREG001;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Component
@Slf4j
public class NavOrgenhetNavnPlugin extends JaxbHelper<NavEnhet> implements ElementEnricherPlugin {
	public static final String ELEMENT_NS = "http://nav.no/dok/brevdata/felles/v1/NAVFelles";
	public static final String ELEMENT_LOCALNAME = "navEnhet";
	public static final String ELEMENT_LOCALNAME_BEHANDLENDEENHET = "behandlendeEnhet";
	public static final String UGYLDIG_INPUT = "NavOrgenhetNavnPlugin - Ugyldig input";
	public static final String PLUGIN_NAME = "NavOrgenhetNavnPlugin";

	private OrganisasjonEnhetKontaktinformasjonV1Consumer norg2Consumer;
	private Norg2Mapper norg2Mapper;
	private MicrometerMetrics metrics;

	public NavOrgenhetNavnPlugin() {
		super(NavEnhet.class);
	}
	
	@Inject
	public NavOrgenhetNavnPlugin(OrganisasjonEnhetKontaktinformasjonV1Consumer norg2Consumer, Norg2Mapper norg2Mapper,
								 MicrometerMetrics metrics) {
		super(NavEnhet.class);
		this.norg2Consumer = norg2Consumer;
		this.norg2Mapper = norg2Mapper;
		this.metrics = metrics;
	}
	
	@Override
	public Node processElement(Node content, Map<String, Object> valueMap, String tema) {

		metrics.pluginReceived(SERVICE_CODE_TREG001, PLUGIN_NAME);

		validateElementType(content);
		
		try {
			NavEnhet navEnhet = unmarshal(content);

			log.info(format("Henter NavOrgenhetNavn. EnhetsId=%s", navEnhet.getEnhetsId()));

			//Skal elementet berikes?
			if (navEnhet.isBerik()) {
				validateEnhet(navEnhet);
				Organisasjonsenhet wsEnhet = norg2Consumer.hentKontaktinformasjonForEnhet(navEnhet.getEnhetsId());
				norg2Mapper.mapEnhetNavn(wsEnhet, navEnhet);
			} else {
				log.info(format("TREG001 NavOrgEnhetPlugin: element-berik=%s. Hopper over beriking av element=%s med enhetsId=%s.", navEnhet.isBerik(), content.getLocalName(), navEnhet.getEnhetsId()));
				return content;
			}

			Document newNode = convertObjectToDocument(navEnhet);
			Element documentElement = newNode.getDocumentElement();

			log.info(format("NavOrgenhetNavn er beriket med data. EnhetsId=%s", navEnhet.getEnhetsId()));
			return newNode.renameNode(documentElement, content.getNamespaceURI(), content.getLocalName());

		} catch (ParserConfigurationException | MarshallerException e) {
			throw new RegOppslagTechnicalException(format("Feil i %s: %s", PLUGIN_NAME, e.getMessage()), e, UGYLDIG_INPUT);
		}
	}
	
	private void validateEnhet(NavEnhet navEnhet)  {
		if (StringUtils.isEmpty(navEnhet.getEnhetsId())) {
			throw new RegoppslagIllegalArgumentException(format("Feil i %s: Mangler enhetdId.", PLUGIN_NAME), BAD_REQUEST);
		}
	}
	
	private void validateElementType(Node element)  {
		if (!(ELEMENT_LOCALNAME.equals(element.getLocalName()) || ELEMENT_LOCALNAME_BEHANDLENDEENHET.equals(element.getLocalName()))) {
			throw new RegoppslagIllegalArgumentException("Unexpected element. Expected " + ELEMENT_LOCALNAME
					+ ". Found " + element.getLocalName(), BAD_REQUEST);
		}
	}
}
