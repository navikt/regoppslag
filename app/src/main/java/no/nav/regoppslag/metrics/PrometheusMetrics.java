package no.nav.regoppslag.metrics;

import static no.nav.regoppslag.metrics.PrometheusLabels.LABEL_CONSUMER_ID;
import static no.nav.regoppslag.metrics.PrometheusLabels.LABEL_ERROR_TYPE;
import static no.nav.regoppslag.metrics.PrometheusLabels.LABEL_EVENT;
import static no.nav.regoppslag.metrics.PrometheusLabels.LABEL_EXCEPTION_NAME;
import static no.nav.regoppslag.metrics.PrometheusLabels.LABEL_PROCESS;
import static no.nav.regoppslag.metrics.PrometheusLabels.LABEL_PROCESS_CALLED;
import static no.nav.regoppslag.metrics.PrometheusLabels.LABEL_PROCESS_CODE;
import static no.nav.regoppslag.metrics.PrometheusLabels.LABEL_TYPE;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class PrometheusMetrics {
	public static final String DOK_NAMESPACE = "dok";
	
	public static final Gauge isReady = Gauge.build()
			.namespace(DOK_NAMESPACE)
			.name("app_is_ready")
			.help("App is ready to receive traffic")
			.register();
	
	public static final Counter requestCounter = Counter.build()
			.namespace(DOK_NAMESPACE)
			.name("request_total_counter")
			.help("Counts total number of messages received per event")
			.labelNames(LABEL_PROCESS, LABEL_TYPE, LABEL_CONSUMER_ID, LABEL_EVENT).register();
	
	public static final Counter requestExceptionCounter = Counter.build()
			.namespace(DOK_NAMESPACE)
			.name("request_exception_total_counter")
			.help("Total exception counter.")
			.labelNames(LABEL_PROCESS, LABEL_ERROR_TYPE, LABEL_EXCEPTION_NAME)
			.register();
	
	public static final Histogram requestLatency = Histogram.build()
			.namespace(DOK_NAMESPACE)
			.name("internal_request_latency_seconds_histogram")
			.help("request latency in seconds.")
			.labelNames(LABEL_PROCESS, LABEL_PROCESS_CODE, LABEL_PROCESS_CALLED)
			.register();
	
	public static String getConsumerId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication.getName().equalsIgnoreCase("anonymousUser")) {
			return "Ukjent";
		}
		return authentication.getName();
	}
	
}
