package no.nav.regoppslag.treg001;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.navfelles.Sakspart;
import no.nav.dok.brevdata.felles.v1.simpletypes.AktoerType;
import no.nav.regoppslag.consumer.ereg.EregConsumer;
import no.nav.regoppslag.consumer.ereg.support.Organisasjon;
import no.nav.regoppslag.consumer.ereg.support.OrganisasjonEregMapper;
import no.nav.regoppslag.consumer.pdl.PdlGraphQLConsumer;
import no.nav.regoppslag.exceptions.MarshallerException;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.regoppslag.treg001.xmlenricher.ElementEnricherPlugin;
import no.nav.regoppslag.treg001.xmlenricher.util.JaxbHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;
import java.util.Map;

import static no.nav.regoppslag.metrics.MetricLabels.SERVICE_CODE_TREG001;
import static no.nav.regoppslag.treg001.xmlenricher.util.ValueMapKeys.DOKUMENTTYPEID;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Component
@Slf4j
public class SakspartPlugin extends JaxbHelper<Sakspart> implements ElementEnricherPlugin {

	private static final String ELEMENT_LOCALNAME = "sakspart";
	private static final String UGYLDIG_INPUT = "SaksportPlugin - Ugyldig input";
	private static final String PLUGIN_NAME = "SakspartPlugin";

	private final EregConsumer eregConsumer;
	private final OrganisasjonEregMapper organisasjonEregMapper;
	private final MicrometerMetrics metrics;
	private final PdlGraphQLConsumer pdlGraphQLConsumer;

	public SakspartPlugin(MicrometerMetrics metrics,
						  PdlGraphQLConsumer pdlGraphQLConsumer,
						  EregConsumer eregConsumer,
						  OrganisasjonEregMapper organisasjonEregMapper) {
		super(Sakspart.class);
		this.metrics = metrics;
		this.pdlGraphQLConsumer = pdlGraphQLConsumer;
		this.eregConsumer = eregConsumer;
		this.organisasjonEregMapper = organisasjonEregMapper;
	}

	@Override
	public Node processElement(Node content, Map<String, Object> valueMap, String tema) throws RegOppslagSecurityException {
		String dokumenttypeId = (String) valueMap.get(DOKUMENTTYPEID.name());
		metrics.pluginReceived(SERVICE_CODE_TREG001, PLUGIN_NAME);

		validateElementType(content);
		try {
			if (dokumenttypeId == null) {
				throw new RegoppslagIllegalArgumentException(String.format("Feil i %s, dokumentTypeId kan ikke være tom", PLUGIN_NAME), BAD_REQUEST);
			}
			Sakspart sakspart = unmarshal(content);
			log.info(String.format("Henter sakspart info. dokumentTypeId=%s", dokumenttypeId));

			//Skal elementet berikes?
			if (sakspart.isBerik()) {
				validateMottaker(sakspart);

				if (AktoerType.PERSON.equals(sakspart.getTypeKode())) {
					String navn = pdlGraphQLConsumer.hentNavn(sakspart.getId(), tema);
					sakspart.setNavn(navn);

				} else {
					Organisasjon organisasjon = eregConsumer.hentOrganisasjon(sakspart.getId());
					sakspart.setNavn(organisasjonEregMapper.getSakspartNavn(organisasjon));
				}
			}

			Document newNode = convertObjectToDocument(sakspart);
			Element documentElement = newNode.getDocumentElement();

			log.info(String.format("Sakspart er beriket med data. dokumentTypeId=%s", dokumenttypeId));

			return newNode.renameNode(documentElement, content.getNamespaceURI(), content.getLocalName());
		} catch (ParserConfigurationException | MarshallerException e) {
			throw new RegOppslagTechnicalException(String.format("Feil i %s: %s", PLUGIN_NAME, e.getMessage()), e, UGYLDIG_INPUT);
		} finally {
			invalidateSecurityContext();
		}
	}

	private void validateMottaker(Sakspart sakspart) {

		if (sakspart.getTypeKode() == null) {
			throw new RegoppslagIllegalArgumentException(String.format("Feil i %s: Sakspart mangler AktoerTypeKode.", PLUGIN_NAME), BAD_REQUEST);
		}

		if (StringUtils.isEmpty(sakspart.getId())) {
			throw new RegoppslagIllegalArgumentException(String.format("Feil i %s: Sakspart mangler id", PLUGIN_NAME), BAD_REQUEST);
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