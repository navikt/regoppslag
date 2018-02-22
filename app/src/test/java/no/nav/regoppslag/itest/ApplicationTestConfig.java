package no.nav.regoppslag.itest;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Configuration
public class ApplicationTestConfig {

	@Bean
	public ProducerTemplate producerTemplate(CamelContext camelContext) {
		return camelContext.createProducerTemplate();
	}

}