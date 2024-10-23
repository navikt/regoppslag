package no.nav.regoppslag.treg001;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.regoppslag.exceptions.FeilGrunnetHoeytVolumWorkaroundException;
import no.nav.regoppslag.exceptions.MarshallerException;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;
import no.nav.regoppslag.treg001.xmlenricher.ElementEnricherPlugin;
import no.nav.regoppslag.treg001.xmlenricher.util.JaxbHelper;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;
import java.util.Map;

import static java.lang.String.format;
import static no.nav.regoppslag.treg001.xmlenricher.util.ValueMapKeys.DOKUMENTTYPEID;
import static no.nav.regoppslag.util.SafeLoggingUtil.removeUnsafeChars;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Component
@Slf4j
public class MottakerPlugin extends JaxbHelper<Mottaker> implements ElementEnricherPlugin {

	private static final String ELEMENT_LOCALNAME = "mottaker";
	public static final String FEIL_GRUNNET_HOEYT_VOLUM_REASON_CODE = "feil-grunnet-hoeyt-volum-workaround";

	private final MapPdlForTreg001 mapPdlForTreg001;

	public MottakerPlugin(MapPdlForTreg001 mapPdlForTreg001) {
		super(Mottaker.class);
		this.mapPdlForTreg001 = mapPdlForTreg001;
	}

	@Override
	public Node processElement(Node content, Map<String, Object> valueMap) throws RegOppslagSecurityException {
		String dokumenttypeId = (String) valueMap.get(DOKUMENTTYPEID.name());
		validateElementType(content);

		if (dokumenttypeId == null) {
			throw new RegoppslagIllegalArgumentException("Feil i MottakerPlugin, dokumentTypeId kan ikke være tom", BAD_REQUEST);
		}

		try {
			Mottaker mottaker = unmarshal(content);
			validateMottaker(mottaker);
			Mottaker newMottaker = mapPdlForTreg001.getMottakerFraPdl(mottaker, dokumenttypeId);

			Document newNode = convertObjectToDocument(newMottaker);
			Element documentElement = newNode.getDocumentElement();

			log.info("Mottaker er beriket med data. dokumentTypeId={}", removeUnsafeChars(dokumenttypeId));

			return newNode.renameNode(documentElement, content.getNamespaceURI(), content.getLocalName());
		} catch (ParserConfigurationException | MarshallerException e) {
			throw new RegOppslagTechnicalException(format("Feil i MottakerPlugin med feilmelding=%s", e.getMessage()), e);
		} finally {
			clearSecurityContext();
		}

	}

	// Høyt volum av kall fra dokprod kan resultere i sporadiske feil i MottakerPlugin. Meldinger havner da på QDOK001_FUNKSJONELL_FEIL i dokprod.
	// Ved å returnere bad request med reason code kan dokprod fange opp dette, og heller gjøre kompletterBrevdata-kallet på nytt slik at melding ikke går til feilkø.
	private void validateMottaker(Mottaker mottaker) {
		if (mottaker.getTypeKode() == null) {
			throw new FeilGrunnetHoeytVolumWorkaroundException("Feil i MottakerPlugin med feilmelding=Mottakerdata mangler AktoerType. AktoerType kan ikke være null.", FEIL_GRUNNET_HOEYT_VOLUM_REASON_CODE);
		}

		if (isBlank(mottaker.getId()) || mottaker.getId().trim().isEmpty()) {
			throw new FeilGrunnetHoeytVolumWorkaroundException("Feil i MottakerPlugin med feilmelding=Mottakerdata mangler mottakerId", FEIL_GRUNNET_HOEYT_VOLUM_REASON_CODE);
		}
	}

	private void validateElementType(Node element) {
		if (!ELEMENT_LOCALNAME.equals(element.getLocalName())) {
			throw new RegoppslagIllegalArgumentException("Unexpected element. Expected " + ELEMENT_LOCALNAME
					+ ". Found {" + element.getNamespaceURI() + "}" + element.getLocalName(), BAD_REQUEST);
		}
	}

}