package no.nav.regoppslag.nais.selftest;

import static no.nav.regoppslag.metrics.PrometheusMetrics.dependencyPingable;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.vavr.control.Try;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Slf4j
@Setter
@Getter
public abstract class AbstractDependencyCheck {

	protected final DependencyType type;
	protected final Importance importance;
	protected String name;
	protected String address;
	private final CircuitBreakerRegistry circuitBreakerRegistry;
	private static final ExecutorService executor = Executors.newSingleThreadExecutor();
	private final TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom().timeoutDuration(Duration.ofMillis(3200)).cancelRunningFuture(true).build();
	private final TimeLimiter timeLimiter = TimeLimiter.of(timeLimiterConfig);

	public AbstractDependencyCheck(DependencyType type, String name, String address, Importance importance) {
		this.type = type;
		this.name = name;
		this.address = address;
		this.importance = importance;
		this.circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
	}

	protected abstract void doCheck();
	
	public Try<DependencyCheckResult> check(final Authentication authentication) {
		final String dependencyName = this.name;
		Supplier<Future<DependencyCheckResult>> futureSupplier = () -> executor.submit(getCheckCallable(authentication));
		Callable<DependencyCheckResult> timeRestrictedCall = TimeLimiter.decorateFutureSupplier(timeLimiter, futureSupplier);
		CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(dependencyName);
		Callable<DependencyCheckResult> chainedCallable = CircuitBreaker.decorateCallable(circuitBreaker, timeRestrictedCall);
		return Try.ofCallable(chainedCallable)
				.onSuccess(success -> dependencyPingable.labels(this.name).set(1))
				.onFailure(throwable -> {
					dependencyPingable.labels(this.name).set(0);
					log.error("Call to dependency={} with type={} at url={} timed out or circuitbreaker was tripped.", getName(), getType(), getAddress(), throwable);
				})
				.recover(throwable -> DependencyCheckResult.builder()
						.endpoint(getName())
						.address(getAddress())
						.type(getType())
						.importance(getImportance())
						.result(getImportance().equals(Importance.CRITICAL) ? Result.ERROR : Result.WARNING)
						.errorMessage("Call to dependency=" + getName() +" timed out or circuitbreaker tripped. ErrorMessage="+getErrorMessageFromThrowable(throwable))
						.throwable(throwable)
						.build()
				);

	}

	public Callable<DependencyCheckResult> getCheckCallable(Authentication authentication) {
		return () -> {
			SecurityContextHolder.getContext().setAuthentication(authentication);
			DependencyCheckResult.DependencyCheckResultBuilder builder = DependencyCheckResult.builder()
					.type(getType())
					.endpoint(getName())
					.importance(getImportance())
					.address(getAddress());

			Instant start = Instant.now();
			doCheck();
			Instant end = Instant.now();
			Long responseTime=Duration.between(start, end).toMillis();
			return builder.result(Result.OK).responseTime(String.valueOf(responseTime)+"ms").build();
		};
	}

	protected String getErrorMessageFromThrowable(Throwable e) {
		if (e instanceof TimeoutException) {
			return "Call to dependency timed out by circuitbreaker";
		}
		return e.getCause()==null?e.getMessage():e.getCause().getMessage();
	}

}
