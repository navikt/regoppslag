package no.nav.regoppslag;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import no.nav.regoppslag.config.AppVersion;
import no.nav.regoppslag.config.RestConsumerConfig;
import no.nav.regoppslag.config.TomcatConfig;
import no.nav.regoppslag.config.cxf.OrganisasjonEnhetKontaktinformasjonV1EndpointConfig;
import no.nav.regoppslag.config.cxf.OrganisasjonV4EndpointConfig;
import no.nav.regoppslag.config.fasit.DokumenttypeInfoV3Alias;
import no.nav.regoppslag.config.fasit.OrganisasjonEnhetKontaktinformasjonV1Alias;
import no.nav.regoppslag.config.fasit.OrganisasjonV4Alias;
import no.nav.regoppslag.config.fasit.ServiceuserAlias;
import no.nav.regoppslag.consumer.norg2.OrganisasjonEnhetKontaktinformasjonV1Consumer;
import no.nav.regoppslag.consumer.organisasjonv4.OrganisasjonV4Consumer;
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
		OrganisasjonV4Alias.class,
		OrganisasjonEnhetKontaktinformasjonV1Alias.class,
		DokumenttypeInfoV3Alias.class,
		ServiceuserAlias.class
})
@Import({TomcatConfig.class,
		OrganisasjonV4Consumer.class,
		OrganisasjonEnhetKontaktinformasjonV1Consumer.class,
		OrganisasjonEnhetKontaktinformasjonV1EndpointConfig.class,
		OrganisasjonV4EndpointConfig.class,
		ElementEnricherConfig.class,
		RestConsumerConfig.class,
		AppVersion.class})
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
