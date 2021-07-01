package no.nav.regoppslag;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import no.nav.regoppslag.config.AppVersion;
import no.nav.regoppslag.config.ElementEnricherConfig;
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
import no.nav.regoppslag.service.PostnummerService;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.retry.annotation.EnableRetry;

@EnableConfigurationProperties({
		OrganisasjonV4Alias.class,
		OrganisasjonEnhetKontaktinformasjonV1Alias.class,
		DokumenttypeInfoV3Alias.class,
		ServiceuserAlias.class
})
@Import({TomcatConfig.class,
		PostnummerService.class,
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
public class ApplicationConfig {

	@Bean
	public DokTimedAspect timedAspect(MeterRegistry meterRegistry) {
		return new DokTimedAspect(meterRegistry);
	}

	@Bean
	JvmThreadMetrics threadMetrics(){
		return new JvmThreadMetrics();
	}

}
