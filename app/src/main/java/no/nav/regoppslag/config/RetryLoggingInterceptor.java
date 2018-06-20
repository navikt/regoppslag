package no.nav.regoppslag.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.listener.RetryListenerSupport;
import org.springframework.stereotype.Component;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
@Slf4j
public class RetryLoggingInterceptor extends RetryListenerSupport {

	@Override
	public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
		super.onError(context, callback, throwable);
		log.warn(String.format("Retry trigget for %s. gang med feilmelding=%s ", context.getRetryCount(), throwable.getMessage()), throwable);

	}
}
