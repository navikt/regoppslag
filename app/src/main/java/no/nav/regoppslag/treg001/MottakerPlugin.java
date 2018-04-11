package no.nav.regoppslag.treg001;

import static no.nav.regoppslag.consumer.dokkat.Tkat020DokumenttypeInfo.HENT_DOKKAT_SPRAAKINFO;
import static no.nav.regoppslag.consumer.organisasjonv4.OrganisasjonV4Consumer.HENT_ORGANISASJON;
import static no.nav.regoppslag.consumer.organisasjonv4.OrganisasjonV4Consumer.ORGANISASJON_V4;
import static no.nav.regoppslag.consumer.personv3.PersonV3Consumer.HENT_PERSON;
import static no.nav.regoppslag.metrics.PrometheusLabels.CACHE_HIT;
import static no.nav.regoppslag.metrics.PrometheusLabels.SERVICE_CODE_TREG001;
import static no.nav.regoppslag.metrics.PrometheusMetrics.cacheCounter;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestCounter;
import static no.nav.regoppslag.nais.checks.PersonV3Check.PERSONV3_LABEL;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.dok.metaforcemal.jaxb2.gen.AktoerType;
import no.nav.dok.metaforcemal.jaxb2.gen.Mottaker;
import no.nav.dokkat.api.tkat020.v3.SpraakInfoTo;
import no.nav.regoppslag.consumer.dokkat.Tkat020DokumenttypeInfo;
import no.nav.regoppslag.consumer.organisasjonv4.OrganisasjonV4Consumer;
import no.nav.regoppslag.consumer.organisasjonv4.support.OrganisasjonV4Mapper;
import no.nav.regoppslag.consumer.personv3.PersonV3Consumer;
import no.nav.regoppslag.consumer.personv3.support.PersonV3Mapper;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.treg001.support.Maalform;
import no.nav.regoppslag.xmlenricher.ElementEnricherPlugin;
import no.nav.regoppslag.xmlenricher.util.JaxbHelper;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
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
import java.util.List;

/**
 * @author Hans Petter Simonsen - Miles
 */
@Component
@Scope("prototype")
@Slf4j
public class MottakerPlugin extends JaxbHelper<Mottaker> implements ElementEnricherPlugin {
	Logger LOG = LoggerFactory.getLogger(MottakerPlugin.class);
	public static final String ELEMENT_NS = "http://nav.no/dok/pesysbrev/felles/v1/PesysFelles";
	public static final String ELEMENT_LOCALNAME = "mottaker";
	
	private PersonV3Consumer personV3Consumer;
	
	private PersonV3Mapper personV3Mapper;
	
	private OrganisasjonV4Consumer organisasjonV4Consumer;
	
	private OrganisasjonV4Mapper organisasjonV4Mapper;
	
	private Tkat020DokumenttypeInfo tkat020DokumenttypeInfo;
	
	private Maalform maalform;
	
	private Authentication authentication;
	
	public MottakerPlugin() {
		super(Mottaker.class);
	}
	
	@Inject
	public MottakerPlugin(PersonV3Consumer personV3Consumer, PersonV3Mapper personV3Mapper, OrganisasjonV4Consumer organisasjonV4Consumer, OrganisasjonV4Mapper organisasjonV4Mapper, Tkat020DokumenttypeInfo tkat020DokumenttypeInfo, Maalform maalform) {
		super(Mottaker.class);
		this.personV3Consumer = personV3Consumer;
		this.personV3Mapper = personV3Mapper;
		this.organisasjonV4Consumer = organisasjonV4Consumer;
		this.organisasjonV4Mapper = organisasjonV4Mapper;
		this.tkat020DokumenttypeInfo = tkat020DokumenttypeInfo;
		this.maalform = maalform;
		this.authentication = SecurityContextHolder.getContext().getAuthentication();
	}
	
