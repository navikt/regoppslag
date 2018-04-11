package no.nav.regoppslag.treg001;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.common.ValiderOgKompletterBrevdataRequest;
import no.nav.regoppslag.common.ValiderOgKompletterBrevdataResponse;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.xmlenricher.ElementEnricher;
import no.nav.regoppslag.xmlenricher.exceptions.MissingPluginException;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
@Slf4j
@Service
public class KompletterBrevdataService {

	private final ElementEnricher elementEnricher;

	@Inject
	public KompletterBrevdataService(ElementEnricher elementEnricher) {
		this.elementEnricher = elementEnricher;
	}

	public static Document stringToDocument(String xml) throws ParserConfigurationException, IOException, SAXException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setNamespaceAware(true);
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		StringReader str = new StringReader(xml);
		return builder.parse(new InputSource(str));
	}

	public static String documentToString(Document xmlDocument) throws TransformerException {
		StringWriter writer = new StringWriter();
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.transform(new DOMSource(xmlDocument), new StreamResult(writer));
		return writer.toString();
	}
	
	public ValiderOgKompletterBrevdataResponse hentBrevdataFraRegistre(ValiderOgKompletterBrevdataRequest request) throws RegOppslagFunctionalException, RegOppslagTechnicalException, RegOppslagSecurityException {

		String responseBrevdata;
		try {
			Document brevdata = stringToDocument(request.getBrevdata());
			Document brevdataUtfylt = elementEnricher.process(brevdata, request.getDokumentTypeId());
			responseBrevdata = documentToString(brevdataUtfylt);
		} catch (ParserConfigurationException | IOException | TransformerConfigurationException | MissingPluginException e) {
			log.error(e.getMessage(), e);
			throw new RegOppslagTechnicalException(e);
		} catch (SAXException | XPathExpressionException | TransformerException e) {
			log.warn(e.getMessage(), e);
			throw new RegOppslagFunctionalException(e);
		} catch (RegOppslagFunctionalException t) {
			log.warn(t.getMessage(), t);
			throw new RegOppslagFunctionalException(String.format("Funksjonell feil: dokumenttypeId=%s feilmelding=%s", request.getDokumentTypeId(), t.getMessage()));
		} catch (RegOppslagTechnicalException t) {
			log.error(t.getMessage(), t);
			throw new RegOppslagTechnicalException(String.format("Teknisk feil: dokumenttypeId=%s description=%s", request.getDokumentTypeId(), t.getMessage()));
		} catch (RegOppslagSecurityException e) {
			throw new RegOppslagSecurityException(String.format("Sikkerhetsfeil: dokumenttypeId=%s feilmelding=%s", request.getDokumentTypeId(), e
					.getMessage()));
		}
		return ValiderOgKompletterBrevdataResponse.builder().brevdata(responseBrevdata).build();

	}
}
