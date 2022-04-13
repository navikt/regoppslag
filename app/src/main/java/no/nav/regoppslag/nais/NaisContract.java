package no.nav.regoppslag.nais;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.config.AppVersion;
import no.nav.regoppslag.config.nais.NaisCheckSTSTokenRetriever;
import no.nav.regoppslag.nais.selftest.AbstractDependencyCheck;
import no.nav.regoppslag.nais.selftest.DependencyCheckResult;
import no.nav.regoppslag.nais.selftest.Importance;
import no.nav.regoppslag.nais.selftest.Result;
import no.nav.regoppslag.nais.selftest.SelftestResult;
import no.nav.security.token.support.core.api.Unprotected;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.springframework.security.core.authority.AuthorityUtils.NO_AUTHORITIES;

@Slf4j
@RestController
@Unprotected
public class NaisContract {

	private static final String APPLICATION_ALIVE = "Application is alive!";
	private static final String APPLICATION_READY = "Application is ready for traffic!";
	private static final String APPLICATION_NOT_READY = "Application is not ready for traffic :-(";
	private static final String APP_NAME = "regoppslag";
	private static AtomicInteger isReady = new AtomicInteger(1);

	private final String version;
	private final List<AbstractDependencyCheck> dependencyCheckList;

	@Autowired
	private NaisCheckSTSTokenRetriever naisCheckSTSTokenRetriever;

	@Autowired
	public NaisContract(MeterRegistry meterRegistry,
						List<AbstractDependencyCheck> dependencyCheckList,
						AppVersion version) {
		Gauge.builder("dok_app_is_ready", isReady, AtomicInteger::get).register(meterRegistry);
		this.dependencyCheckList = new ArrayList<>(dependencyCheckList);
		this.version = version.getVersion();
	}

	@GetMapping("/isAlive")
	public String isAlive() {
		return APPLICATION_ALIVE;
	}

	@RequestMapping(value = "/isReady")
	public ResponseEntity isReady() throws Exception {
		List<DependencyCheckResult> results = new ArrayList<>();

		checkCriticalDependencies(results);

		if (isAnyVitalDependencyUnhealthy(results.stream()
				.map(DependencyCheckResult::getResult)
				.collect(Collectors.toList()))) {
			isReady.set(0);
			return new ResponseEntity<>(APPLICATION_NOT_READY, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		isReady.set(1);

		return new ResponseEntity<>(APPLICATION_READY, HttpStatus.OK);
	}

	@GetMapping(value = "/internal/selftest", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	SelftestResult selftest() throws Exception {
		List<DependencyCheckResult> results = new ArrayList<>();
		checkDependencies(results);
		return SelftestResult.builder()
				.appName(APP_NAME)
				.version(version)
				.dependencyCheckResults(results)
				.result(getOverallSelftestResult(results))
				.build();
	}

	private boolean isAnyVitalDependencyUnhealthy(List<Result> results) {
		return results.stream().anyMatch(result -> result.equals(Result.ERROR));
	}

	private Result getOverallSelftestResult(List<DependencyCheckResult> results) {
		if (results.stream().anyMatch(result -> result.getResult().equals(Result.ERROR))) {
			return Result.ERROR;
		} else if (results.stream().anyMatch(result -> result.getResult().equals(Result.WARNING))) {
			return Result.WARNING;
		}
		return Result.OK;
	}

	private void checkCriticalDependencies(List<DependencyCheckResult> results) throws Exception {
		UsernamePasswordAuthenticationToken authenticationToken = getSTSAuthenticationToken();

		Flowable.fromIterable(dependencyCheckList)
				.filter(dependency -> dependency.getImportance().equals(Importance.CRITICAL))
				.parallel()
				.runOn(Schedulers.io())
				.map(payload -> payload.check(authenticationToken).get())
				.sequential().blockingSubscribe(results::add);
	}

	private void checkDependencies(List<DependencyCheckResult> results) throws Exception {
		UsernamePasswordAuthenticationToken authenticationToken = getSTSAuthenticationToken();

		Flowable.fromIterable(dependencyCheckList)
				.parallel()
				.runOn(Schedulers.io())
				.map(payload -> payload.check(authenticationToken).get())
				.sequential().blockingSubscribe(results::add);
	}

	private UsernamePasswordAuthenticationToken getSTSAuthenticationToken() throws Exception {
		String decodedToken = naisCheckSTSTokenRetriever.requestStsToken();
		return new UsernamePasswordAuthenticationToken("NaisIsReadySamlToken", decodedToken, NO_AUTHORITIES);
	}

}