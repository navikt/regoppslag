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
import static no.nav.regoppslag.consumer.azure.AzureAdGraphService.HENT_FULLT_NAVN;

/**
 * Cachemanager for bruk ved lokalt kjøring av applikasjonen.
 * Redis cache krever en Redis server som for å fungere. Redis serveren som kjører på nais er ikke eksponert ut og er derfor ikke mulig å aksessere lokalt.
 * For å slippe å starte opp Redis server lokalt så vil denne klassen configurere cachemanager som kan kjøre ved lokalt kjøring av applikasjonen.
 */
@Profile("local")
@Configuration
@EnableCaching
public class LocalCacheConfig {

	public static final String HENT_ENHET_NAVN = "hentEnhetNavn";
	public static final String HENT_ENHET_KONTAKTINFO = "hentEnhetKontaktInfo";
	public static final String HENT_ORGANISASJON = "hentOrganisasjon";
	public static final String HENT_PERSON = "hentPerson";
	public static final String HENT_NAVN = "hentNavn";
	public static final String STS_CACHE_NAME = "STS_CACHE_NAME";
	public static final String HENT_DOKKAT_SPRAAKINFO = "hentDokumenttypeInfoSpraak";
	public static final String RESTSTS_CACHE_NAME = "RESTSTS_CACHE_NAME";
	public static final String AZURE_CLIENT_CREDENTIAL_TOKEN_CACHE = "AZURE_CACHE_NAME";

	@Bean
	@Primary
	public CacheManager cacheManager() {

		SimpleCacheManager cacheManager = new SimpleCacheManager();
		cacheManager.setCaches(Arrays.asList(
				new CaffeineCache(HENT_FULLT_NAVN, Caffeine.newBuilder()
						.expireAfterWrite(DEFAULT_CACHE_EXPIRATION_TIME.getSeconds(), SECONDS)
						.build()),
				new CaffeineCache(HENT_ENHET_NAVN, Caffeine.newBuilder()
						.expireAfterWrite(DEFAULT_CACHE_EXPIRATION_TIME.getSeconds(), SECONDS)
						.build()),
				new CaffeineCache(HENT_ENHET_KONTAKTINFO, Caffeine.newBuilder()
						.expireAfterWrite(DEFAULT_CACHE_EXPIRATION_TIME.getSeconds(), SECONDS)
						.build()),
				new CaffeineCache(HENT_PERSON, Caffeine.newBuilder()
						.expireAfterWrite(HENT_PERSON_CACHE_EXPIRATION_TIME.getSeconds(), SECONDS)
						.build()),
				new CaffeineCache(HENT_ORGANISASJON, Caffeine.newBuilder()
						.expireAfterWrite(DEFAULT_CACHE_EXPIRATION_TIME.getSeconds(), SECONDS)
						.build()),
				new CaffeineCache(HENT_DOKKAT_SPRAAKINFO, Caffeine.newBuilder()
						.expireAfterWrite(DEFAULT_CACHE_EXPIRATION_TIME.getSeconds(), SECONDS)
						.build()),
				new CaffeineCache(STS_CACHE_NAME, Caffeine.newBuilder()
						.expireAfterWrite(STS_CACHE_EXPIRATION_TIME.getSeconds(), SECONDS)
						.build()),
				new CaffeineCache(RESTSTS_CACHE_NAME, Caffeine.newBuilder()
						.expireAfterWrite(STS_CACHE_EXPIRATION_TIME.getSeconds(), SECONDS)
						.build()),
				new CaffeineCache(AZURE_CLIENT_CREDENTIAL_TOKEN_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(STS_CACHE_EXPIRATION_TIME.getSeconds(), SECONDS)
						.build())));
		return cacheManager;
	}

}
