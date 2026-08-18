package no.nav.regoppslag.config.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;

import static java.util.concurrent.TimeUnit.SECONDS;
import static no.nav.regoppslag.config.cache.CacheConfig.DEFAULT_CACHE_EXPIRATION_TIME;
import static no.nav.regoppslag.config.cache.CacheConfig.HENT_PERSON_CACHE_EXPIRATION_TIME;
import static no.nav.regoppslag.config.cache.CacheConfig.STS_CACHE_EXPIRATION_TIME;
import static no.nav.regoppslag.config.cache.CacheConfig.HENT_NAV_ANSATT_NAVN;

/// Cachemanager for bruk ved lokalt kjøring av applikasjonen.
@Profile("local")
@Configuration
@EnableCaching
public class LocalCacheConfig {

	@Bean
	@Primary
	public CacheManager cacheManager() {

		SimpleCacheManager cacheManager = new SimpleCacheManager();
		cacheManager.setCaches(Arrays.asList(
				new CaffeineCache(HENT_NAV_ANSATT_NAVN, Caffeine.newBuilder()
						.expireAfterWrite(DEFAULT_CACHE_EXPIRATION_TIME.getSeconds(), SECONDS)
						.build()),
				new CaffeineCache(CacheConfig.HENT_ENHET_NAVN, Caffeine.newBuilder()
						.expireAfterWrite(DEFAULT_CACHE_EXPIRATION_TIME.getSeconds(), SECONDS)
						.build()),
				new CaffeineCache(CacheConfig.HENT_ENHET_KONTAKTINFO, Caffeine.newBuilder()
						.expireAfterWrite(DEFAULT_CACHE_EXPIRATION_TIME.getSeconds(), SECONDS)
						.build()),
				new CaffeineCache(CacheConfig.HENT_BRUKER_PERSONDATA, Caffeine.newBuilder()
						.expireAfterWrite(HENT_PERSON_CACHE_EXPIRATION_TIME.getSeconds(), SECONDS)
						.build()),
				new CaffeineCache(CacheConfig.HENT_ORGANISASJON, Caffeine.newBuilder()
						.expireAfterWrite(DEFAULT_CACHE_EXPIRATION_TIME.getSeconds(), SECONDS)
						.build()),
				new CaffeineCache(CacheConfig.HENT_DOKMET_SPRAAKINFO, Caffeine.newBuilder()
						.expireAfterWrite(DEFAULT_CACHE_EXPIRATION_TIME.getSeconds(), SECONDS)
						.build()),
				new CaffeineCache(CacheConfig.AZURE_CLIENT_CREDENTIAL_TOKEN, Caffeine.newBuilder()
						.expireAfterWrite(STS_CACHE_EXPIRATION_TIME.getSeconds(), SECONDS)
						.build())));
		return cacheManager;
	}

}
