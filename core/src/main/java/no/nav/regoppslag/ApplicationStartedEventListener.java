package no.nav.regoppslag;

import org.springframework.boot.actuate.metrics.cache.CacheMetricsRegistrar;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationListener;

public class ApplicationStartedEventListener implements ApplicationListener<ApplicationStartedEvent> {

	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		configureCacheMetrics(event);
	}

	private static void configureCacheMetrics(ApplicationStartedEvent event) {
		CacheMetricsRegistrar cacheMetricsRegistrar = event.getApplicationContext().getBean(CacheMetricsRegistrar.class);
		CacheManager cacheManager = event.getApplicationContext().getBean(CacheManager.class);

		cacheManager.getCacheNames().forEach(cacheName -> cacheMetricsRegistrar.bindCacheToRegistry(cacheManager.getCache(cacheName)));
	}
}
