package no.nav.regoppslag.treg001;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.regoppslag.exceptions.MarshallerException;
import no.nav.regoppslag.exceptions.MottakerManglerWorkaroundException;
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
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Component
@Slf4j
public class MottakerPlugin extends JaxbHelper<Mottaker> implements ElementEnricherPlugin {

	private static final String ELEMENT_LOCALNAME = "mottaker";
	private static final String PLUGIN_NAME = "MottakerPlugin";
	public static final String MOTTAKER_MANGLER_REASON_CODE = "mottaker-mangler-workaround";

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
			throw new RegoppslagIllegalArgumentException(format("Feil i %s, dokumentTypeId kan ikke være tom", PLUGIN_NAME), BAD_REQUEST);
		}

		try {
			Mottaker mottaker = unmarshal(content);
			validateMottaker(mottaker);
			Mottaker newMottaker = mapPdlForTreg001.getMottakerFraPdl(mottaker, dokumenttypeId);

			Document newNode = convertObjectToDocument(newMottaker);
			Element documentElement = newNode.getDocumentElement();

			log.info("Mottaker er beriket med data. dokumentTypeId={}", dokumenttypeId);

			return newNode.renameNode(documentElement, content.getNamespaceURI(), content.getLocalName());
		} catch (ParserConfigurationException | MarshallerException e) {
			throw new RegOppslagTechnicalException(format("Feil i %s med feilmelding=%s", PLUGIN_NAME, e.getMessage()), e);
		} finally {
			clearSecurityContext();
		}

	}

	private void validateMottaker(Mottaker mottaker) {
		if (mottaker.getTypeKode() == null) {
			throw new MottakerManglerWorkaroundException(format("Feil i %s med feilmelding=Mottakerdata mangler AktoerType. AktoerType kan ikke være null.", PLUGIN_NAME), MOTTAKER_MANGLER_REASON_CODE);
		}

		if (isBlank(mottaker.getId()) || mottaker.getId().trim().isEmpty()) {
			throw new MottakerManglerWorkaroundException(format("Feil i %s med feilmelding=Mottakerdata mangler mottakerId", PLUGIN_NAME), MOTTAKER_MANGLER_REASON_CODE);
		}
	}

	private void validateElementType(Node element) {
		if (!ELEMENT_LOCALNAME.equals(element.getLocalName())) {
			throw new RegoppslagIllegalArgumentException("Unexpected element. Expected " + ELEMENT_LOCALNAME
					+ ". Found {" + element.getNamespaceURI() + "}" + element.getLocalName(), BAD_REQUEST);
		}
	}

}