package no.nav.regoppslag.treg001;

import static no.nav.regoppslag.consumer.dokkat.Tkat020DokumenttypeInfo.HENT_DOKKAT_SPRAAKINFO;
import static no.nav.regoppslag.consumer.organisasjonv4.OrganisasjonV4Consumer.HENT_ORGANISASJON;
import static no.nav.regoppslag.consumer.organisasjonv4.OrganisasjonV4Consumer.ORGV4_ORG_IKKE_FUNNET;
import static no.nav.regoppslag.consumer.personv3.PersonV3Consumer.HENT_PERSON;
import static no.nav.regoppslag.consumer.personv3.PersonV3Consumer.PERSON_IKKE_FUNNET;
import static no.nav.regoppslag.metrics.PrometheusLabels.CACHE_COUNTER;
import static no.nav.regoppslag.metrics.PrometheusLabels.CACHE_TOTAL;
import static no.nav.regoppslag.metrics.PrometheusLabels.GENERELT;
import static no.nav.regoppslag.metrics.PrometheusLabels.PLUGIN;
import static no.nav.regoppslag.metrics.PrometheusLabels.RECEIVED;
import static no.nav.regoppslag.metrics.PrometheusLabels.SERVICE_CODE_TREG001;
import static no.nav.regoppslag.metrics.PrometheusMetrics.getConsumerId;
import static no.nav.regoppslag.metrics.PrometheusMetrics.getSubjectId;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestCounter;
import static no.nav.regoppslag.xmlenricher.util.ValueMapKeys.DOKUMENTTYPEID;
import static no.nav.regoppslag.xmlenricher.util.ValueMapKeys.MAALFORM;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.simpletypes.AktoerType;
import no.nav.dokkat.api.tkat020.v3.SpraakInfoTo;
import no.nav.regoppslag.consumer.dokkat.Tkat020DokumenttypeInfo;
import no.nav.regoppslag.consumer.organisasjonv4.OrganisasjonV4Consumer;
import no.nav.regoppslag.consumer.organisasjonv4.support.OrganisasjonV4Mapper;
import no.nav.regoppslag.consumer.personv3.PersonV3Consumer;
import no.nav.regoppslag.consumer.personv3.support.PersonV3Mapper;
import no.nav.regoppslag.exceptions.MarshallerException;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.treg001.support.Maalform;
import no.nav.regoppslag.xmlenricher.ElementEnricherPlugin;
import no.nav.regoppslag.xmlenricher.util.JaxbHelper;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.List;
import java.util.Map;

/**
 * @author Hans Petter Simonsen - Miles
 */
@Component
@Slf4j
public class MottakerPlugin extends JaxbHelper<Mottaker> implements ElementEnricherPlugin {

	private static final String ELEMENT_NS = "http://nav.no/dok/brevdata/felles/v1/NAVFelles";
	private static final String ELEMENT_LOCALNAME = "mottaker";
	private static final String UGYLDIG_INPUT = "MottakerPlugin - Ugyldig input";
	public static final String PLUGIN_NAME = "MottakerPlugin";

	private PersonV3Consumer personV3Consumer;

	private PersonV3Mapper personV3Mapper;

	private OrganisasjonV4Consumer organisasjonV4Consumer;

	private OrganisasjonV4Mapper organisasjonV4Mapper;

	private Tkat020DokumenttypeInfo tkat020DokumenttypeInfo;


	public MottakerPlugin() {
		super(Mottaker.class);
	}

	@Inject
	public MottakerPlugin(PersonV3Consumer personV3Consumer, PersonV3Mapper personV3Mapper, OrganisasjonV4Consumer organisasjonV4Consumer, OrganisasjonV4Mapper organisasjonV4Mapper, Tkat020DokumenttypeInfo tkat020DokumenttypeInfo) {
		super(Mottaker.class);
		this.personV3Consumer = personV3Consumer;
		this.personV3Mapper = personV3Mapper;
		this.organisasjonV4Consumer = organisasjonV4Consumer;
		this.organisasjonV4Mapper = organisasjonV4Mapper;
		this.tkat020DokumenttypeInfo = tkat020DokumenttypeInfo;
	}

