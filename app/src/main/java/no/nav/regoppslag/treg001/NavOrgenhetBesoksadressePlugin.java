package no.nav.regoppslag.treg001;

import static no.nav.regoppslag.consumer.norg2.OrganisasjonEnhetKontaktinformasjonV1Consumer.HENT_ENHET_NAVN;
import static no.nav.regoppslag.consumer.norg2.OrganisasjonEnhetKontaktinformasjonV1Consumer.KUNNE_IKKE_FINNE_ENHET;
import static no.nav.regoppslag.metrics.PrometheusLabels.CACHE_COUNTER;
import static no.nav.regoppslag.metrics.PrometheusLabels.CACHE_TOTAL;
import static no.nav.regoppslag.metrics.PrometheusLabels.PLUGIN;
import static no.nav.regoppslag.metrics.PrometheusLabels.RECEIVED;
import static no.nav.regoppslag.metrics.PrometheusLabels.SERVICE_CODE_TREG001;
import static no.nav.regoppslag.metrics.PrometheusMetrics.getConsumerId;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestCounter;
import static no.nav.regoppslag.xmlenricher.util.ValueMapKeys.DOKUMENTTYPEID;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.navfelles.Besoksadresse;
import no.nav.regoppslag.consumer.norg2.OrganisasjonEnhetKontaktinformasjonV1Consumer;
import no.nav.regoppslag.consumer.norg2.support.Norg2Mapper;
import no.nav.regoppslag.exceptions.MarshallerException;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.xmlenricher.ElementEnricherPlugin;
import no.nav.regoppslag.xmlenricher.util.JaxbHelper;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.Organisasjonsenhet;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Map;

@Component
@Slf4j
public class NavOrgenhetBesoksadressePlugin extends JaxbHelper<Besoksadresse> implements ElementEnricherPlugin {

	public static final String ELEMENT_LOCALNAME = "besoksadresse";
	public static final String UGYLDIG_INPUT = "NavOrgenhetBesokAdressePlugin - Ugyldig input";
	public static final String PLUGIN_NAME = "NavOrgenhetBesoksadressePlugin";

	private OrganisasjonEnhetKontaktinformasjonV1Consumer norg2Consumer;
	private Norg2Mapper norg2Mapper;

	public NavOrgenhetBesoksadressePlugin() {
		super(Besoksadresse.class);
	}

	@Inject
	public NavOrgenhetBesoksadressePlugin(OrganisasjonEnhetKontaktinformasjonV1Consumer norg2Consumer, Norg2Mapper norg2Mapper) {
		super(Besoksadresse.class);
		this.norg2Consumer = norg2Consumer;
		this.norg2Mapper = norg2Mapper;
	}

	@Override
	public Node processElement(Node content, Map<String, Object> valueMap) throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		String dokumenttypeId = (String) valueMap.get(DOKUMENTTYPEID.name());

		requestCounter.labels(SERVICE_CODE_TREG001, PLUGIN_NAME, PLUGIN, getConsumerId(), RECEIVED)
				.inc();

		validateElementType(content);

		try {
			Besoksadresse adresse = unmarshal(content);

			log.info(String.format("Henter NavOrgenhet info. DokumentTypeId=%s, EnhetsId=%s", dokumenttypeId, adresse
					.getEnhetsId()));

			//Skal elementet berikes?
			if (adresse.isBerik()) {
				validateAdresse(adresse);

				requestCounter.labels(SERVICE_CODE_TREG001, HENT_ENHET_NAVN, CACHE_COUNTER, getConsumerId(), CACHE_TOTAL)
						.inc();
				Organisasjonsenhet wsEnhet = norg2Consumer.hentKontaktinformasjonForEnhet(adresse.getEnhetsId());

				if (wsEnhet == null) {
					throw new RegOppslagFunctionalException(String.format("Feil i %s:  Kunne ikke finne enhet med enhetsId=%s", PLUGIN_NAME, adresse
							.getEnhetsId()), PLUGIN_NAME + " - " + KUNNE_IKKE_FINNE_ENHET);
				}

				norg2Mapper.mapBesokadresse(wsEnhet, adresse);
			}

			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setNamespaceAware(true);

			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document document = builder.newDocument();

			Node node = marshal(adresse, document);
			Document newNode = (Document) node;
			Element documentElement = newNode.getDocumentElement();

			log.info(String.format("NavOrgenhet er beriket med data. DokumentTypeId=%s, EnhetsId=%s", dokumenttypeId, adresse
					.getEnhetsId()));
			return newNode.renameNode(documentElement, content.getNamespaceURI(), content.getLocalName());

		} catch (ParserConfigurationException | MarshallerException e) {
			throw new RegOppslagTechnicalException(String.format("Feil i %s: %s", PLUGIN_NAME, e.getMessage()), e, UGYLDIG_INPUT);
		}
	}

	private void validateAdresse(Besoksadresse adresse) throws RegOppslagFunctionalException {
		if (StringUtils.isEmpty(adresse.getEnhetsId())) {
			throw new RegOppslagFunctionalException(String.format("Feil i %s: Mangler enhetsId.", PLUGIN_NAME), UGYLDIG_INPUT);
		}
	}

	private void validateElementType(Node element) throws RegOppslagFunctionalException {
		if (!ELEMENT_LOCALNAME.equals(element.getLocalName())) {
			throw new RegOppslagFunctionalException("Unexpected element. Expected " + ELEMENT_LOCALNAME
					+ ". Found {" + element.getNamespaceURI() + "}" + element.getLocalName(), UGYLDIG_INPUT);
		}
	}
}
