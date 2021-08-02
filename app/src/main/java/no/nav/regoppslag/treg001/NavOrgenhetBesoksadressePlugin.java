package no.nav.regoppslag.treg001;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.navfelles.Besoksadresse;
import no.nav.regoppslag.consumer.norg2.OrganisasjonEnhetKontaktinformasjonV1Consumer;
import no.nav.regoppslag.consumer.norg2.support.Norg2Mapper;
import no.nav.regoppslag.exceptions.MarshallerException;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.regoppslag.xmlenricher.ElementEnricherPlugin;
import no.nav.regoppslag.xmlenricher.util.JaxbHelper;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.Organisasjonsenhet;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Map;

import static java.lang.String.format;
import static no.nav.regoppslag.metrics.MetricLabels.SERVICE_CODE_TREG001;
import static no.nav.regoppslag.xmlenricher.util.ValueMapKeys.DOKUMENTTYPEID;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Component
@Slf4j
public class NavOrgenhetBesoksadressePlugin extends JaxbHelper<Besoksadresse> implements ElementEnricherPlugin {

	public static final String ELEMENT_LOCALNAME = "besoksadresse";
	public static final String UGYLDIG_INPUT = "NavOrgenhetBesokAdressePlugin - Ugyldig input";
	public static final String PLUGIN_NAME = "NavOrgenhetBesoksadressePlugin";

	private OrganisasjonEnhetKontaktinformasjonV1Consumer norg2Consumer;
	private Norg2Mapper norg2Mapper;
	private MicrometerMetrics metrics;

	public NavOrgenhetBesoksadressePlugin() {
		super(Besoksadresse.class);
	}

	@Inject
	public NavOrgenhetBesoksadressePlugin(OrganisasjonEnhetKontaktinformasjonV1Consumer norg2Consumer, Norg2Mapper norg2Mapper,
										  MicrometerMetrics metrics) {
		super(Besoksadresse.class);
		this.norg2Consumer = norg2Consumer;
		this.norg2Mapper = norg2Mapper;
		this.metrics = metrics;
	}

	@Override
	public Node processElement(Node content, Map<String, Object> valueMap, String tema) throws RegOppslagFunctionalException {
		String dokumenttypeId = (String) valueMap.get(DOKUMENTTYPEID.name());

		metrics.pluginReceived(SERVICE_CODE_TREG001, PLUGIN_NAME);

		validateElementType(content);

		try {
			Besoksadresse adresse = unmarshal(content);

			log.info(format("Henter NavOrgenhet info. DokumentTypeId=%s, EnhetsId=%s", dokumenttypeId, adresse
					.getEnhetsId()));

			//Skal elementet berikes?
			if (adresse.isBerik()) {
				validateAdresse(adresse);
				Organisasjonsenhet wsEnhet = norg2Consumer.hentKontaktinformasjonForEnhet(adresse.getEnhetsId());
				norg2Mapper.mapBesokadresse(wsEnhet, adresse);
			}

			Document newNode = convertObjectToDocument(adresse);
			Element documentElement = newNode.getDocumentElement();

			log.info(format("NavOrgenhet er beriket med data. DokumentTypeId=%s, EnhetsId=%s", dokumenttypeId, adresse
					.getEnhetsId()));
			return newNode.renameNode(documentElement, content.getNamespaceURI(), content.getLocalName());

		} catch (ParserConfigurationException | MarshallerException e) {
			throw new RegOppslagTechnicalException(format("Feil i %s: %s", PLUGIN_NAME, e.getMessage()), e, UGYLDIG_INPUT);
		}
	}

	private void validateAdresse(Besoksadresse adresse) throws RegOppslagFunctionalException {
		if (StringUtils.isEmpty(adresse.getEnhetsId())) {
			throw new RegoppslagIllegalArgumentException(format("Feil i %s: Mangler enhetsId. %s", PLUGIN_NAME, UGYLDIG_INPUT), BAD_REQUEST);
		}
	}

	private void validateElementType(Node element) throws RegOppslagFunctionalException {
		if (!ELEMENT_LOCALNAME.equals(element.getLocalName())) {
			throw new RegoppslagIllegalArgumentException("Unexpected element. Expected " + ELEMENT_LOCALNAME
					+ ". Found {" + element.getNamespaceURI() + "}" + element.getLocalName() + UGYLDIG_INPUT, BAD_REQUEST);
		}
	}
}
