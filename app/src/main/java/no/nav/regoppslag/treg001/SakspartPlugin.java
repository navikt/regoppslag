package no.nav.regoppslag.treg001;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.navfelles.Sakspart;
import no.nav.dok.brevdata.felles.v1.simpletypes.AktoerType;
import no.nav.regoppslag.consumer.organisasjonv4.OrganisasjonV4Consumer;
import no.nav.regoppslag.consumer.organisasjonv4.support.OrganisasjonV4Mapper;
import no.nav.regoppslag.consumer.pdl.PdlGraphQLConsumer;
import no.nav.regoppslag.consumer.personv3.PersonV3Consumer;
import no.nav.regoppslag.consumer.personv3.support.PersonV3Mapper;
import no.nav.regoppslag.exceptions.MarshallerException;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;
import no.nav.regoppslag.metrics.MicrometerMetrics;
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

import javax.xml.parsers.ParserConfigurationException;
import java.util.Map;

import static no.nav.regoppslag.metrics.MetricLabels.SERVICE_CODE_TREG001;
import static no.nav.regoppslag.xmlenricher.util.ValueMapKeys.DOKUMENTTYPEID;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * @author Hans Petter Simonsen - Miles
 */
@Component
@Slf4j
public class SakspartPlugin extends JaxbHelper<Sakspart> implements ElementEnricherPlugin {

	private static final String ELEMENT_LOCALNAME = "sakspart";
	private static final String UGYLDIG_INPUT = "SaksportPlugin - Ugyldig input";
	private static final String PLUGIN_NAME = "SakspartPlugin";

	private final PersonV3Consumer personV3Consumer;
	private final PersonV3Mapper personV3Mapper;
	private final OrganisasjonV4Consumer organisasjonV4Consumer;
	private final OrganisasjonV4Mapper organisasjonV4Mapper;
	private final MicrometerMetrics metrics;
	private final PdlGraphQLConsumer pdlGraphQLConsumer;

	public SakspartPlugin(PersonV3Consumer personV3Consumer,
						  PersonV3Mapper personV3Mapper, OrganisasjonV4Consumer organisasjonV4Consumer,
						  OrganisasjonV4Mapper organisasjonV4Mapper, MicrometerMetrics metrics,
						  PdlGraphQLConsumer pdlGraphQLConsumer) {
		super(Sakspart.class);
		this.personV3Consumer = personV3Consumer;
		this.personV3Mapper = personV3Mapper;
		this.organisasjonV4Consumer = organisasjonV4Consumer;
		this.organisasjonV4Mapper = organisasjonV4Mapper;
		this.metrics = metrics;
		this.pdlGraphQLConsumer = pdlGraphQLConsumer;
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
					Bruker person = personV3Consumer.hentPerson(sakspart.getId(), SERVICE_CODE_TREG001);
					String navn = StringUtils.isBlank(tema) ? personV3Mapper.getSakspartNavn(person) :
							pdlGraphQLConsumer.hentNavn(sakspart.getId(), tema);
					sakspart.setNavn(navn);

				} else {
					Organisasjon organisasjon = organisasjonV4Consumer.hentOrganisasjon(sakspart.getId());
					sakspart.setNavn(organisasjonV4Mapper.getSakspartNavn(organisasjon));
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