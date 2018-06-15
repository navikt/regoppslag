package no.nav.regoppslag.config.cache;

import static no.nav.regoppslag.consumer.personv3.PersonV3Consumer.HENT_PERSON;
import static no.nav.regoppslag.metrics.PrometheusLabels.CACHE_ERROR;
import static no.nav.regoppslag.metrics.PrometheusLabels.REDIS_CACHE;
import static no.nav.regoppslag.metrics.PrometheusMetrics.getConsumerId;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestCounter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

/**
 * CustomCacheErrorHandler
 * Cache feil bør ikke påvirke tjenestene. Denne klassen vil derfor ignorere og logge alle cachefeil.
 *
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Slf4j
public class CustomCacheErrorHandler implements CacheErrorHandler {
	
	
	@Override
	public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
		if (cache.getName().equals(HENT_PERSON)) {
			key = hidePersonIdent((String) key);
			//TODO: Fjern dette senere hvis det ikke er nødvendig
		}
		log.warn(String.format("Feil ved Cache Get operasjon. CacheNavn=%s, feilklasse=%s, feilmelding=%s", cache.getName(), exception
				.getClass()
				.getSimpleName(), exception.getMessage()));
		requestCounter.labels(REDIS_CACHE, REDIS_CACHE, CACHE_ERROR, getConsumerId(), "GET").inc();
	}
	
	@Override
	public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
		log.warn(String.format("Feil ved Cache Put operasjon. CacheNavn=%s, feilklasse=%s, feilmelding=%s", cache.getName(), exception
				.getClass()
				.getSimpleName(), exception.getMessage()));
	}
	
	@Override
	public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
		log.warn(String.format("Feil ved Cache Evict operasjon. CacheNavn=%s, feilklasse=%s, feilmelding=%s", cache.getName(), exception
				.getClass()
				.getSimpleName(), exception.getMessage()));
	}
	
	@Override
	public void handleCacheClearError(RuntimeException exception, Cache cache) {
		log.warn(String.format("Feil ved Cache Clear operasjon. CacheNavn=%s, feilklasse=%s, feilmelding=%s", cache.getName(), exception
				.getClass()
				.getSimpleName(), exception.getMessage()));
	}
	
	private String hidePersonIdent(String key) {
		return (key).replace((key).substring(0, 11), "<personident>");
	}
}
