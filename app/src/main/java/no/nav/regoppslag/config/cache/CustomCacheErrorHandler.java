package no.nav.regoppslag.config.cache;

import static no.nav.regoppslag.metrics.PrometheusMetrics.requestCounter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

/**
 * CustomCacheErrorHandler
 * Cache feil bør ikke påvirke tjenestene. Denne klassen vil derfor ignorere og logge alle cachefeil.
 *
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Slf4j
public class CustomCacheErrorHandler implements CacheErrorHandler {
	
	private final JedisConnectionFactory jedisConnectionFactory;
	
	public CustomCacheErrorHandler(JedisConnectionFactory jedisConnectionFactory) {
		this.jedisConnectionFactory = jedisConnectionFactory;
	}
	
	@Override
	public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
		log.warn(String.format("Feil ved Cache Get operasjon. CacheNavn=%s, nøkkel=%s, feilklasse=%s, feilmelding=%s", cache.getName(), key, exception
				.getClass()
				.getSimpleName(), exception.getMessage()));
		requestCounter.labels("Redis", "CacheError", "Get").inc();
		
		try {
			
			/** Destroys the pool connections and reconnects **/
			jedisConnectionFactory.destroy();
			jedisConnectionFactory.getConnection().getSubscription().isAlive();
			log.info("Reconnected to redis");
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}
	
	@Override
	public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
		log.warn(String.format("Feil ved Cache Put operasjon. CacheNavn=%s, nøkkel=%s, feilklasse=%s, feilmelding=%s", cache.getName(), key, exception
				.getClass()
				.getSimpleName(), exception.getMessage()));
		requestCounter.labels("Redis", "CacheError", "Put").inc();
	}
	
	@Override
	public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
		log.warn(String.format("Feil ved Cache Evict operasjon. CacheNavn=%s, nøkkel=%s, feilklasse=%s, feilmelding=%s", cache.getName(), key, exception
				.getClass()
				.getSimpleName(), exception.getMessage()));
		requestCounter.labels("Redis", "CacheError", "Evict").inc();
	}
	
	@Override
	public void handleCacheClearError(RuntimeException exception, Cache cache) {
		log.warn(String.format("Feil ved Cache Clear operasjon. CacheNavn=%s, feilklasse=%s, feilmelding=%s", cache.getName(), exception
				.getClass()
				.getSimpleName(), exception.getMessage()));
		requestCounter.labels("Redis", "CacheError", "Clear").inc();
	}
}
