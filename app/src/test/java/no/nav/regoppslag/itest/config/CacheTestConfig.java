package no.nav.regoppslag.itest.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static no.nav.regoppslag.config.cache.LocalCacheConfig.RESTSTS_CACHE_NAME;
import static no.nav.regoppslag.consumer.ldap.LdapAdeoUserLookup.HENT_FULLT_NAVN;
import static no.nav.regoppslag.consumer.norg2.OrganisasjonEnhetKontaktinformasjonV1Consumer.HENT_ENHET_NAVN;
import static no.nav.regoppslag.metrics.MetricLabels.HENT_DOKKAT_SPRAAKINFO;
import static no.nav.regoppslag.metrics.MetricLabels.HENT_ORGANISASJON;
import static no.nav.regoppslag.metrics.MetricLabels.HENT_PERSON;
import static no.nav.regoppslag.nais.NaisCheckSTSTokenRetriever.STS_CACHE_NAME;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */

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
		cacheManager.setCaches(Arrays.asList(
				new CaffeineCache(HENT_FULLT_NAVN, Caffeine.newBuilder()
						.expireAfterWrite(DEFAULT_CACHE_EXPIRATION_TIME.getSeconds(), TimeUnit.SECONDS)
						.build()),
				new CaffeineCache(HENT_ENHET_NAVN, Caffeine.newBuilder()
						.expireAfterWrite(DEFAULT_CACHE_EXPIRATION_TIME.getSeconds(), TimeUnit.SECONDS)
						.build()),
				new CaffeineCache(HENT_PERSON, Caffeine.newBuilder()
						.expireAfterWrite(HENT_PERSON_CACHE_EXPIRATION_TIME.getSeconds(), TimeUnit.SECONDS)
						.build()),
				new CaffeineCache(HENT_ORGANISASJON, Caffeine.newBuilder()
						.expireAfterWrite(DEFAULT_CACHE_EXPIRATION_TIME.getSeconds(), TimeUnit.SECONDS)
						.build()),
				new CaffeineCache(HENT_DOKKAT_SPRAAKINFO, Caffeine.newBuilder()
						.expireAfterWrite(DEFAULT_CACHE_EXPIRATION_TIME.getSeconds(), TimeUnit.SECONDS)
						.build()),
				new CaffeineCache(STS_CACHE_NAME, Caffeine.newBuilder()
						.expireAfterWrite(STS_CACHE_EXPIRATION_TIME.getSeconds(), TimeUnit.SECONDS)
						.build()),
				new CaffeineCache(RESTSTS_CACHE_NAME, Caffeine.newBuilder()
						.expireAfterWrite(STS_CACHE_EXPIRATION_TIME.getSeconds(), TimeUnit.SECONDS)
						.build())));
		return cacheManager;

	}
}
