package no.nav.regoppslag.nais;

import static no.nav.regoppslag.metrics.PrometheusMetrics.dependencyPingable;
import static no.nav.regoppslag.metrics.PrometheusMetrics.isReady;
import static no.nav.regoppslag.nais.checkcore.NaisCheckSTSConfig.STS_CACHE_NAME;
import static org.springframework.security.core.authority.AuthorityUtils.NO_AUTHORITIES;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.nais.checkcore.AbstractDependencyCheck;
import no.nav.regoppslag.nais.checkcore.DependencyCheckResult;
import no.nav.regoppslag.nais.checkcore.Importance;
import no.nav.regoppslag.nais.checkcore.Result;
import no.nav.regoppslag.nais.checks.PersonV3Check;
import org.apache.cxf.ws.security.trust.STSClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */

@Slf4j
@RestController

public class NaisContract {
	
	private static final String APPLICATION_ALIVE = "Application is alive!";
	private static final String APPLICATION_READY = "Application is ready for traffic!";
	private static final String APPLICATION_NOT_READY = "Application is not ready for traffic :-(";
	
	private final List<AbstractDependencyCheck> checkList;
	
	private CircuitBreakerRegistry circuitBreakerRegistry;
	
	private static final ExecutorService executor = Executors.newSingleThreadExecutor();
	
	private final TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
			.timeoutDuration(Duration.ofMillis(2800))
			.cancelRunningFuture(true).build();
	private final TimeLimiter timeLimiter = TimeLimiter.of(timeLimiterConfig);
	
	
	@Inject
	private STSClient stsClient;
	
	@Inject
	public NaisContract(List<AbstractDependencyCheck> checks) {
		checkList = new ArrayList<>(checks);
		circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
	}
	
	@Cacheable(value = STS_CACHE_NAME, key = "#methodName")
	public String requestStsToken() throws Exception {
		return elementToString(stsClient.requestSecurityToken().getToken());
	}
	
	@GetMapping("/isAlive")
	public String isAlive() {
		return APPLICATION_ALIVE;
	}
	
	@ResponseBody
	@RequestMapping(value = "/isReady", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity isReady() throws Exception {
		
		
		List<DependencyCheckResult> results = new ArrayList<>();
		
		checkDependencies(results);
		
		if (isAnyVitalDependencyUnhealthy(results.stream()
				.map(DependencyCheckResult::getResult)
				.collect(Collectors.toList()))) {
			isReady.dec();
			return new ResponseEntity<>(APPLICATION_NOT_READY, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		isReady.set(1);
		
		return new ResponseEntity<>(APPLICATION_READY, HttpStatus.OK);
		
	}
	
	private void checkDependencies(List<DependencyCheckResult> results) throws Exception {
		UsernamePasswordAuthenticationToken authenticationToken = getSTSAuthenticationToken();
		
		Flowable.fromIterable(checkList)
				.parallel()
				.runOn(Schedulers.newThread())
				.map(payload -> {
					if (payload instanceof PersonV3Check) {
						return dependencyCheck(payload, authenticationToken);
					}
					return dependencyCheck(payload, null);
				})
				.sequential().blockingSubscribe(result -> results.add(result.get()));
	}
	
	private UsernamePasswordAuthenticationToken getSTSAuthenticationToken() throws Exception {
		String decodedToken = requestStsToken();
		return new UsernamePasswordAuthenticationToken("NaisIsReadySamlToken", decodedToken, NO_AUTHORITIES);
	}
	
	private boolean isAnyVitalDependencyUnhealthy(List<no.nav.regoppslag.nais.checkcore.Result> results) {
		return results.stream().anyMatch((result) -> result.equals(Result.ERROR));
	}
	
	private Try<DependencyCheckResult> dependencyCheck(final AbstractDependencyCheck dependencyCheck, final Authentication authentication) {
		final String dependencyName = dependencyCheck.getName().toLowerCase();
		Supplier<Future<DependencyCheckResult>> futureSupplier = () -> executor.submit(dependencyCheck.check(authentication));
		Callable<DependencyCheckResult> timeRestrictedCall = TimeLimiter.decorateFutureSupplier(timeLimiter, futureSupplier);
		CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(dependencyName);
		Callable<DependencyCheckResult> chainedCallable = CircuitBreaker.decorateCallable(circuitBreaker, timeRestrictedCall);
		return Try.ofCallable(chainedCallable)
				.onSuccess(dependencyCheckResult -> dependencyPingable.labels(dependencyName).set(1.0))
				.onFailure(throwable -> {
					dependencyPingable.labels(dependencyName).dec();
					log.error("Call to dependency={} at url={} timed out or circuitbreaker was tripped.", dependencyCheck.getName(), dependencyCheck
							.getAddress(), throwable);
				})
				.recover(throwable -> DependencyCheckResult.builder()
						.endpoint(dependencyCheck.getName())
						.address(dependencyCheck.getAddress())
						.type(dependencyCheck.getType())
						.importance(dependencyCheck.getImportance())
						.result(dependencyCheck.getImportance()
								.stream()
								.anyMatch(importance -> importance.equals(Importance.CRITICAL)) ? Result.ERROR : Result.WARNING)
						.errorMessage("Call to dependency=" + dependencyCheck.getName() + " at url=" + dependencyCheck.getAddress() + " timed out or circuitbreaker tripped.")
						.build()
				);
	}
	
	private String elementToString(Element element) {
		try {
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(element);
			StreamResult result = new StreamResult(new StringWriter());
			transformer.transform(source, result);
			return result.getWriter().toString();
		} catch (TransformerException e) {
			throw new RuntimeException(String.format("Exception when converting Element to String. errorMsg=%s", e
					.getMessage()));
		}
	}
}
