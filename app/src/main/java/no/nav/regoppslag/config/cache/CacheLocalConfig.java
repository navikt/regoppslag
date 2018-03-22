package no.nav.regoppslag.config.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
@Configuration
@Profile("local")
public class CacheLocalConfig {
	
	@Bean
	public CacheManager cacheManager() {
		return new NoOpCacheManager();
	}
	
}
