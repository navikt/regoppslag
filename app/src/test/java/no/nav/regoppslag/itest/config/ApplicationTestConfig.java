package no.nav.regoppslag.itest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Configuration
@Import({CacheTestConfig.class, RestTemplateTestConfig.class})
@Profile("itest")
public class ApplicationTestConfig {
}
