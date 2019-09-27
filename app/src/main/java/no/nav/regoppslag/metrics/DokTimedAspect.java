package no.nav.regoppslag.metrics;

import static java.util.Arrays.asList;
import static no.nav.regoppslag.metrics.MetricLabels.CONSUMER;
import static no.nav.regoppslag.metrics.MicrometerMetrics.getConsumerId;

import io.micrometer.core.annotation.Incubating;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.lang.NonNullApi;
import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;
import java.util.function.Function;

@Aspect
@NonNullApi
@Incubating(since = "1.0.0")
@Slf4j
@Order(1)
public class DokTimedAspect {

	private final MeterRegistry registry;
	private final Function<ProceedingJoinPoint, Iterable<Tag>> tagsBasedOnJoinpoint;

	public DokTimedAspect(MeterRegistry registry) {
		this(registry, pjp ->
				Tags.of("class", pjp.getStaticPart().getSignature().getDeclaringTypeName(),
						"method", pjp.getStaticPart().getSignature().getName())
		);
	}

	private DokTimedAspect(MeterRegistry registry, Function<ProceedingJoinPoint, Iterable<Tag>> tagsBasedOnJoinpoint) {
		this.registry = registry;
		this.tagsBasedOnJoinpoint = tagsBasedOnJoinpoint;
	}

	@Before("execution (@org.springframework.cache.annotation.Cacheable * *.*(..))")
	public void cacheLookup(JoinPoint pjp) {
		Method method = ((MethodSignature) pjp.getSignature()).getMethod();

		Cacheable cacheable = method.getAnnotation(Cacheable.class);
		if (cacheable == null || cacheable.value().length < 1) {
			return;
		}
		Counter.builder("dok_request_counter")
				.tag("process", cacheable.value()[0])
				.tag("type", "cacheCounter")
				.tag(CONSUMER, getConsumerId())
				.tag("event", "cacheTotal")
				.register(registry).increment();
	}

	@Around("execution (@no.nav.regoppslag.metrics.Metrics * *.*(..))")
	public Object incrementMetrics(ProceedingJoinPoint pjp) throws Throwable {
		Method method = ((MethodSignature) pjp.getSignature()).getMethod();

		Metrics metrics = method.getAnnotation(Metrics.class);
		if (metrics == null || metrics.value().isEmpty()) {
			return pjp.proceed();
		}

		Timer.Sample sample = Timer.start(registry);
		try {
			return pjp.proceed();
		} catch (Exception e) {
			if(metrics.countExceptions()) {
				Counter.builder(metrics.value() + "_exception")
						.tags("error_type", isFunctionalException(method, e) ? "functional" : "technical")
						.tags("exception_name", e.getClass().getSimpleName())
						.tags(metrics.extraTags())
						.tags(tagsBasedOnJoinpoint.apply(pjp))
						.register(registry)
						.increment();
			}

			throw e;

		} finally {
			sample.stop(Timer.builder(metrics.value())
					.description(metrics.description().isEmpty() ? null : metrics.description())
					.tags(metrics.extraTags())
					.tags(tagsBasedOnJoinpoint.apply(pjp))
					.publishPercentileHistogram(metrics.histogram())
					.publishPercentiles(metrics.percentiles().length == 0 ? null : metrics.percentiles())
					.register(registry));
		}
	}

	private boolean isFunctionalException(Method method, Exception e) {
		return asList(method.getExceptionTypes()).contains(e.getClass()) || isFunctionalException(e);
	}

	private boolean isFunctionalException(Throwable e) {
		return e instanceof RegOppslagFunctionalException;
	}
}
