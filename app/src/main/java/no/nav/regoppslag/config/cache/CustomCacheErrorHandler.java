package no.nav.regoppslag.config.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.data.redis.RedisConnectionFailureException;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Slf4j
public class CustomCacheErrorHandler implements CacheErrorHandler {
	
	@Override
	public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
		log.warn(String.format("Feil ved Cache Get operasjon. CacheNavn=%s, feilklasse=%s, feilmelding=%s", cache.getName(), exception.getClass().getSimpleName(), exception.getMessage()));
		if(!(exception instanceof RedisConnectionFailureException)){
			throw exception;
		}
	}
	
	@Override
	public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
		log.warn(String.format("Feil ved Cache Put operasjon. CacheNavn=%s, feilklasse=%s, feilmelding=%s", cache.getName(), exception.getClass().getSimpleName(), exception.getMessage()));
		if(!(exception instanceof RedisConnectionFailureException)){
			throw exception;
		}
	}
	
	@Override
	public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
		if(!(exception instanceof RedisConnectionFailureException)){
			throw exception;
		}
	}
	
	@Override
	public void handleCacheClearError(RuntimeException exception, Cache cache) {
		if(!(exception instanceof RedisConnectionFailureException)){
			throw exception;
		}
	}
}
