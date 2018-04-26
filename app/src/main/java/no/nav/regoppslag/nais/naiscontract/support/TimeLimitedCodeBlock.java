package no.nav.regoppslag.nais.naiscontract.support;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.exceptions.SelftestTimeoutException;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by T133804 on 15.08.2017.
 */
@Slf4j
public final class TimeLimitedCodeBlock {
	
	private TimeLimitedCodeBlock() {
	}
	
	public static void runWithTimeout(final Runnable runnable, long timeout, TimeUnit timeUnit, String methodName) {
		Callable<Object> callableObj = () -> {
			runnable.run();
			return null;
		};
		
		runWithTimeout(callableObj, timeout, timeUnit, methodName);
	}
	
	public static <T> T runWithTimeout(Callable<T> callable, long timeout, TimeUnit timeUnit, String methodName) {
		final ExecutorService executor = Executors.newSingleThreadExecutor();
		final Future<T> future = executor.submit(callable);
		executor.shutdown(); // This does not cancel the already-scheduled task.
		try {
			return future.get(timeout, timeUnit);
		} catch (TimeoutException e) {
			future.cancel(true);
			if(log.isDebugEnabled()) {
				log.debug("Selftest " + methodName + " timed out.", e);
			}
			throw new SelftestTimeoutException(methodName + " timed out", e.getCause());
		} catch (InterruptedException |ExecutionException e) {
			if(log.isDebugEnabled()) {
				log.debug("Selftest " + methodName + " failed.", e);
			}
			throw new SelftestTimeoutException(methodName + " failed with message:" + e.getMessage() , e.getCause());
		}
	}
}
