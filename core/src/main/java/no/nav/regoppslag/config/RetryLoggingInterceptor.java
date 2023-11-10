package no.nav.regoppslag.config;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RetryLoggingInterceptor implements RetryListener {

	@Override
	public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
		if (!(throwable instanceof RegOppslagFunctionalException)) {
			log.warn(String.format("Retry trigget for %s. gang med feilmelding=%s ", context.getRetryCount(), throwable.getMessage()), throwable);
		}
	}
}
