package no.nav.regoppslag.config;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import org.springframework.context.event.EventListener;
import org.springframework.core.retry.RetryException;
import org.springframework.resilience.retry.MethodRetryEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RetryLoggingInterceptor {

	@EventListener
	public void onRetry(MethodRetryEvent event) {
		if (event.getFailure() instanceof RetryException retryException) {
			categorizeAndLogExhaustedRetries(event, retryException.getCause());
		} else {
			categorizeAndLogExhaustedRetries(event, event.getFailure());
		}
	}

	private static void categorizeAndLogExhaustedRetries(MethodRetryEvent event, Throwable throwable) {
		if (!(throwable instanceof RegOppslagFunctionalException)) {
			log.warn("Retry for {} failed: {}", event.getMethod().getName(), event.getFailure().getMessage(), event.getFailure());
		}
	}
}
