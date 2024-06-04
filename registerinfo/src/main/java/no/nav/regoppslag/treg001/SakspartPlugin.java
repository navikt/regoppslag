package no.nav.regoppslag.treg001;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.navfelles.Sakspart;
import no.nav.regoppslag.consumer.ereg.EregConsumer;
import no.nav.regoppslag.consumer.ereg.support.Organisasjon;
import no.nav.regoppslag.consumer.ereg.support.OrganisasjonEregMapper;
import no.nav.regoppslag.consumer.pdl.PdlGraphQLConsumer;
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
import static no.nav.dok.brevdata.felles.v1.simpletypes.AktoerType.PERSON;
import static no.nav.regoppslag.treg001.MottakerPlugin.MOTTAKER_MANGLER_REASON_CODE;
import static no.nav.regoppslag.treg001.xmlenricher.util.ValueMapKeys.DOKUMENTTYPEID;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Component
@Slf4j
public class SakspartPlugin extends JaxbHelper<Sakspart> implements ElementEnricherPlugin {

	private static final String ELEMENT_LOCALNAME = "sakspart";

	private final EregConsumer eregConsumer;
	private final OrganisasjonEregMapper organisasjonEregMapper;
	private final PdlGraphQLConsumer pdlGraphQLConsumer;

	public SakspartPlugin(PdlGraphQLConsumer pdlGraphQLConsumer,
						  EregConsumer eregConsumer,
						  OrganisasjonEregMapper organisasjonEregMapper) {
		super(Sakspart.class);

		this.pdlGraphQLConsumer = pdlGraphQLConsumer;
		this.eregConsumer = eregConsumer;
		this.organisasjonEregMapper = organisasjonEregMapper;
	}

	@Override
	public Node processElement(Node content, Map<String, Object> valueMap) throws RegOppslagSecurityException {
		String dokumenttypeId = (String) valueMap.get(DOKUMENTTYPEID.name());

		validateElementType(content);
		try {
			if (dokumenttypeId == null) {
				throw new RegoppslagIllegalArgumentException("Feil i SakspartPlugin, dokumentTypeId kan ikke være tom", BAD_REQUEST);
			}
			Sakspart sakspart = unmarshal(content);
			log.info("Henter sakspart info. dokumentTypeId={}", dokumenttypeId);

			//Skal elementet berikes?
			if (sakspart.isBerik()) {
				validateSakspart(sakspart);

				if (PERSON.equals(sakspart.getTypeKode())) {
					String navn = pdlGraphQLConsumer.hentNavn(sakspart.getId());
					sakspart.setNavn(navn);

				} else {
					Organisasjon organisasjon = eregConsumer.hentOrganisasjon(sakspart.getId());
					sakspart.setNavn(organisasjonEregMapper.getSakspartNavn(organisasjon));
				}
			}

			Document newNode = convertObjectToDocument(sakspart);
			Element documentElement = newNode.getDocumentElement();

			log.info("Sakspart er beriket med data. dokumentTypeId={}", dokumenttypeId);

			return newNode.renameNode(documentElement, content.getNamespaceURI(), content.getLocalName());
		} catch (ParserConfigurationException | MarshallerException e) {
			throw new RegOppslagTechnicalException(format("Feil i SakspartPlugin med feilmelding=%s", e.getMessage()), e);
		} finally {
			clearSecurityContext();
		}
	}

	private void validateSakspart(Sakspart sakspart) {
		if (sakspart.getTypeKode() == null) {
			throw new MottakerManglerWorkaroundException("Feil i SakspartPlugin med feilmelding=Sakspart mangler AktoerTypeKode.", MOTTAKER_MANGLER_REASON_CODE);
		}

		if (isEmpty(sakspart.getId())) {
			throw new MottakerManglerWorkaroundException("Feil i SakspartPlugin med feilmelding=Sakspart mangler id", MOTTAKER_MANGLER_REASON_CODE);
		}
	}

	private void validateElementType(Node element) {
		if (!ELEMENT_LOCALNAME.equals(element.getLocalName())) {
			throw new RegoppslagIllegalArgumentException("Unexpected element. Expected " + ELEMENT_LOCALNAME
					+ ". Found {" + element.getNamespaceURI() + "}" + element.getLocalName(), BAD_REQUEST);
		}
	}
}