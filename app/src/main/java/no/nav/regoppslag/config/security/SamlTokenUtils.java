package no.nav.regoppslag.config.security;

import static no.nav.regoppslag.metrics.PrometheusMetrics.getConsumerId;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestCounter;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.exceptions.SamlTokenInterceptorException;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.common.saml.SamlAssertionWrapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Slf4j
public class SamlTokenUtils {
	
	public static SamlAssertionWrapper getSamlAssertionWrapperFromContext() {
		
		if (SecurityContextHolder.getContext().getAuthentication() == null || !SecurityContextHolder.getContext()
				.getAuthentication()
				.isAuthenticated()) {
			
			//TODO: Testkode, fjern senere ved bekreftelse av riktig oppførsel
			if (SecurityContextHolder.getContext().getAuthentication() != null && !SecurityContextHolder.getContext()
					.getAuthentication()
					.isAuthenticated()) {
				log.error("Sikkerhetstokenet i securityContext er ugyldig fordi den har allerede blitt brukt. " +
						"Dette er noe som ikke bør skje! (feil i kode?).");
				requestCounter.labels("SAML_TOKEN_OUT_INTERCEPTOR", "SAML_TOKEN_OUT_INTERCEPTOR", "SAML_NOT_ALLOWED", getConsumerId(), "SAML_AUTHENTICATION_FALSE")
						.inc();
				
			}
			
			return null;
		}
		
		//Securitytoken can only be used once
		SecurityContextHolder.getContext().getAuthentication().setAuthenticated(false);
		
		String credentials = (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();
		
		if (StringUtils.isEmpty(credentials)) {
			return null;
		}
		
		Element element = samlTokenToElement(credentials);
		return elementToSamlAssertionWrapper(element);
	}
	
	public static Element samlTokenToElement(String decodedSaml) {
		InputStream is = new ByteArrayInputStream(decodedSaml.getBytes());
		Document doc;
		
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(is, StandardCharsets.UTF_8.name());
		} catch (ParserConfigurationException | IOException | SAXException e) {
			log.error(String.format("Feil ved parsing av SAML assertion token. Feilmelding=%s", e.getMessage()), e);
			throw new SamlTokenInterceptorException("Feil ved parsing av SAML assertion token. Det kan hende tokenet er i feil format");
		}
		
		return doc.getDocumentElement();
	}
	
	public static SamlAssertionWrapper elementToSamlAssertionWrapper(Element token) {
		
		try {
			return new SamlAssertionWrapper(token);
		} catch (WSSecurityException e) {
			log.error(String.format("Feilet ved parsing av SAML assertion element til SamlAssertionWrapper. Feilmelding=%s", e.getMessage()), e);
			throw new SamlTokenInterceptorException("Feilet ved parsing av SAML assertion token. Det kan hende tokenet er i feil format");
		}
		
	}
	
}
