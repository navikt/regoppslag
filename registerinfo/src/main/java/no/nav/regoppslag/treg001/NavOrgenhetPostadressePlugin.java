package no.nav.regoppslag.treg001;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.navfelles.Postadresse;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;
import java.util.Map;

import static no.nav.regoppslag.metrics.MetricLabels.SERVICE_CODE_TREG001;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Component
@Slf4j
public class NavOrgenhetPostadressePlugin extends JaxbHelper<Postadresse> implements ElementEnricherPlugin {

	public static final String ELEMENT_NS = "http://nav.no/dok/brevdata/felles/v1/NAVFelles";
	public static final String ELEMENT_LOCALNAME_POST = "postadresse";
	public static final String ELEMENT_LOCALNAME_RETUR = "returadresse";
	public static final String UGYLDIG_INPUT = "NavOrgenhetPostAdressePlugin - Ugyldig input";
	public static final String PLUGIN_NAME = "NavOrgenhetPostadressePlugin";

	private OrganisasjonEnhetKontaktinformasjonV1Consumer norg2Consumer;
	private Norg2Mapper norg2Mapper;
	private MicrometerMetrics metrics;

	public NavOrgenhetPostadressePlugin() {
		super(Postadresse.class);
	}

	@Autowired
	public NavOrgenhetPostadressePlugin(OrganisasjonEnhetKontaktinformasjonV1Consumer norg2Consumer, Norg2Mapper norg2Mapper,
										MicrometerMetrics metrics) {
		super(Postadresse.class);
		this.norg2Consumer = norg2Consumer;
		this.norg2Mapper = norg2Mapper;
		this.metrics = metrics;
	}

	@Override
	public Node processElement(Node content, Map<String, Object> valueMap, String tema) {
		metrics.pluginReceived(SERVICE_CODE_TREG001, PLUGIN_NAME);

		validateElementType(content);

		try {
			Postadresse adresse = unmarshal(content);
			log.info(String.format("Henter NavOrgenhet info. EnhetsId=%s", adresse.getEnhetsId()));

			//Skal elementet berikes?
			if (adresse.isBerik()) {
				validateAdresse(adresse);
				Organisasjonsenhet wsEnhet = norg2Consumer.hentKontaktinformasjonForEnhet(adresse.getEnhetsId());
				norg2Mapper.mapPostadresse(wsEnhet, adresse);
			}

			Document newNode = convertObjectToDocument(adresse);
			Element documentElement = newNode.getDocumentElement();

			log.info(String.format("NavOrgenhet er beriket med data. EnhetsId=%s", adresse.getEnhetsId()));

			return newNode.renameNode(documentElement, content.getNamespaceURI(), content.getLocalName());

		} catch (ParserConfigurationException | MarshallerException e) {
			throw new RegOppslagTechnicalException(String.format("Feil i %s: %s", PLUGIN_NAME, e.getMessage()), e, UGYLDIG_INPUT);
		}
	}

	private void validateAdresse(Postadresse adresse)  {
		if (StringUtils.isEmpty(adresse.getEnhetsId())) {
			throw new RegoppslagIllegalArgumentException(String.format("Feil i %s: Mangler enhetId.", PLUGIN_NAME), BAD_REQUEST);
		}
	}

	private void validateElementType(Node element)  {
		if (!(ELEMENT_LOCALNAME_POST.equals(element.getLocalName()) || ELEMENT_LOCALNAME_RETUR.equals(element.getLocalName()))) {
			throw new RegoppslagIllegalArgumentException("Unexpected element. Expected " + ELEMENT_LOCALNAME_POST
					+ " or " + ELEMENT_LOCALNAME_RETUR + ". Found {" + element.getNamespaceURI() + "}" + element
					.getLocalName(), BAD_REQUEST);
		}
	}
}