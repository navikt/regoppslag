package no.nav.regoppslag;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import no.nav.regoppslag.config.RestConsumerConfig;
import no.nav.regoppslag.config.WebClientConfig;
import no.nav.regoppslag.config.properties.RegoppslagProperties;
import no.nav.regoppslag.consumer.azure.AzureProperties;
import no.nav.regoppslag.metrics.DokTimedAspect;
import no.nav.regoppslag.treg001.ElementEnricherConfig;
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableConfigurationProperties({
		RegoppslagProperties.class,
		AzureProperties.class
})
@Import({
		ElementEnricherConfig.class,
		RestConsumerConfig.class,
		WebClientConfig.class
})
@EnableRetry
@Configuration
@EnableAspectJAutoProxy
@EnableAutoConfiguration
@EnableWebMvc
@EnableJwtTokenValidation(ignore = {"org.springframework", "org.springdoc"})
public class ApplicationConfig {

	@Bean
	public DokTimedAspect timedAspect(MeterRegistry meterRegistry) {
		return new DokTimedAspect(meterRegistry);
	}

	@Bean
	JvmThreadMetrics threadMetrics() {
		return new JvmThreadMetrics();
	}

}
