package no.nav.regoppslag.treg001.plugins;

import static no.nav.regoppslag.metrics.PrometheusLabels.SERVICE_CODE_TREG001;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestCounter;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.metaforcemal.jaxb2.gen.Postadresse;
import no.nav.regoppslag.consumer.norg2.OrganisasjonEnhetKontaktinformasjonV1Consumer;
import no.nav.regoppslag.consumer.norg2.support.Norg2Mapper;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.xmlenricher.ElementEnricherPlugin;
import no.nav.regoppslag.xmlenricher.exceptions.InvalidElementException;
import no.nav.regoppslag.xmlenricher.util.JaxbHelper;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.Organisasjonsenhet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Component
@Scope("prototype")
@Slf4j
public class NavOrgenhetPostadressePlugin extends JaxbHelper<Postadresse> implements ElementEnricherPlugin {
	Logger LOG = LoggerFactory.getLogger(NavOrgenhetPostadressePlugin.class);

	private OrganisasjonEnhetKontaktinformasjonV1Consumer norg2Consumer;
	private Norg2Mapper norg2Mapper;

	public NavOrgenhetPostadressePlugin() {
		super(Postadresse.class);
	}

	@Inject
	public NavOrgenhetPostadressePlugin(OrganisasjonEnhetKontaktinformasjonV1Consumer norg2Consumer, Norg2Mapper norg2Mapper) {
		super(Postadresse.class);
		this.norg2Consumer = norg2Consumer;
		this.norg2Mapper = norg2Mapper;
	}


	@Override
	public Node processElement(Node content, String dokumentTypeId) throws RegOppslagFunctionalException, RegOppslagTechnicalException, InvalidElementException {
		try {

			log.info("Henter NavOrgenhet info");

			requestCounter.labels(SERVICE_CODE_TREG001, "NavOrgenhetPostadressePlugin");

			Postadresse adresse = unmarshal(content);

			Organisasjonsenhet wsEnhet = norg2Consumer.hentKontaktinformasjonForEnhet(adresse.getEnhetsId());

			norg2Mapper.mapPostadresse(wsEnhet, adresse);

			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setNamespaceAware(true);

			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document document = builder.newDocument();

			Node node = marshal(adresse, document);
			Document newNode = (Document) node;
			Element documentElement = newNode.getDocumentElement();

			log.info("NavOrgenhet er beriket med data");
			return newNode.renameNode(documentElement, content.getNamespaceURI(), content.getLocalName());

		} catch (JAXBException | ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}
}