package no.nav.regoppslag.metrics;

import static no.nav.regoppslag.metrics.MetricLabels.COMPONENT;
import static no.nav.regoppslag.metrics.MetricLabels.CONSUMER;
import static no.nav.regoppslag.util.MDCConstants.CONSUMER_ID;
import static no.nav.regoppslag.util.MDCConstants.UKJENT;
import static no.nav.regoppslag.util.MDCConstants.USER_ID;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Slf4j
@Component
public class MicrometerMetrics {
	@Inject
	private MeterRegistry registry;

	public void cacheMiss(String cacheName) {
		Counter.builder("dok_request_counter")
				.tag("process", cacheName)
				.tag("type", "cacheCounter")
				.tag(CONSUMER, getConsumerId())
				.tag("event", "cacheMiss")
				.register(registry).increment();
	}

	public void cacheError(String cacheName, String operation) {
		Counter.builder("dok_cache_exception_counter")
				.tag("process", cacheName)
				.tag("type", "cacheError")
				.tag(CONSUMER, getConsumerId())
				.tag("operation", operation)
				.register(registry).increment();
	}

	public void pluginReceived(String serviceCode, String pluginName) {
		Counter.builder("dok_plugin_counter")
				.tag("service", serviceCode)
				.tag("consumer_name", getConsumerId())
				.tag("plugin", pluginName)
				.tag("event", "received")
				.register(registry).increment();
	}

	public void meter(String serviceCode, String componentName, String event, String value) {
		Counter.builder("dok_event_counter")
				.tag("service", serviceCode)
				.tag(COMPONENT, componentName)
				.tag("consumer_name", getConsumerId())
				.tag("event", event)
				.tag("value", value)
				.register(registry).increment();
	}

	public static String getConsumerId() {
		return MDC.get(CONSUMER_ID) == null ? UKJENT : MDC.get(CONSUMER_ID);
	}

	public static String getUserId() {
		return MDC.get(USER_ID) == null ? UKJENT : MDC.get(USER_ID);
	}

}
