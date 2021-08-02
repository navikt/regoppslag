package no.nav.regoppslag.config.security;

import static no.nav.regoppslag.config.security.SamlTokenUtils.getSamlAssertionWrapperFromContext;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.exceptions.SamlTokenInterceptorException;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.headers.Header;
import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.ws.security.wss4j.SamlTokenInterceptor;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.common.saml.SamlAssertionWrapper;
import org.apache.wss4j.dom.engine.WSSConfig;
import org.springframework.http.HttpStatus;
import org.w3c.dom.Element;

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
				log.warn(msg);
				throw new SamlTokenInterceptorException(msg);
			}
			
			Element el = (Element) h.getObject();
			el = (Element) DOMUtils.getDomElement(el);
			el.appendChild(wrapper.toDOM(el.getOwnerDocument()));
			
		} catch (WSSecurityException e) {
			log.warn(String.format("Feilet ved komplettering av SAML assertion token til SOAP meldingen. Det kan hende tokenet er i feil format. Feilmelding=%s", e
					.getMessage()), e);
			throw new SamlTokenInterceptorException("Feilet ved komplettering av SAML assertion token fra header til SOAP meldingen. Det kan hende tokenet er i feil format");
		}
		
	}
	
	
}
