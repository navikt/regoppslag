package no.nav.regoppslag.nais.naiscontract.support;

import no.nav.regoppslag.config.fasit.ServiceuserAlias;
import org.apache.cxf.Bus;
import org.apache.cxf.ws.security.trust.STSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class SelftestSTSConfig {
	
	public static final String STS_CACHE_NAME = "STS_CACHE_NAME";
	
	@Value("${securityTokenService.url}")
	private String stsUrl;

	@Inject
	private ServiceuserAlias serviceuserAlias;

	@Bean
	public STSClient stsClient(Bus cxf) {
		return NaisContractSTSConfigUtil.configureStsRequestSamlToken(stsUrl, serviceuserAlias.getUsername(), serviceuserAlias.getPassword(), cxf);

	}
}