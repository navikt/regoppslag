package no.nav.regoppslag.nais;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.config.fasit.ServiceuserAlias;
import org.apache.cxf.Bus;
import org.apache.cxf.ws.security.trust.STSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

@Component
@Slf4j
public class NaisCheckSTSTokenRetriever {
	
	public static final String STS_CACHE_NAME = "STS_CACHE_NAME";

	private final STSClient stsClient;

	@Inject
	public NaisCheckSTSTokenRetriever(@Value("${securityTokenService.url}") String stsUrl, ServiceuserAlias serviceuserAlias, Bus cxf) {
		this.stsClient = NaisCheckSTSConfigUtil.configureStsRequestSamlToken(stsUrl, serviceuserAlias.getUsername(), serviceuserAlias.getPassword(), cxf);
	}

	@Cacheable(value = STS_CACHE_NAME, key="#root.methodName")
	public String requestStsToken() throws Exception {
		log.info("Henter SAML security token fra STS for bruk i NAIS isReady check");
		return elementToString(stsClient.requestSecurityToken().getToken());
	}

	private String elementToString(Element element) {
		try {
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(element);
			StreamResult result = new StreamResult(new StringWriter());
			transformer.transform(source, result);
			return result.getWriter().toString();
		} catch (TransformerException e) {
			throw new RuntimeException(String.format("Exception when converting Element to String. errorMsg=%s", e
					.getMessage()));
		}
	}
}