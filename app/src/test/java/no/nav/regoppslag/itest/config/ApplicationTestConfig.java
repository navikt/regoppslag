package no.nav.regoppslag.itest.config;

import no.nav.regoppslag.config.CacheTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Configuration
@Import({CacheTestConfig.class, RestTemplateTestConfig.class})
@Profile("itest")
public class ApplicationTestConfig {
}
