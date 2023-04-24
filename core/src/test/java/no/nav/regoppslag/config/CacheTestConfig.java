package no.nav.regoppslag.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.NoOpCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static no.nav.regoppslag.config.cache.CacheConfig.HENT_DOKMET_SPRAAKINFO;
import static no.nav.regoppslag.config.cache.CacheConfig.HENT_ENHET_KONTAKTINFO;
import static no.nav.regoppslag.config.cache.CacheConfig.HENT_ENHET_NAVN;
import static no.nav.regoppslag.config.cache.CacheConfig.HENT_ORGANISASJON;
import static no.nav.regoppslag.config.cache.CacheConfig.HENT_PERSON;
import static no.nav.regoppslag.config.cache.CacheConfig.RESTSTS_CACHE_NAME;
import static no.nav.regoppslag.consumer.azure.AzureAdGraphService.HENT_FULLT_NAVN;


@Profile("itest")
@Configuration
@EnableCaching
public class CacheTestConfig {
	static final Duration DEFAULT_CACHE_EXPIRATION_TIME = Duration.ofDays(2L);
	static final Duration HENT_PERSON_CACHE_EXPIRATION_TIME = Duration.ofSeconds(10L);
	static final Duration STS_CACHE_EXPIRATION_TIME = Duration.ofMinutes(50L);

	@Bean
	public LettuceConnectionFactory lettuceConnectionFactory() {
		return new LettuceConnectionFactory();
	}

	@Bean
	public CacheManager cacheManager() {

		SimpleCacheManager cacheManager = new SimpleCacheManager();
		CaffeineCache cacheHentFulltNavn = new CaffeineCache(HENT_FULLT_NAVN, Caffeine.newBuilder()
				.expireAfterAccess(2, TimeUnit.DAYS)
				.maximumSize(2000)
				.build());
		cacheManager.setCaches(Arrays.asList(cacheHentFulltNavn,
				new NoOpCache(HENT_ENHET_NAVN),
				new NoOpCache(HENT_PERSON),
				new NoOpCache(HENT_ORGANISASJON),
				new NoOpCache(HENT_DOKMET_SPRAAKINFO),
				new CaffeineCache(HENT_FULLT_NAVN, Caffeine.newBuilder()
						.expireAfterWrite(DEFAULT_CACHE_EXPIRATION_TIME.getSeconds(), TimeUnit.SECONDS)
						.build()),
				new CaffeineCache(HENT_ENHET_NAVN, Caffeine.newBuilder()
						.expireAfterWrite(DEFAULT_CACHE_EXPIRATION_TIME.getSeconds(), TimeUnit.SECONDS)
						.build()),
				new CaffeineCache(HENT_ENHET_KONTAKTINFO, Caffeine.newBuilder()
						.expireAfterWrite(DEFAULT_CACHE_EXPIRATION_TIME.getSeconds(), TimeUnit.SECONDS)
						.build()),
				new CaffeineCache(HENT_PERSON, Caffeine.newBuilder()
						.expireAfterWrite(HENT_PERSON_CACHE_EXPIRATION_TIME.getSeconds(), TimeUnit.SECONDS)
						.build()),
				new CaffeineCache(HENT_ORGANISASJON, Caffeine.newBuilder()
						.expireAfterWrite(DEFAULT_CACHE_EXPIRATION_TIME.getSeconds(), TimeUnit.SECONDS)
						.build()),
				new CaffeineCache(HENT_DOKMET_SPRAAKINFO, Caffeine.newBuilder()
						.expireAfterWrite(DEFAULT_CACHE_EXPIRATION_TIME.getSeconds(), TimeUnit.SECONDS)
						.build()),
				new CaffeineCache(RESTSTS_CACHE_NAME, Caffeine.newBuilder()
						.expireAfterWrite(STS_CACHE_EXPIRATION_TIME.getSeconds(), TimeUnit.SECONDS)
						.build())));
		return cacheManager;

	}
}
