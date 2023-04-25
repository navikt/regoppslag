package no.nav.regoppslag.config.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Duration;
import java.util.Arrays;

@Profile("nais")
@Configuration
@EnableCaching
@Slf4j
public class CacheConfig extends CachingConfigurerSupport {

	public static final String HENT_ENHET_NAVN = "hentEnhetNavn";
	public static final String HENT_ENHET_KONTAKTINFO = "hentEnhetKontaktInfo";
	public static final String HENT_ORGANISASJON = "hentOrganisasjon";
	public static final String HENT_PERSON = "hentPerson";
	public static final String HENT_NAVN = "hentNavn";
	public static final String HENT_DOKMET_SPRAAKINFO = "hentDokumenttypeInfoSpraak";
	public static final String RESTSTS_CACHE_NAME = "RESTSTS_CACHE_NAME";
	public static final String AZURE_CLIENT_CREDENTIAL_TOKEN = "AzureClientCredentialToken";

	static final Duration DEFAULT_CACHE_EXPIRATION_TIME = Duration.ofDays(1L);
	static final Duration HENT_NAVN_CACHE_EXPIRATION_TIME = Duration.ofSeconds(30L);
	static final Duration HENT_PERSON_CACHE_EXPIRATION_TIME = Duration.ofSeconds(30L);
	static final Duration STS_CACHE_EXPIRATION_TIME = Duration.ofMinutes(50L);
	static final Duration AZURE_CLIENT_CREDENTIAL_TOKEN_EXPIRATION_TIME = Duration.ofMinutes(50L);

	@Bean
	public CacheManager inMemoryCacheManager() {
		SimpleCacheManager cacheManager = new SimpleCacheManager();
		cacheManager.setCaches(Arrays.asList(
				new CaffeineCache(HENT_ENHET_NAVN, Caffeine.newBuilder()
						.expireAfterWrite(DEFAULT_CACHE_EXPIRATION_TIME)
						.recordStats()
						.build()),
				new CaffeineCache(HENT_ENHET_KONTAKTINFO, Caffeine.newBuilder()
						.expireAfterWrite(DEFAULT_CACHE_EXPIRATION_TIME)
						.recordStats()
						.build()),
				new CaffeineCache(HENT_ORGANISASJON, Caffeine.newBuilder()
						.expireAfterWrite(DEFAULT_CACHE_EXPIRATION_TIME)
						.recordStats()
						.build()),
				new CaffeineCache(HENT_DOKMET_SPRAAKINFO, Caffeine.newBuilder()
						.expireAfterWrite(DEFAULT_CACHE_EXPIRATION_TIME)
						.recordStats()
						.build()),
				new CaffeineCache(HENT_NAVN, Caffeine.newBuilder()
						.expireAfterWrite(HENT_NAVN_CACHE_EXPIRATION_TIME)
						.recordStats()
						.build()),
				new CaffeineCache(HENT_PERSON, Caffeine.newBuilder()
						.expireAfterWrite(HENT_PERSON_CACHE_EXPIRATION_TIME)
						.recordStats()
						.build()),
				new CaffeineCache(RESTSTS_CACHE_NAME, Caffeine.newBuilder()
						.expireAfterWrite(STS_CACHE_EXPIRATION_TIME)
						.recordStats()
						.build()),
				new CaffeineCache(AZURE_CLIENT_CREDENTIAL_TOKEN, Caffeine.newBuilder()
						.expireAfterWrite(AZURE_CLIENT_CREDENTIAL_TOKEN_EXPIRATION_TIME)
						.recordStats()
						.build())));
		return cacheManager;
	}
}