	@Override
	public Node processElement(Node content, String dokumentTypeId, NamespacePrefixMapper prefixMapper) throws RegOppslagFunctionalException, RegOppslagTechnicalException, RegOppslagSecurityException {
		if (prefixMapper != null) {
			setNamespacePrefixMapper(prefixMapper);
		}
		
		SecurityContextHolder.getContext().setAuthentication(this.authentication);
		validateElementType(content);
		try {
			requestCounter.labels(SERVICE_CODE_TREG001, "plugin", "MottakerPlugin").inc();
			if (dokumentTypeId == null) {
				throw new RegOppslagFunctionalException("Feil i mottakerPlugin, dokumentTypeId må ha verdi!");
			}
			Mottaker mottaker = unmarshal(content);
			log.info(String.format("Henter mottaker info. dokumentTypeId=%s, MottakerId=%s", dokumentTypeId, mottaker.getId()));
			
			validateMottaker(mottaker);
			
			if (AktoerType.PERSON.equals(mottaker.getTypeKode())) {
				cacheCounter.labels(HENT_PERSON, PERSONV3_LABEL, CACHE_HIT).inc();
				Bruker person = personV3Consumer.hentPerson(mottaker.getId());
				if (person == null) {
					throw new RegOppslagFunctionalException(String.format("Feil i mottakerPlugin:  Kunne ikke finne person. mottakerId=%s", mottaker
							.getId()));
				}
				
				personV3Mapper.map(person, mottaker);
				
			} else {
				cacheCounter.labels(HENT_ORGANISASJON, ORGANISASJON_V4, CACHE_HIT).inc();
				Organisasjon organisasjon = organisasjonV4Consumer.hentOrganisasjon(mottaker.getId());
				if (organisasjon == null) {
					throw new RegOppslagFunctionalException(String.format("Feil i mottakerPlugin:  Kunne ikke finne organisasjon. mottakerId=%s", mottaker
							.getId()));
				}
				organisasjonV4Mapper.map(organisasjon, mottaker);
			}
			//Sjekker språket på malen opp mot mottakers preferanser
			cacheCounter.labels(HENT_DOKKAT_SPRAAKINFO, "DOKKAT", CACHE_HIT).inc();
			List<SpraakInfoTo> sprakinfos = tkat020DokumenttypeInfo.hentDokumenttypeInfoSpraak(dokumentTypeId);
			if (sprakinfos == null) {
				log.warn("Finner ikke språkinfo i DOKKAT for dokumenttypeid=" + dokumentTypeId);
			}
			maalform.setMaalform(mottaker, sprakinfos);
			
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setNamespaceAware(true);
			
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document document = builder.newDocument();
			
			Node node = marshal(mottaker, document);
			Document newNode = (Document) node;
			Element documentElement = newNode.getDocumentElement();
			
			log.info(String.format("Mottaker er beriket med data. dokumentTypeId=%s, MottakerId=%s", dokumentTypeId, mottaker.getId()));
			
			return newNode.renameNode(documentElement, content.getNamespaceURI(), content.getLocalName());
		} catch (JAXBException |
				ParserConfigurationException e) {
			throw new RegOppslagFunctionalException(e);
		}
		
	}
	
	private void validateMottaker(Mottaker mottaker) throws RegOppslagFunctionalException {
		if (mottaker.getTypeKode() == null || StringUtils.isEmpty(mottaker.getId())) {
			throw new RegOppslagFunctionalException(String.format("Feil i mottakerPlugin: Mottakerdata mangler påkrevde parametere."));
		}
	}
	
	private void validateElementType(Node element) throws RegOppslagFunctionalException {
		if (!ELEMENT_NS.equals(element.getNamespaceURI())
				|| !ELEMENT_LOCALNAME.equals(element.getLocalName())) {
			throw new RegOppslagFunctionalException("Unexpected element. Expected {" + ELEMENT_NS + "}" + ELEMENT_LOCALNAME
					+ ". Found {" + element.getNamespaceURI() + "}" + element.getLocalName());
		}
	}
}