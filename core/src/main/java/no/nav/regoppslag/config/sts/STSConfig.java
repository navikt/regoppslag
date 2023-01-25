package no.nav.regoppslag.config.sts;

import no.nav.regoppslag.config.fasit.ServiceuserAlias;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("nais")
public class STSConfig {
	
	@Value("${securityTokenService.url}")
	private String stsUrl;
	
	@Autowired
	private ServiceuserAlias serviceuserAlias;
	
	public void configureSTS(Object port){
		Client client = ClientProxy.getClient(port);
		STSConfigUtil.configureStsRequestSamlToken(client, stsUrl, serviceuserAlias.getUsername(), serviceuserAlias.getPassword());
	}
	
}
