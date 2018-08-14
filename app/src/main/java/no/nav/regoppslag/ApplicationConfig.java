package no.nav.regoppslag;

import io.prometheus.client.spring.boot.EnablePrometheusEndpoint;
import io.prometheus.client.spring.boot.EnableSpringBootMetricsCollector;
import io.prometheus.client.spring.web.EnablePrometheusTiming;
import no.nav.regoppslag.config.ElementEnricherConfig;
import no.nav.regoppslag.config.RestConsumerConfig;
import no.nav.regoppslag.config.cxf.OrganisasjonEnhetKontaktinformasjonV1EndpointConfig;
import no.nav.regoppslag.config.cxf.OrganisasjonV4EndpointConfig;
import no.nav.regoppslag.config.cxf.PersonV3EndpointConfig;
import no.nav.regoppslag.config.fasit.DokumenttypeInfoV3Alias;
import no.nav.regoppslag.config.fasit.OrganisasjonEnhetKontaktinformasjonV1Alias;
import no.nav.regoppslag.config.fasit.OrganisasjonV4Alias;
import no.nav.regoppslag.config.fasit.PersonV3Alias;
import no.nav.regoppslag.config.fasit.ServiceuserAlias;
import no.nav.regoppslag.consumer.norg2.OrganisasjonEnhetKontaktinformasjonV1Consumer;
import no.nav.regoppslag.consumer.organisasjonv4.OrganisasjonV4Consumer;
import no.nav.regoppslag.consumer.personv3.PersonV3Consumer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.retry.annotation.EnableRetry;

@EnableConfigurationProperties({
		OrganisasjonV4Alias.class,
		PersonV3Alias.class,
		OrganisasjonEnhetKontaktinformasjonV1Alias.class,
		DokumenttypeInfoV3Alias.class,
		ServiceuserAlias.class
})
@Import({
		PersonV3Consumer.class,
		OrganisasjonV4Consumer.class,
		OrganisasjonEnhetKontaktinformasjonV1Consumer.class,
		OrganisasjonEnhetKontaktinformasjonV1EndpointConfig.class,
		OrganisasjonV4EndpointConfig.class,
		PersonV3EndpointConfig.class,
		ElementEnricherConfig.class,
		RestConsumerConfig.class})
@EnablePrometheusEndpoint
@EnablePrometheusTiming
@EnableSpringBootMetricsCollector
@EnableRetry
@Configuration
public class ApplicationConfig {
}
