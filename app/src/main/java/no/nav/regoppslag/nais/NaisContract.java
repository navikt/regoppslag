package no.nav.regoppslag.nais;

import static no.nav.regoppslag.metrics.PrometheusMetrics.isReady;
import static no.nav.regoppslag.nais.naiscontract.support.SelftestSTSConfig.STS_CACHE_NAME;
import static org.springframework.security.core.authority.AuthorityUtils.NO_AUTHORITIES;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.nais.checks.PersonV3Check;
import no.nav.regoppslag.nais.naiscontract.support.AbstractNaisIsReadyTest;
import no.nav.regoppslag.nais.naiscontract.support.Result;
import no.nav.regoppslag.nais.naiscontract.support.SelftestCheck;
import org.apache.cxf.ws.security.trust.STSClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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
import java.util.ArrayList;
import java.util.List;
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
	
	private final List<AbstractNaisIsReadyTest> checkList;
	@Inject
	private STSClient stsClient;
	
	@Inject
	public NaisContract(List<AbstractNaisIsReadyTest> checks) {
		checkList = new ArrayList<>(checks);
	}

	@GetMapping("/isAlive")
	public String isAlive() {
		return APPLICATION_ALIVE;
	}

	@ResponseBody
	@RequestMapping(value = "/isReady", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity isReady() throws Exception {
		try {
			
			List<SelftestCheck> results = new ArrayList<>();
			
			checkDependencies(results);
			
			if (isAnyVitalDependencyUnhealthy(results.stream().map(SelftestCheck::getResult).collect(Collectors.toList()))) {
				isReady.dec();
				log.error("IsReady check failed. ErrorMsg=" + results.stream()
						.filter(selftestCheck -> selftestCheck.getErrorMessage() != null)
						.map(selftestCheck -> selftestCheck.getName() + ": " + selftestCheck.getErrorMessage())
						.collect(Collectors.toList()));
				return new ResponseEntity<>(APPLICATION_NOT_READY, HttpStatus.INTERNAL_SERVER_ERROR);
			}

			isReady.set(1);

			return new ResponseEntity<>(APPLICATION_READY, HttpStatus.OK);
		} finally {
			SecurityContextHolder.clearContext();
		}
	}
	
	private void checkDependencies(List<SelftestCheck> results) throws Exception {
		UsernamePasswordAuthenticationToken authenticationToken = getSTSAuthenticationToken();
		
		Flowable.fromIterable(checkList)
				.parallel()
				.runOn(Schedulers.io())
				.map(payload -> {
							if (payload instanceof PersonV3Check) {
								return payload.check(authenticationToken);
							}
							return payload.check(null);
						}
				).sequential().blockingSubscribe(results::add);
	}
	
	private UsernamePasswordAuthenticationToken getSTSAuthenticationToken() throws Exception {
		String decodedToken = requestStsToken();
		return new UsernamePasswordAuthenticationToken("NaisIsReadySamlToken", decodedToken, NO_AUTHORITIES);
	}
	
	
	private boolean isAnyVitalDependencyUnhealthy(List<Result> results) {
		return results.stream().anyMatch((result) -> result.equals(Result.ERROR));
	}
	
	
	private boolean isAnyNonVitalDependencyUnhealthy(List<Result> results) {
		return results.stream().anyMatch((result) -> result.equals(Result.WARNING));
	}
	
	
	
	@Cacheable(value = STS_CACHE_NAME, key = "#methodName")
	public String requestStsToken() throws Exception {
		return elementToString(stsClient.requestSecurityToken().getToken());
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
