package no.nav.regoppslag.nais;

import static no.nav.regoppslag.metrics.PrometheusMetrics.isReady;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.nais.checks.OrganisasjonEnhetKontaktinformasjonV1Check;
import no.nav.regoppslag.nais.checks.OrganisasjonV4Check;
import no.nav.regoppslag.nais.checks.PersonV3Check;
import no.nav.regoppslag.nais.selftest.support.ApplicationNotReadyException;
import no.nav.regoppslag.nais.selftest.support.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */

@Slf4j
@RestController
public class NaisContract {
	
	public static final String APPLICATION_ALIVE = "Application is alive!";
	public static final String APPLICATION_READY = "Application is ready for traffic!";
	private static final int MAX_READY_FAIL = 3;
	public static final String ROUTE_SUSPENDED = "Suspended";
	public static final String ROUTE_STARTED = "Started";
	
	private final PersonV3Check personV3Check;
	private final OrganisasjonV4Check organisasjonV4Check;
	private final OrganisasjonEnhetKontaktinformasjonV1Check organisasjonEnhetKontaktinformasjonV1Check;
	
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
	public ResponseEntity isReady() {
		List<Result> results=new ArrayList<>();
		try {
			//TODO: Denne vil ikke feil hvis det skjer noe feil. Fiks det
			results.add(personV3Check.check().getResult());
			results.add(organisasjonV4Check.check().getResult());
			results.add(organisasjonEnhetKontaktinformasjonV1Check.check().getResult());
			isReady.set(1);
			
		} catch (ApplicationNotReadyException e) {
			String errorMsg = "Application not ready to accept traffic.";
			log.error(errorMsg, e);
			isReady.dec();
			return new ResponseEntity<>(errorMsg + " reason=" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		if (results.stream().filter((result) -> {return result.equals(Result.ERROR)||result.equals(Result.WARNING);}).count()>0){
			String errorMsg = "Application not ready to accept traffic.";
			isReady.dec();
			return new ResponseEntity<>(errorMsg, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return new ResponseEntity<>(APPLICATION_READY, HttpStatus.OK);
	}
	
	
	


}
