package no.nav.regoppslag.treg001;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.simpletypes.Spraakkode;
import no.nav.dokkat.api.tkat020.v3.SpraakInfoTo;
import no.nav.regoppslag.consumer.dkif.DigitalKontaktinformasjon;
import no.nav.regoppslag.consumer.dokkat.Tkat020DokumenttypeInfo;
import no.nav.regoppslag.consumer.organisasjonv4.OrganisasjonV4Consumer;
import no.nav.regoppslag.consumer.organisasjonv4.support.OrganisasjonV4Mapper;
import no.nav.regoppslag.exceptions.MarshallerException;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.regoppslag.treg001.support.SpraakKodeMapper;
import no.nav.regoppslag.treg001.xmlenricher.ElementEnricherPlugin;
import no.nav.regoppslag.treg001.xmlenricher.util.JaxbHelper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static no.nav.regoppslag.metrics.MetricLabels.SERVICE_CODE_TREG001;
import static no.nav.regoppslag.treg001.xmlenricher.util.ValueMapKeys.DOKUMENTTYPEID;
import static no.nav.regoppslag.treg001.xmlenricher.util.ValueMapKeys.MAALFORM;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

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
	private final MapPdlForTreg001 mapPdlForTreg001;
	private final MicrometerMetrics metrics;
	private final DigitalKontaktinformasjon digitalKontaktinformasjon;
	private final Tkat020DokumenttypeInfo tkat020DokumenttypeInfo;

	@Inject
	public MottakerPlugin(OrganisasjonV4Consumer organisasjonV4Consumer, OrganisasjonV4Mapper organisasjonV4Mapper,
						  MapPdlForTreg001 mapPdlForTreg001, DigitalKontaktinformasjon digitalKontaktinformasjon,
						  Tkat020DokumenttypeInfo tkat020DokumenttypeInfo,
						  MicrometerMetrics metrics) {
		super(Mottaker.class);
		this.organisasjonV4Consumer = organisasjonV4Consumer;
		this.organisasjonV4Mapper = organisasjonV4Mapper;
		this.mapPdlForTreg001 = mapPdlForTreg001;
		this.digitalKontaktinformasjon = digitalKontaktinformasjon;
		this.tkat020DokumenttypeInfo = tkat020DokumenttypeInfo;
		this.metrics = metrics;
	}

	@Override
	public Node processElement(Node content, Map<String, Object> valueMap, String tema) throws RegOppslagSecurityException {
		String dokumenttypeId = (String) valueMap.get(DOKUMENTTYPEID.name());
		SpraakKodeMapper spraakKodeMapper = (SpraakKodeMapper) valueMap.get(MAALFORM.name());
		metrics.pluginReceived(SERVICE_CODE_TREG001, PLUGIN_NAME);

		validateElementType(content);

		if (dokumenttypeId == null) {
			throw new RegoppslagIllegalArgumentException(format("Feil i %s, dokumentTypeId kan ikke være tom", PLUGIN_NAME), BAD_REQUEST);
		}

		try {

			Mottaker mottaker = unmarshal(content);
			validateMottaker(mottaker);
			Mottaker newMottaker = mapPdlForTreg001.getMottakerFraPdl(tema, mottaker);
			final Spraakkode spraakkode = getSpraakkode(spraakKodeMapper, mottaker, dokumenttypeId, digitalKontaktinformasjon.hentSpraak(mottaker.getId(), false));
			newMottaker.setSpraakkode(spraakkode);

			Document newNode = convertObjectToDocument(newMottaker);
			Element documentElement = newNode.getDocumentElement();

			log.info(format("Mottaker er beriket med data. dokumentTypeId=%s", dokumenttypeId));

			return newNode.renameNode(documentElement, content.getNamespaceURI(), content.getLocalName());
		} catch (ParserConfigurationException | MarshallerException e) {
			throw new RegOppslagTechnicalException(format("Feil i %s: %s", PLUGIN_NAME, e.getMessage()), e, UGYLDIG_INPUT);
		} finally {
			invalidateSecurityContext();
		}

	}

	public Spraakkode getSpraakkode(SpraakKodeMapper spraakKodeMapper, Mottaker mottaker, String dokumenttypeId, String spraak) {
		log.info(format("Henter mottaker info. dokumentTypeId=%s", dokumenttypeId));
		//Sjekker språket på malen opp mot mottakers preferanser
		List<SpraakInfoTo> sprakinfos = tkat020DokumenttypeInfo.hentDokumenttypeInfoSpraak(dokumenttypeId);
		if (sprakinfos == null || sprakinfos.isEmpty()) {
			log.warn(format("Finner ikke språkinfo i DOKKAT for dokumenttypeid=%s.", dokumenttypeId));
		}
		return spraakKodeMapper.getSpraakKode(mottaker, spraak, sprakinfos);
	}

	private void validateMottaker(Mottaker mottaker) {

		if (mottaker.getTypeKode() == null) {
			throw new RegoppslagIllegalArgumentException(format("Feil i %s: Mottakerdata mangler AktoerType. AktoerType kan ikke være null.", PLUGIN_NAME), BAD_REQUEST);
		}

		if (isBlank(mottaker.getId()) || mottaker.getId().trim().isEmpty()) {
			throw new RegoppslagIllegalArgumentException(format("Feil i %s: Mottakerdata mangler mottakerId", PLUGIN_NAME), BAD_REQUEST);
		}

	}

	private void validateElementType(Node element) {
		if (!ELEMENT_LOCALNAME.equals(element.getLocalName())) {
			throw new RegoppslagIllegalArgumentException("Unexpected element. Expected " + ELEMENT_LOCALNAME
					+ ". Found {" + element.getNamespaceURI() + "}" + element.getLocalName(), BAD_REQUEST);
		}
	}

	private void invalidateSecurityContext() {
		if (SecurityContextHolder.getContext().getAuthentication() != null) {
			SecurityContextHolder.getContext().getAuthentication().setAuthenticated(false);
		}
	}
}