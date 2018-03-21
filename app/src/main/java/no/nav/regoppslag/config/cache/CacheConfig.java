package no.nav.regoppslag.config.cache;

import static no.nav.regoppslag.config.security.provider.rest.SecurityConfig.LDAP_CACHE_RS_LOGIN;
import static no.nav.regoppslag.consumer.dokkat.Tkat020DokumenttypeInfo.HENT_DOKKAT_SPRAAKINFO;
import static no.nav.regoppslag.consumer.ldap.LdapAdeoUserLookup.HENT_FULLT_NAVN;
import static no.nav.regoppslag.consumer.norg2.OrganisasjonEnhetKontaktinformasjonV1Consumer.HENT_ENHET_NAVN;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
@Configuration
@EnableCaching
public class CacheConfig {

	@Bean
	public CacheManager cacheManager() {
		// configure and return an implementation of Spring's CacheManager SPI
		SimpleCacheManager cacheManager = new SimpleCacheManager();
		//TODO BRUKER
		//TODO ORGANISASJON
		//TODO Virksomhetsadresse EREG
		CaffeineCache cacheHentFulltNavn = new CaffeineCache(HENT_FULLT_NAVN, Caffeine.newBuilder()
				.expireAfterAccess(2, TimeUnit.DAYS)
				.maximumSize(2000)
				.build());
		cacheManager.setCaches(Arrays.asList(cacheHentFulltNavn,
				new ConcurrentMapCache(LDAP_CACHE_RS_LOGIN),
				new ConcurrentMapCache(HENT_ENHET_NAVN),
				new ConcurrentMapCache(HENT_DOKKAT_SPRAAKINFO)));
		return cacheManager;
	}

}
