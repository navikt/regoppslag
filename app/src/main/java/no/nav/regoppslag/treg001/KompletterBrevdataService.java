package no.nav.regoppslag.treg001;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.api.KompletterBrevdataRequest;
import no.nav.regoppslag.api.KompletterBrevdataResponse;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagIkkeFunnetException;
import no.nav.regoppslag.exceptions.RegOppslagParsingException;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;
import no.nav.regoppslag.exceptions.UkjentAdresseException;
import no.nav.regoppslag.exceptions.UkjentAdressePersonErDoed;
import no.nav.regoppslag.xmlenricher.ElementEnricher;
import no.nav.regoppslag.xmlenricher.exceptions.MarshallerTechnicalException;
import no.nav.regoppslag.xmlenricher.exceptions.MissingPluginException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
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

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.GONE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

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

	@Retryable(include = MarshallerTechnicalException.class, backoff = @Backoff(delay = 500, multiplier = 3))
	public KompletterBrevdataResponse hentBrevdataFraRegistre(KompletterBrevdataRequest request) throws RegOppslagSecurityException {
		String responseBrevdata;
		try {
			Document brevdata = stringToDocument(request.getBrevdata());
			Document brevdataUtfylt = elementEnricher.process(brevdata, request.getDokumentTypeId());

			responseBrevdata = documentToString(brevdataUtfylt);
		} catch (MarshallerTechnicalException e) {
			//Hindre at RegOppslagTechnicalException ikke catcher og ikke logg fordi retryInterceptor logger feilen
			throw e;
		} catch (ParserConfigurationException | IOException | TransformerConfigurationException | MissingPluginException e) {
			log.error("Teknisk feil ved parsing av brevdata: " + e.getMessage(), e);
			throw new RegOppslagTechnicalException(e, "Teknisk feil ved parsing av brevdata");
		} catch (SAXException | XPathExpressionException | TransformerException e) {
			log.warn("Feil ved parsing av brevdata: " + e.getMessage(), e);
			throw new RegOppslagParsingException("Feil ved parsing av brevdata. " + e.getMessage(), e, BAD_REQUEST);
		} catch (RegOppslagIkkeFunnetException | RegoppslagIllegalArgumentException
				| UkjentAdresseException | UkjentAdressePersonErDoed e) {
			log.warn("TREG001 Funksjonell feil: " + e.getMessage());
			if (GONE.equals(e.getHttpStatus())) {
				log.error("TREG001 funksjonell feil : {}", e.getMessage());
				throw new UkjentAdressePersonErDoed(e.getLocalizedMessage(), e, "TREG001", e.getHttpStatus());
			} else if (NOT_FOUND.equals(e.getHttpStatus())) {
				throw new RegOppslagIkkeFunnetException(String.format("Funksjonell feil: dokumenttypeId=%s feilmelding=%s", request.getDokumentTypeId(), e
						.getMessage()), e, e.getMetricMessage(), e.getHttpStatus());
			} else {
				throw new RegoppslagIllegalArgumentException(String.format("Funksjonell feil: dokumenttypeId=%s feilmelding=%s", request.getDokumentTypeId(), e
						.getMessage()), e, e.getMetricMessage(), e.getHttpStatus());
			}
		} catch (RegOppslagSecurityException e) {
			log.warn("TREG001 Sikkerhetsfeil: " + e.getMessage());
			throw new RegOppslagSecurityException(String.format("Sikkerhetsfeil: dokumenttypeId=%s feilmelding=%s", request.getDokumentTypeId(), e
					.getMessage()), e.getShortDescription());
		}
		return KompletterBrevdataResponse.builder().brevdata(responseBrevdata).build();

	}
}
