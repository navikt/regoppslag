package no.nav.regoppslag.nais;

import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.Bus;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.cxf.ws.security.trust.STSClient;

import java.util.HashMap;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Slf4j
public class NaisCheckSTSConfigUtil {

	private static final String STS_CLIENT_AUTHENTICATION_POLICY = "classpath:policy/untPolicy.xml";
	
	public static STSClient configureStsRequestSamlToken(String stsUrl, String username, String password, Bus bus) {
		STSClient stsClient = new STSClient(bus);
		configureSTSClient(stsClient, stsUrl, username, password);
		return stsClient;
	}

	protected static STSClient configureSTSClient(STSClient stsClient, String location, String username, String password) {

		stsClient.setEnableAppliesTo(false);
		stsClient.setAllowRenewing(false);
		stsClient.setLocation(location);
		stsClient.setTokenType("http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0");
		stsClient.setKeyType("http://docs.oasis-open.org/ws-sx/ws-trust/200512/Bearer");

		HashMap<String, Object> properties = new HashMap<>();
		properties.put(SecurityConstants.USERNAME, username);
		properties.put(SecurityConstants.PASSWORD, password);
		stsClient.setProperties(properties);

		//used for the STS client to authenticate itself to the STS provider.
		stsClient.setPolicy(STS_CLIENT_AUTHENTICATION_POLICY);

		return stsClient;
	}

}
