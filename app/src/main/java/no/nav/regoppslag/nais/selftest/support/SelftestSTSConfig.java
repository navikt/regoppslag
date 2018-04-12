package no.nav.regoppslag.nais.selftest.support;

import static no.nav.regoppslag.nais.selftest.support.NaisContractSTSConfigUtil.configureStsRequestSamlToken;

import no.nav.regoppslag.config.fasit.ServiceuserAlias;
import org.apache.cxf.ws.security.trust.STSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class SelftestSTSConfig {

	@Value("${securityTokenService.url}")
	private String stsUrl;

	@Inject
	private ServiceuserAlias serviceuserAlias;

	@Bean
	public STSClient stsClient() {
		return configureStsRequestSamlToken(stsUrl, serviceuserAlias.getUsername(), serviceuserAlias.getPassword());

	}

}