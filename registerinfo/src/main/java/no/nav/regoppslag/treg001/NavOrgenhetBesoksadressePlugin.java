package no.nav.regoppslag.treg001;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.navfelles.Besoksadresse;
import no.nav.regoppslag.consumer.norg2.OrganisasjonsenhetConsumer;
import no.nav.regoppslag.consumer.norg2.support.Norg2Mapper;
import no.nav.regoppslag.consumer.norg2.to.EnhetKontaktinformasjon;
import no.nav.regoppslag.consumer.norg2.to.EnhetNavn;
import no.nav.regoppslag.exceptions.MarshallerException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;
import no.nav.regoppslag.treg001.xmlenricher.ElementEnricherPlugin;
import no.nav.regoppslag.treg001.xmlenricher.util.JaxbHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;
import java.util.Map;

import static java.lang.String.format;
import static no.nav.regoppslag.treg001.xmlenricher.util.ValueMapKeys.DOKUMENTTYPEID;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Component
@Slf4j
public class NavOrgenhetBesoksadressePlugin extends JaxbHelper<Besoksadresse> implements ElementEnricherPlugin {

	public static final String ELEMENT_LOCALNAME = "besoksadresse";
	public static final String PLUGIN_NAME = "NavOrgenhetBesoksadressePlugin";

	private OrganisasjonsenhetConsumer norg2Consumer;
	private Norg2Mapper norg2Mapper;

	public NavOrgenhetBesoksadressePlugin() {
		super(Besoksadresse.class);
	}

	@Autowired
	public NavOrgenhetBesoksadressePlugin(OrganisasjonsenhetConsumer norg2Consumer,
										  Norg2Mapper norg2Mapper) {
		super(Besoksadresse.class);
		this.norg2Consumer = norg2Consumer;
		this.norg2Mapper = norg2Mapper;
	}

	@Override
	public Node processElement(Node content, Map<String, Object> valueMap, String tema) {
		String dokumenttypeId = (String) valueMap.get(DOKUMENTTYPEID.name());

		validateElementType(content);

		try {
			Besoksadresse adresse = unmarshal(content);

			log.info("Henter NavOrgenhet info. DokumentTypeId={}, EnhetsId={}", dokumenttypeId, adresse.getEnhetsId());

			//Skal elementet berikes?
			if (adresse.isBerik()) {
				validateAdresse(adresse);

				EnhetNavn enhetNavn = norg2Consumer.hentEnhetNavn(adresse.getEnhetsId());
				EnhetKontaktinformasjon kontaktinformasjon = norg2Consumer.hentEnhetKontaktinformasjon(adresse.getEnhetsId());

				norg2Mapper.mapBesokadresse(enhetNavn, kontaktinformasjon, adresse);
			}

			Document newNode = convertObjectToDocument(adresse);
			Element documentElement = newNode.getDocumentElement();

			log.info("NavOrgenhet er beriket med data. DokumentTypeId={}, EnhetsId={}", dokumenttypeId, adresse.getEnhetsId());
			return newNode.renameNode(documentElement, content.getNamespaceURI(), content.getLocalName());

		} catch (ParserConfigurationException | MarshallerException e) {
			throw new RegOppslagTechnicalException(format("Feil i %s med feilmelding=%s", PLUGIN_NAME, e.getMessage()), e);
		} finally {
			clearSecurityContext();
		}
	}

	private void validateAdresse(Besoksadresse adresse) {
		if (isEmpty(adresse.getEnhetsId())) {
			throw new RegoppslagIllegalArgumentException(format("Feil i %s med feilmelding=EnhetsId mangler.", PLUGIN_NAME), BAD_REQUEST);
		}
	}

	private void validateElementType(Node element) {
		if (!ELEMENT_LOCALNAME.equals(element.getLocalName())) {
			throw new RegoppslagIllegalArgumentException("Unexpected element. Expected " + ELEMENT_LOCALNAME
					+ ". Found {" + element.getNamespaceURI() + "}" + element.getLocalName(), BAD_REQUEST);
		}
	}
}
