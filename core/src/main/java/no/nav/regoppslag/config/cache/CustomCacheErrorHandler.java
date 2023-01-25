package no.nav.regoppslag.config.cache;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

/**
 * CustomCacheErrorHandler
 * Cache feil bør ikke påvirke tjenestene. Denne klassen vil derfor ignorere og logge alle cachefeil.
 */
@Slf4j
public class CustomCacheErrorHandler implements CacheErrorHandler {

	@Autowired
	MicrometerMetrics metrics;

	@Override
	public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
		log.warn(String.format("Feil ved Cache Get operasjon. CacheNavn=%s, feilklasse=%s, feilmelding=%s", cache.getName(), exception
				.getClass()
				.getSimpleName(), exception.getMessage()));
		metrics.cacheError(cache.getName(), "GET");
	}
	
	@Override
	public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
		log.warn(String.format("Feil ved Cache Put operasjon. CacheNavn=%s, feilklasse=%s, feilmelding=%s", cache.getName(), exception
				.getClass()
				.getSimpleName(), exception.getMessage()));
		log.warn(exception.getMessage(), exception);
		metrics.cacheError(cache.getName(), "PUT");
	}
	
	@Override
	public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
		log.warn(String.format("Feil ved Cache Evict operasjon. CacheNavn=%s, feilklasse=%s, feilmelding=%s", cache.getName(), exception
				.getClass()
				.getSimpleName(), exception.getMessage()));
		metrics.cacheError(cache.getName(), "EVICT");
	}
	
	@Override
	public void handleCacheClearError(RuntimeException exception, Cache cache) {
		log.warn(String.format("Feil ved Cache Clear operasjon. CacheNavn=%s, feilklasse=%s, feilmelding=%s", cache.getName(), exception
				.getClass()
				.getSimpleName(), exception.getMessage()));
		metrics.cacheError(cache.getName(), "CLEAR");
	}
	
}
