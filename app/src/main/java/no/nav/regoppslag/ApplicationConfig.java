package no.nav.regoppslag;

import io.prometheus.client.spring.boot.EnablePrometheusEndpoint;
import io.prometheus.client.spring.boot.EnableSpringBootMetricsCollector;
import io.prometheus.client.spring.web.EnablePrometheusTiming;
import no.nav.regoppslag.config.OrchestratorConfig;
import no.nav.regoppslag.config.cxf.OrganisasjonEnhetKontaktinformasjonV1EndpointConfig;
import no.nav.regoppslag.config.cxf.OrganisasjonV4EndpointConfig;
import no.nav.regoppslag.config.cxf.PersonV3EndpointConfig;
import no.nav.regoppslag.config.fasit.NavAppCertAlias;
import no.nav.regoppslag.config.fasit.OrganisasjonEnhetKontaktinformasjonV1Alias;
import no.nav.regoppslag.config.fasit.OrganisasjonV4Alias;
import no.nav.regoppslag.config.fasit.PersonV3Alias;
import no.nav.regoppslag.consumer.norg2.OrganisasjonEnhetKontaktinformasjonV1Consumer;
import no.nav.regoppslag.consumer.organisasjonv4.OrganisasjonV4Consumer;
import no.nav.regoppslag.consumer.personv3.PersonV3Consumer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@EnableConfigurationProperties({
		OrganisasjonV4Alias.class,
		PersonV3Alias.class,
		OrganisasjonEnhetKontaktinformasjonV1Alias.class,
		NavAppCertAlias.class
})
@Import({
		PersonV3Consumer.class,
		OrganisasjonV4Consumer.class,
		OrganisasjonEnhetKontaktinformasjonV1Consumer.class,
		OrganisasjonEnhetKontaktinformasjonV1EndpointConfig.class,
		OrganisasjonV4EndpointConfig.class,
		PersonV3EndpointConfig.class,
		OrchestratorConfig.class})
@EnablePrometheusEndpoint
@EnablePrometheusTiming
@EnableSpringBootMetricsCollector
@Configuration
public class ApplicationConfig {
}
