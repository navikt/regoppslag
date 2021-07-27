package no.nav.regoppslag.treg001;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.simpletypes.AktoerType;
import no.nav.dokkat.api.tkat020.v3.SpraakInfoTo;
import no.nav.regoppslag.consumer.dkif.DigitalKontaktinformasjon;
import no.nav.regoppslag.consumer.dokkat.Tkat020DokumenttypeInfo;
import no.nav.regoppslag.consumer.organisasjonv4.OrganisasjonV4Consumer;
import no.nav.regoppslag.consumer.organisasjonv4.support.OrganisasjonV4Mapper;
import no.nav.regoppslag.consumer.pdl.PdlGraphQLConsumer;
import no.nav.regoppslag.consumer.pdl.PdlMottakerInfo;
import no.nav.regoppslag.consumer.pdl.map.MapPDLResponse;
import no.nav.regoppslag.exceptions.MarshallerException;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.regoppslag.treg001.support.SpraakKodeMapper;
import no.nav.regoppslag.treg001.to.MottakerTo;
import no.nav.regoppslag.xmlenricher.ElementEnricherPlugin;
import no.nav.regoppslag.xmlenricher.util.JaxbHelper;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import java.util.List;
import java.util.Map;

import static no.nav.regoppslag.metrics.MetricLabels.SERVICE_CODE_TREG001;
import static no.nav.regoppslag.xmlenricher.util.ValueMapKeys.DOKUMENTTYPEID;
import static no.nav.regoppslag.xmlenricher.util.ValueMapKeys.MAALFORM;

/**
 * @author Hans Petter Simonsen - Miles
 */
@Component
@Slf4j
public class MottakerPlugin extends JaxbHelper<Mottaker> implements ElementEnricherPlugin {

	private static final String ELEMENT_LOCALNAME = "mottaker";
	private static final String UGYLDIG_INPUT = "MottakerPlugin - Ugyldig input";
	private static final String PLUGIN_NAME = "MottakerPlugin";

	private final OrganisasjonV4Consumer organisasjonV4Consumer;
	private final OrganisasjonV4Mapper organisasjonV4Mapper;
	private final Tkat020DokumenttypeInfo tkat020DokumenttypeInfo;
	private final PdlGraphQLConsumer pdlGraphQLConsumer;
	private final MapPDLResponse mapPDLResponse;
	private final MapMottakerRequestFromPdl mapMottakerRequestFromPdl;
	private final MicrometerMetrics metrics;
	private final DigitalKontaktinformasjon digitalKontaktinformasjon;

	@Inject
	public MottakerPlugin(PdlGraphQLConsumer pdlGraphQLConsumer,
						  MapPDLResponse mapPDLResponse, OrganisasjonV4Consumer organisasjonV4Consumer,
						  OrganisasjonV4Mapper organisasjonV4Mapper, Tkat020DokumenttypeInfo tkat020DokumenttypeInfo,
						  MicrometerMetrics metrics, MapMottakerRequestFromPdl mapMottakerRequestFromPdl,
						  DigitalKontaktinformasjon digitalKontaktinformasjon) {
		super(Mottaker.class);
		this.organisasjonV4Consumer = organisasjonV4Consumer;
		this.organisasjonV4Mapper = organisasjonV4Mapper;
		this.tkat020DokumenttypeInfo = tkat020DokumenttypeInfo;
		this.pdlGraphQLConsumer = pdlGraphQLConsumer;
		this.mapPDLResponse = mapPDLResponse;
		this.mapMottakerRequestFromPdl = mapMottakerRequestFromPdl;
		this.metrics = metrics;
		this.digitalKontaktinformasjon = digitalKontaktinformasjon;
	}

	@Override
	public Node processElement(Node content, Map<String, Object> valueMap, String tema) throws RegOppslagFunctionalException, RegOppslagTechnicalException, RegOppslagSecurityException {
		String dokumenttypeId = (String) valueMap.get(DOKUMENTTYPEID.name());
		SpraakKodeMapper spraakKodeMapper = (SpraakKodeMapper) valueMap.get(MAALFORM.name());
		metrics.pluginReceived(SERVICE_CODE_TREG001, PLUGIN_NAME);

		validateElementType(content);
		try {
			if (dokumenttypeId == null) {
				throw new RegOppslagFunctionalException(String.format("Feil i %s, dokumentTypeId kan ikke være tom", PLUGIN_NAME), UGYLDIG_INPUT);
			}
			Mottaker mottaker = getMottaker(content, tema);
			log.info(String.format("Henter mottaker info. dokumentTypeId=%s", dokumenttypeId));

			//Sjekker språket på malen opp mot mottakers preferanser
			List<SpraakInfoTo> sprakinfos = tkat020DokumenttypeInfo.hentDokumenttypeInfoSpraak(dokumenttypeId);
			if (sprakinfos == null || sprakinfos.isEmpty()) {
				log.warn(String.format("Finner ikke språkinfo i DOKKAT for dokumenttypeid=%s.", dokumenttypeId));
			}

			mottaker.setSpraakkode(spraakKodeMapper.getSpraakKode(mottaker, digitalKontaktinformasjon.hentSpraak(mottaker.getId(), false), sprakinfos));

			Document newNode = convertObjectToDocument(mottaker);
			Element documentElement = newNode.getDocumentElement();

			log.info(String.format("Mottaker er beriket med data. dokumentTypeId=%s", dokumenttypeId));

			return newNode.renameNode(documentElement, content.getNamespaceURI(), content.getLocalName());
		} catch (ParserConfigurationException | MarshallerException e) {
			throw new RegOppslagTechnicalException(String.format("Feil i %s: %s", PLUGIN_NAME, e.getMessage()), e, UGYLDIG_INPUT);
		} finally {
			invalidateSecurityContext();
		}

	}

	private Mottaker getMottaker(Node content, String tema) throws RegOppslagFunctionalException, RegOppslagTechnicalException{
		try {

			Mottaker mottaker = unmarshal(content);

			//Skal elementet berikes?
			if (mottaker.isBerik()) {
				validateMottaker(mottaker);
				if (AktoerType.PERSON.equals(mottaker.getTypeKode())) {
					PdlMottakerInfo hentPerson = mapPDLResponse.mapHentPerson(
							pdlGraphQLConsumer.hentPerson(mottaker.getId(), tema), SERVICE_CODE_TREG001);
					mottaker = mapMottakerRequestFromPdl.mapMottakerFraPdl(hentPerson);
				} else {
					Organisasjon organisasjon = organisasjonV4Consumer.hentOrganisasjon(mottaker.getId());
					MottakerTo mottakerTo = organisasjonV4Mapper.map(mottaker.getId(), organisasjon, SERVICE_CODE_TREG001);
					mottaker.setMottakeradresse(mottakerTo.getMottaker().getMottakeradresse());
					mottaker.setKortNavn(mottakerTo.getMottaker().getKortNavn());
					mottaker.setNavn(mottakerTo.getMottaker().getNavn());
				}
			}
			return mottaker;
		} catch (MarshallerException e) {
			throw new RegOppslagTechnicalException(String.format("Feil i %s: %s", PLUGIN_NAME, e.getMessage()), e, UGYLDIG_INPUT);
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