package no.nav.regoppslag.itest.config;

import no.nav.regoppslag.config.sts.STSConfig;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
@Profile("itest")
public class STSTestConfig extends STSConfig {
	
	@Override
	public void configureSTS(Object port){

	}
	
}
