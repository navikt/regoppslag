package no.nav.regoppslag.config.cache;

import static no.nav.regoppslag.config.cache.CacheConfig.DEFAULT_CACHE_EXPIRATION_SECONDS;
import static no.nav.regoppslag.config.cache.CacheConfig.HENT_PERSON_CACHE_EXPIRATION_SECONDS;
import static no.nav.regoppslag.config.cache.CacheConfig.STS_CACHE_EXPIRATION_SECONDS;
import static no.nav.regoppslag.consumer.dokkat.Tkat020DokumenttypeInfo.HENT_DOKKAT_SPRAAKINFO;
import static no.nav.regoppslag.consumer.ldap.LdapAdeoUserLookup.HENT_FULLT_NAVN;
import static no.nav.regoppslag.consumer.norg2.OrganisasjonEnhetKontaktinformasjonV1Consumer.HENT_ENHET_NAVN;
import static no.nav.regoppslag.consumer.organisasjonv4.OrganisasjonV4Consumer.HENT_ORGANISASJON;
import static no.nav.regoppslag.consumer.personv3.PersonV3Consumer.HENT_PERSON;
import static no.nav.regoppslag.nais.naiscontract.support.SelftestSTSConfig.STS_CACHE_NAME;

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
import java.util.concurrent.TimeUnit;

/**
 * Cachemanager for bruk ved lokalt kjøring av applikasjonen.
 * <p>
 * Redis cache krever en Redis server som for å fungere. Redis serveren som kjører på nais er ikke eksponert ut og er derfor ikke mulig å aksessere lokalt.
 * For å slippe å starte opp Redis server lokalt så vil denne klassen configurere cachemanager som kan kjøre ved lokalt kjøring av applikasjonen.
 *
 * @author Ugur Alpay Cenar, Visma Consulting.
 */

@Profile("local")
@Configuration
@EnableCaching
public class LocalCacheConfig {
	
	
	@Bean
	@Primary
	public CacheManager cacheManager() {

		SimpleCacheManager cacheManager = new SimpleCacheManager();
		cacheManager.setCaches(Arrays.asList(
				new CaffeineCache(HENT_FULLT_NAVN, Caffeine.newBuilder()
						.expireAfterWrite(DEFAULT_CACHE_EXPIRATION_SECONDS, TimeUnit.SECONDS)
						.build()),
				new CaffeineCache(HENT_ENHET_NAVN, Caffeine.newBuilder()
						.expireAfterWrite(DEFAULT_CACHE_EXPIRATION_SECONDS, TimeUnit.SECONDS)
						.build()),
				new CaffeineCache(HENT_PERSON, Caffeine.newBuilder()
						.expireAfterWrite(HENT_PERSON_CACHE_EXPIRATION_SECONDS, TimeUnit.SECONDS)
						.build()),
				new CaffeineCache(HENT_ORGANISASJON, Caffeine.newBuilder()
						.expireAfterWrite(DEFAULT_CACHE_EXPIRATION_SECONDS, TimeUnit.SECONDS)
						.build()),
				new CaffeineCache(HENT_DOKKAT_SPRAAKINFO, Caffeine.newBuilder()
						.expireAfterWrite(DEFAULT_CACHE_EXPIRATION_SECONDS, TimeUnit.SECONDS)
						.build())));
		new CaffeineCache(STS_CACHE_NAME, Caffeine.newBuilder()
				.expireAfterWrite(STS_CACHE_EXPIRATION_SECONDS, TimeUnit.SECONDS)
				.build());
		return cacheManager;
		
	}
	
}
