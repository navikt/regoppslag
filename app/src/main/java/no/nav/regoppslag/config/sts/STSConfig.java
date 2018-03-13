package no.nav.regoppslag.config.sts;

import no.nav.regoppslag.config.fasit.ServiceuserAlias;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
public class STSConfig {
	
	@Value("${securityTokenService.url}")
	private String stsUrl;
	
	@Inject
	private ServiceuserAlias serviceuserAlias;
	
	public void configureSTS(Object port){
		Client client = ClientProxy.getClient(port);
		STSConfigUtil.configureStsRequestSamlToken(client, stsUrl, serviceuserAlias.getUsername(), serviceuserAlias.getPassword());
	}
	
}
