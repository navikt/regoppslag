package no.nav.regoppslag;

import io.prometheus.client.spring.boot.EnablePrometheusEndpoint;
import io.prometheus.client.spring.boot.EnableSpringBootMetricsCollector;
import io.prometheus.client.spring.web.EnablePrometheusTiming;
import no.nav.regoppslag.config.cxf.OrganisasjonV4EndpointConfig;
import no.nav.regoppslag.config.cxf.PersonV3EndpointConfig;
import no.nav.regoppslag.config.fasit.NavAppCertAlias;
import no.nav.regoppslag.config.fasit.OrganisasjonV4Alias;
import no.nav.regoppslag.config.fasit.PersonV3Alias;
import no.nav.regoppslag.nais.checks.OrganisasjonV4Check;
import no.nav.regoppslag.organisasjonv4.OrganisasjonV4Consumer;
import no.nav.regoppslag.personv3.PersonV3Consumer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@EnableConfigurationProperties({
		OrganisasjonV4Alias.class,
		PersonV3Alias.class,
		NavAppCertAlias.class
})
@Import({
		PersonV3Consumer.class,
		OrganisasjonV4Consumer.class,
		OrganisasjonV4EndpointConfig.class,
		PersonV3EndpointConfig.class,
		OrganisasjonV4Check.class})
@EnablePrometheusEndpoint
@EnablePrometheusTiming
@EnableSpringBootMetricsCollector
@Configuration
public class ApplicationConfig {
}
