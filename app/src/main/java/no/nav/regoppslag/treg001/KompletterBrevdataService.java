package no.nav.regoppslag.treg001;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.common.ValiderOgKompletterBrevdataRequest;
import no.nav.regoppslag.common.ValiderOgKompletterBrevdataResponse;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.xmlenricher.exceptions.MissingPluginException;
import no.nav.regoppslag.xmlenricher.exceptions.MultiExceptionHolder;
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
	
	private Orchestrator orchestrator;
	
	@Inject
	public KompletterBrevdataService(Orchestrator orchestrator) {
		this.orchestrator = orchestrator;
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
	
	public ValiderOgKompletterBrevdataResponse hentBrevdataFraRegistre(ValiderOgKompletterBrevdataRequest request) throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		
		String responseBrevdata = null;
		try {
			Document brevdata = stringToDocument(request.getBrevdata());
			Document brevdataUtfylt = orchestrator.process(brevdata, request.getDokumentTypeId());
			responseBrevdata = documentToString(brevdataUtfylt);
		} catch (ParserConfigurationException | IOException | TransformerConfigurationException | MissingPluginException e) {
			log.error(e.getMessage(), e);
			throw new RegOppslagTechnicalException(e);
		} catch (SAXException | XPathExpressionException | TransformerException e) {
			log.error(e.getMessage(), e);
			throw new RegOppslagFunctionalException(e);
		} catch (MultiExceptionHolder t) {
			logExceptions(t);
			if (t.hasFunctionExceptions()) {
				throw new RegOppslagFunctionalException(String.format("Funksjonell feil: dokumenttypeId=%s feilmelding=%s", request.getDokumentTypeId(), t.report()));
			} else {
				throw new RegOppslagTechnicalException(String.format("Teknisk feil: dokumenttypeId=%s description=%s",request.getDokumentTypeId(), t.report()));
			}
		}
		return ValiderOgKompletterBrevdataResponse.builder().brevdata(responseBrevdata).build();
		
	}
	
	private void logExceptions(MultiExceptionHolder t) {
		t.getUnhandledErrors().stream().forEach(error -> {
			if (error instanceof RegOppslagFunctionalException) {
				log.warn(error.getMessage(),error);
			} else {
				log.error(error.getMessage(),error);
			}   });
	}
}
