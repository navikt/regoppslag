package no.nav.regoppslag.treg001;

import static no.nav.regoppslag.consumer.norg2.OrganisasjonEnhetKontaktinformasjonV1Consumer.HENT_ENHET_NAVN;
import static no.nav.regoppslag.metrics.PrometheusLabels.CACHE_TOTAL;
import static no.nav.regoppslag.metrics.PrometheusLabels.LABEL_CACHE_COUNTER;
import static no.nav.regoppslag.metrics.PrometheusLabels.PLUGIN;
import static no.nav.regoppslag.metrics.PrometheusLabels.RECEIVED;
import static no.nav.regoppslag.metrics.PrometheusLabels.SERVICE_CODE_TREG001;
import static no.nav.regoppslag.metrics.PrometheusMetrics.getConsumerId;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestCounter;
import static no.nav.regoppslag.xmlenricher.util.ValueMapKeys.PREFIXMAPPER;
import static no.nav.regoppslag.xmlenricher.util.ValueMapKeys.SECURITYCONTEXT;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.dok.metaforcemal.jaxb2.gen.Postadresse;
import no.nav.regoppslag.consumer.norg2.OrganisasjonEnhetKontaktinformasjonV1Consumer;
import no.nav.regoppslag.consumer.norg2.support.Norg2Mapper;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.xmlenricher.ElementEnricherPlugin;
import no.nav.regoppslag.xmlenricher.util.JaxbHelper;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.Organisasjonsenhet;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Map;

@Component
@Slf4j
public class NavOrgenhetPostadressePlugin extends JaxbHelper<Postadresse> implements ElementEnricherPlugin {

	public static final String ELEMENT_NS = "http://nav.no/dok/pesysbrev/felles/v1/Kontaktinformasjon";
	public static final String ELEMENT_LOCALNAME_POST = "postadresse";
	public static final String ELEMENT_LOCALNAME_RETUR = "returadresse";

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
	public Node processElement(Node content, Map<String, Object> valueMap) throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		NamespacePrefixMapper prefixMapper = (NamespacePrefixMapper) valueMap.get(PREFIXMAPPER.name());
		SecurityContext securityContext = (SecurityContext) valueMap.get(SECURITYCONTEXT.name());
		
		if (prefixMapper != null) {
			setNamespacePrefixMapper(prefixMapper);
		}
		
		SecurityContextHolder.getContext().setAuthentication(securityContext.getAuthentication());
		
		validateElementType(content);
		try {
			requestCounter.labels(SERVICE_CODE_TREG001, "NavOrgenhetPostadressePlugin", PLUGIN, getConsumerId(), RECEIVED)
					.inc();

			Postadresse adresse = unmarshal(content);
			log.info(String.format("Henter NavOrgenhet info. EnhetsId=%s, ConsumerId=%s", adresse.getEnhetsId(), getConsumerId()));

			validateAdresse(adresse);
			
			requestCounter.labels(SERVICE_CODE_TREG001, HENT_ENHET_NAVN, LABEL_CACHE_COUNTER, getConsumerId(), CACHE_TOTAL)
					.inc();
			Organisasjonsenhet wsEnhet = norg2Consumer.hentKontaktinformasjonForEnhet(adresse.getEnhetsId());

			if (wsEnhet == null) {
				throw new RegOppslagFunctionalException(String.format("Feil i NavOrgenhetPostadressePlugin:  Kunne ikke finne enhet. enhetId=%s, ConsumerId=%s", adresse
						.getEnhetsId(), getConsumerId()));
			}

			norg2Mapper.mapPostadresse(wsEnhet, adresse);

			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setNamespaceAware(true);

			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document document = builder.newDocument();

			Node node = marshal(adresse, document);
			Document newNode = (Document) node;
			Element documentElement = newNode.getDocumentElement();
			
			log.info(String.format("NavOrgenhet er beriket med data. EnhetsId=%s, ConsumerId=%s", adresse.getEnhetsId(), getConsumerId()));
			
			return newNode.renameNode(documentElement, content.getNamespaceURI(), content.getLocalName());

		} catch (JAXBException | ParserConfigurationException e) {
			throw new RegOppslagFunctionalException("NavOrgenhetPostadressePlugin: Feil ved parsing av XML", e);
		}
	}

	private void validateAdresse(Postadresse adresse) throws RegOppslagFunctionalException {
		if (StringUtils.isEmpty(adresse.getEnhetsId())) {
			throw new RegOppslagFunctionalException(String.format("Feil i NavOrgenhetPostadressePlugin: mangler enhetId."));
		}
	}

	private void validateElementType(Node element) throws RegOppslagFunctionalException {
		if (!ELEMENT_NS.equals(element.getNamespaceURI())
				|| (!(ELEMENT_LOCALNAME_POST.equals(element.getLocalName()) || ELEMENT_LOCALNAME_RETUR.equals(element.getLocalName())))) {
			throw new RegOppslagFunctionalException("Unexpected element. Expected {" + ELEMENT_NS + "}" + ELEMENT_LOCALNAME_POST
					+ " or {" + ELEMENT_NS + "}" + ELEMENT_LOCALNAME_POST  + ". Found {" + element.getNamespaceURI() + "}" + element.getLocalName());
		}
	}
}