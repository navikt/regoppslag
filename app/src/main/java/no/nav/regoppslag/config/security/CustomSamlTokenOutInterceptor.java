package no.nav.regoppslag.config.security;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.exceptions.SamlTokenInterceptorException;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.headers.Header;
import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.ws.security.wss4j.SamlTokenInterceptor;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.common.saml.SamlAssertionWrapper;
import org.apache.wss4j.dom.engine.WSSConfig;
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
public class CustomSamlTokenOutInterceptor extends SamlTokenInterceptor {
	
	@Override
	protected void addToken(SoapMessage message) {
		WSSConfig.init();
		Header h = findSecurityHeader(message, true);
		
		try {
			SamlAssertionWrapper wrapper = getSamlAssertionWrapperFromContext();
			
			if (wrapper == null) {
				String msg = "Fant ingen SAML assertion token i sikkerhetskontekst. SAML assertion token kreves for å kunne kalle PersonV3";
				log.error(msg);
				throw new SamlTokenInterceptorException(msg);
			}
			
			Element el = (Element) h.getObject();
			el = (Element) DOMUtils.getDomElement(el);
			el.appendChild(wrapper.toDOM(el.getOwnerDocument()));
			
		} catch (WSSecurityException ex) {
			SecurityContextHolder.clearContext();
			log.error(String.format("Feilet ved komplettering av SAML assertion token til SOAP meldingen. Feilmelding=%s", ex.getMessage()));
			throw new SamlTokenInterceptorException("Feilet ved komplettering av SAML assertion token fra header til SOAP meldingen. Det kan hende tokenet er i feil format");
		} finally {
			SecurityContextHolder.clearContext();
		}
		
	}
	
	private SamlAssertionWrapper getSamlAssertionWrapperFromContext() {
		
		if (SecurityContextHolder.getContext().getAuthentication() == null) {
			return null;
		}
		
		String credentials = (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();
		
		if (StringUtils.isEmpty(credentials)) {
			return null;
		}
		
		Element element = samlTokenToElement(credentials);
		return elementToSamlAssertionWrapper(element);
	}
	
	private Element samlTokenToElement(String decodedSaml) {
		InputStream is = new ByteArrayInputStream(decodedSaml.getBytes());
		Document doc;
		
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(is, StandardCharsets.UTF_8.name());
		} catch (ParserConfigurationException | IOException | SAXException e) {
			SecurityContextHolder.clearContext();
			log.error(String.format("Feil ved parsing av SAML assertion token. Feilmelding=%s", e.getMessage()));
			throw new SamlTokenInterceptorException("Feil ved parsing av SAML assertion token. Det kan hende tokenet er i feil format");
		}
		
		return doc.getDocumentElement();
	}
	
	private SamlAssertionWrapper elementToSamlAssertionWrapper(Element token) {
		
		try {
			return new SamlAssertionWrapper(token);
		} catch (WSSecurityException e) {
			SecurityContextHolder.clearContext();
			log.error(String.format("Feilet ved parsing av SAML assertion element til SamlAssertionWrapper. Feilmelding=%s", e.getMessage()));
			throw new SamlTokenInterceptorException("Feilet ved parsing av SAML assertion token. Det kan hende tokenet er i feil format");
		}
		
	}
	
	
}
