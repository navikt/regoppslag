package no.nav.regoppslag.treg001;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.navfelles.Postadresse;
import no.nav.regoppslag.consumer.norg2.OrganisasjonsenhetConsumer;
import no.nav.regoppslag.consumer.norg2.support.Norg2Mapper;
import no.nav.regoppslag.consumer.norg2.to.EnhetKontaktinformasjon;
import no.nav.regoppslag.consumer.norg2.to.EnhetNavn;
import no.nav.regoppslag.exceptions.MarshallerException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;
import no.nav.regoppslag.treg001.xmlenricher.ElementEnricherPlugin;
import no.nav.regoppslag.treg001.xmlenricher.util.JaxbHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;
import java.util.Map;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Component
@Slf4j
public class NavOrgenhetPostadressePlugin extends JaxbHelper<Postadresse> implements ElementEnricherPlugin {

	public static final String ELEMENT_LOCALNAME_POST = "postadresse";
	public static final String ELEMENT_LOCALNAME_RETUR = "returadresse";
	public static final String PLUGIN_NAME = "NavOrgenhetPostadressePlugin";

	private OrganisasjonsenhetConsumer norg2Consumer;
	private Norg2Mapper norg2Mapper;

	public NavOrgenhetPostadressePlugin() {
		super(Postadresse.class);
	}

	@Autowired
	public NavOrgenhetPostadressePlugin(OrganisasjonsenhetConsumer norg2Consumer,
										Norg2Mapper norg2Mapper) {
		super(Postadresse.class);
		this.norg2Mapper = norg2Mapper;
		this.norg2Consumer = norg2Consumer;
	}

	@Override
	public Node processElement(Node content, Map<String, Object> valueMap) {
		validateElementType(content);

		try {
			Postadresse adresse = unmarshal(content);
			log.info("Henter NavOrgenhet info. EnhetsId={}", adresse.getEnhetsId());

			//Skal elementet berikes?
			if (adresse.isBerik()) {
				validateAdresse(adresse);

				EnhetNavn enhetNavn = norg2Consumer.hentEnhetNavn(adresse.getEnhetsId());
				EnhetKontaktinformasjon kontaktinformasjon = norg2Consumer.hentEnhetKontaktinformasjon(adresse.getEnhetsId());
				norg2Mapper.mapPostadresse(enhetNavn, kontaktinformasjon, adresse);
			}

			Document newNode = convertObjectToDocument(adresse);
			Element documentElement = newNode.getDocumentElement();

			log.info("NavOrgenhet er beriket med data. EnhetsId={}", adresse.getEnhetsId());

			return newNode.renameNode(documentElement, content.getNamespaceURI(), content.getLocalName());

		} catch (ParserConfigurationException | MarshallerException e) {
			throw new RegOppslagTechnicalException(format("Feil i %s med feilmelding=%s", PLUGIN_NAME, e.getMessage()), e);
		} finally {
			clearSecurityContext();
		}
	}

	private void validateAdresse(Postadresse adresse) {
		if (StringUtils.isEmpty(adresse.getEnhetsId())) {
			throw new RegoppslagIllegalArgumentException(format("Feil i %s med feilmelding=EnhetsId mangler.", PLUGIN_NAME), BAD_REQUEST);
		}
	}

	private void validateElementType(Node element) {
		if (!(ELEMENT_LOCALNAME_POST.equals(element.getLocalName()) || ELEMENT_LOCALNAME_RETUR.equals(element.getLocalName()))) {
			throw new RegoppslagIllegalArgumentException("Unexpected element. Expected " + ELEMENT_LOCALNAME_POST
					+ " or " + ELEMENT_LOCALNAME_RETUR + ". Found {" + element.getNamespaceURI() + "}" + element
					.getLocalName(), BAD_REQUEST);
		}
	}
}