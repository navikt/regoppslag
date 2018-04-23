package no.nav.regoppslag.nais;

import static no.nav.regoppslag.metrics.PrometheusMetrics.isReady;
import static org.springframework.security.core.authority.AuthorityUtils.NO_AUTHORITIES;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.nais.checks.OrganisasjonEnhetKontaktinformasjonV1Check;
import no.nav.regoppslag.nais.checks.OrganisasjonV4Check;
import no.nav.regoppslag.nais.checks.PersonV3Check;
import no.nav.regoppslag.nais.selftest.support.Result;
import no.nav.regoppslag.nais.selftest.support.SelftestCheck;
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
	public static final String STS_CACHE_NAME = "STS_CACHE_NAME";

	private final PersonV3Check personV3Check;
	private final OrganisasjonV4Check organisasjonV4Check;
	private final OrganisasjonEnhetKontaktinformasjonV1Check organisasjonEnhetKontaktinformasjonV1Check;

	@Inject
	private STSClient stsClient;
	
	@Inject
	public NaisContract(PersonV3Check personV3Check, OrganisasjonV4Check organisasjonV4Check, OrganisasjonEnhetKontaktinformasjonV1Check organisasjonEnhetKontaktinformasjonV1Check) {
		this.personV3Check = personV3Check;
		this.organisasjonV4Check = organisasjonV4Check;
		this.organisasjonEnhetKontaktinformasjonV1Check = organisasjonEnhetKontaktinformasjonV1Check;
	}

	@GetMapping("/isAlive")
	public String isAlive() {
		return APPLICATION_ALIVE;
	}

	@ResponseBody
	@RequestMapping(value = "/isReady", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity isReady() throws Exception {
		try {
			String decodedToken = requestStsToken();
			UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken("SAMLtoken", decodedToken, NO_AUTHORITIES);
			

			List<SelftestCheck> results = new ArrayList<>();
			results.add(personV3Check.check(authRequest));
			results.add(organisasjonV4Check.check(null));
			results.add(organisasjonEnhetKontaktinformasjonV1Check.check(null));
			
			if (isAnyDependencyUnhealthy(results.stream().map(SelftestCheck::getResult).collect(Collectors.toList()))) {
				isReady.labels("APP").dec();
				String responseBody = APPLICATION_NOT_READY + "/n +  " +  results.stream().map(SelftestCheck::getErrorMessage).collect(Collectors.toList());
				return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
			}
			
			isReady.labels("APP").set(1);

			return new ResponseEntity<>(APPLICATION_READY, HttpStatus.OK);
		} finally {
			SecurityContextHolder.clearContext();
		}
	}


	private boolean isAnyDependencyUnhealthy(List<Result> results) {
		return results.stream().anyMatch((result) -> result.equals(Result.ERROR) || result.equals(Result.WARNING));
	}
	
	@Cacheable(value = STS_CACHE_NAME)
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
			throw new RuntimeException(String.format("Exception when converting Element to String in RegoppslagServiceMapper. errorMsg=%s", e
					.getMessage()));
		}
	}
}
