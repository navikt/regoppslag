package no.nav.regoppslag.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Optional;

import static no.nav.regoppslag.metrics.MetricLabels.COMPONENT;
import static no.nav.regoppslag.metrics.MetricLabels.CONSUMER;
import static no.nav.regoppslag.metrics.MetricLabels.CONSUMER_NAME;
import static no.nav.regoppslag.metrics.MetricLabels.EVENT;
import static no.nav.regoppslag.metrics.MetricLabels.OPERATION;
import static no.nav.regoppslag.metrics.MetricLabels.PROCESS;
import static no.nav.regoppslag.metrics.MetricLabels.SERVICE;
import static no.nav.regoppslag.metrics.MetricLabels.TYPE;
import static no.nav.regoppslag.util.MDCConstants.CONSUMER_ID;
import static no.nav.regoppslag.util.MDCConstants.UKJENT;
import static no.nav.regoppslag.util.MDCConstants.USER_ID;

@Slf4j
@Component
public class MicrometerMetrics {

	@Autowired
	private MeterRegistry registry;

	public void cacheMiss(String cacheName) {
		Counter.builder("dok_request_counter")
				.tag(PROCESS, cacheName)
				.tag(TYPE, "cacheCounter")
				.tag(CONSUMER, getConsumerId())
				.tag(EVENT, "cacheMiss")
				.register(registry).increment();
	}

	public void cacheError(String cacheName, String operation) {
		Counter.builder("dok_cache_exception_counter")
				.tag(PROCESS, cacheName)
				.tag(TYPE, "cacheError")
				.tag(CONSUMER, getConsumerId())
				.tag(OPERATION, operation)
				.register(registry).increment();
	}

	public void pluginReceived(String serviceCode, String pluginName) {
		Counter.builder("dok_plugin_counter")
				.tag(SERVICE, serviceCode)
				.tag(CONSUMER_NAME, getConsumerId())
				.tag("plugin", pluginName)
				.tag(EVENT, "received")
				.register(registry).increment();
	}

	public void meter(String serviceCode, String componentName, String event, String value) {
		Counter.builder("dok_event_counter")
				.tag(SERVICE, serviceCode)
				.tag(COMPONENT, componentName)
				.tag(CONSUMER_NAME, getConsumerId())
				.tag(EVENT, event)
				.tag("value", value)
				.register(registry).increment();
	}

	public void dependenycPingable(String name, Double value) {
		Gauge.builder("dependency_ping", value, value1 -> value)
				.tag("label_name", name)
				.tag("help", "Dependency is pingable")
				.register(registry);
	}

	public double countEvents(String service, String component, String event) {
		Optional<Meter> optionalMeter = registry.getMeters().stream()
				.filter(meter -> (
						service.equals(meter.getId().getTag(SERVICE)) &&
						component.equals(meter.getId().getTag(COMPONENT)) &&
						event.equals(meter.getId().getTag(EVENT)) &&
						getConsumerId().equals(meter.getId().getTag(CONSUMER_NAME)))
				).findFirst();
		if(optionalMeter.isPresent()) {
			Iterator<Measurement> measurements = optionalMeter.get().measure().iterator();
			if(measurements.hasNext()) {
				return measurements.next().getValue();
			}
		}
		return -1;
	}

	static String getConsumerId() {
		return MDC.get(CONSUMER_ID) == null ? UKJENT : MDC.get(CONSUMER_ID);
	}

	public static String getUserId() {
		return MDC.get(USER_ID) == null ? UKJENT : MDC.get(USER_ID);
	}

}