	@Override
	public Node processElement(Node content, Map<String, Object> valueMap) throws RegOppslagFunctionalException, RegOppslagTechnicalException, RegOppslagSecurityException {
		String dokumenttypeId = (String) valueMap.get(DOKUMENTTYPEID.name());
		Maalform maalform = (Maalform) valueMap.get(MAALFORM.name());
		requestCounter.labels(SERVICE_CODE_TREG001, PLUGIN_NAME, PLUGIN, getConsumerId(), RECEIVED).inc();

		validateElementType(content);
		try {
			if (dokumenttypeId == null) {
				throw new RegOppslagFunctionalException(String.format("Feil i %s, dokumentTypeId kan ikke være tom", PLUGIN_NAME), UGYLDIG_INPUT);
			}
			Mottaker mottaker = unmarshal(content);
			log.info(String.format("Henter mottaker info. dokumentTypeId=%s", dokumenttypeId));

			//Skal elementet berikes?
			if (mottaker.isBerik()) {
				validateMottaker(mottaker);

				if (AktoerType.PERSON.equals(mottaker.getTypeKode())) {
					requestCounter.labels(SERVICE_CODE_TREG001, HENT_PERSON, CACHE_COUNTER, getConsumerId(), CACHE_TOTAL)
							.inc();
					Bruker person = personV3Consumer.hentPerson(mottaker.getId(), getConsumerId(), getSubjectId(), SERVICE_CODE_TREG001);
					if (person == null) {
						throw new RegOppslagFunctionalException(String.format("Feil i %s:  Kunne ikke finne person. MottakerId=%s", PLUGIN_NAME, mottaker
								.getId()), PLUGIN_NAME + " - " + PERSON_IKKE_FUNNET);
					}
					personV3Mapper.map(person, mottaker, SERVICE_CODE_TREG001);

				} else {
					requestCounter.labels(SERVICE_CODE_TREG001, HENT_ORGANISASJON, CACHE_COUNTER, getConsumerId(), CACHE_TOTAL)
							.inc();
					Organisasjon organisasjon = organisasjonV4Consumer.hentOrganisasjon(mottaker.getId(), SERVICE_CODE_TREG001);
					if (organisasjon == null) {
						throw new RegOppslagFunctionalException(String.format("Feil i %s:  Kunne ikke finne organisasjon. MottakerId=%s", PLUGIN_NAME, mottaker
								.getId()), PLUGIN_NAME + " - " + ORGV4_ORG_IKKE_FUNNET);
					}
					organisasjonV4Mapper.map(organisasjon, mottaker, SERVICE_CODE_TREG001);
				}
			}

			//Sjekker språket på malen opp mot mottakers preferanser
			requestCounter.labels(SERVICE_CODE_TREG001, HENT_DOKKAT_SPRAAKINFO, CACHE_COUNTER, getConsumerId(), CACHE_TOTAL)
					.inc();
			List<SpraakInfoTo> sprakinfos = tkat020DokumenttypeInfo.hentDokumenttypeInfoSpraak(dokumenttypeId);
			if (sprakinfos == null || sprakinfos.isEmpty()) {
				requestCounter.labels(SERVICE_CODE_TREG001, "ManglerSpraakInfo", GENERELT, getConsumerId(), dokumenttypeId)
						.inc();
				log.warn(String.format("Finner ikke språkinfo i DOKKAT for dokumenttypeid=%s.", dokumenttypeId));
			}

			maalform.setMaalform(mottaker, sprakinfos);

			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setNamespaceAware(true);

			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document document = builder.newDocument();

			Node node = marshal(mottaker, document);
			Document newNode = (Document) node;
			Element documentElement = newNode.getDocumentElement();

			log.info(String.format("Mottaker er beriket med data. dokumentTypeId=%s", dokumenttypeId));

			return newNode.renameNode(documentElement, content.getNamespaceURI(), content.getLocalName());
		} catch (ParserConfigurationException | MarshallerException e) {
			throw new RegOppslagTechnicalException(String.format("Feil i %s: %s", PLUGIN_NAME, e.getMessage()), e, UGYLDIG_INPUT);
		} finally {
			invalidateSecurityContext();

		}

	}

	private void validateMottaker(Mottaker mottaker) throws RegOppslagFunctionalException {

		if (mottaker.getTypeKode() == null) {
			throw new RegOppslagFunctionalException(String.format("Feil i %s: Mottakerdata mangler AktoerType. AktoerType kan ikke være null.", PLUGIN_NAME), UGYLDIG_INPUT);
		}

		if (StringUtils.isEmpty(mottaker.getId()) || mottaker.getId().trim().isEmpty()) {
			throw new RegOppslagFunctionalException(String.format("Feil i %s: Mottakerdata mangler mottakerId", PLUGIN_NAME), UGYLDIG_INPUT);
		}

	}

	private void validateElementType(Node element) throws RegOppslagFunctionalException {
		if (!ELEMENT_LOCALNAME.equals(element.getLocalName())) {
			throw new RegOppslagFunctionalException("Unexpected element. Expected " + ELEMENT_LOCALNAME
					+ ". Found {" + element.getNamespaceURI() + "}" + element.getLocalName(), UGYLDIG_INPUT);
		}
	}

	private void invalidateSecurityContext() {
		if (SecurityContextHolder.getContext().getAuthentication() != null) {
			SecurityContextHolder.getContext().getAuthentication().setAuthenticated(false);
		}
	}
}