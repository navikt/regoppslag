package no.nav.regoppslag.rest;

import static no.nav.regoppslag.metrics.PrometheusLabels.SERVICE_CODE_TREG001;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestLatency;

import io.prometheus.client.Histogram;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.service.RegOppslagService;
import no.nav.regoppslag.treg001.RegOppslagRequestTo;
import no.nav.regoppslag.treg001.RegOppslagResponseTo;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */

@RestController
public class RegisteroppslagRestController {
	public static final String REGISTEROPPSLAG_URI_PATH = "/REST/registeroppslag";
	
	private RegOppslagService regOppslagService;
	private Histogram.Timer requestTimer;
	
	@Inject
	public RegisteroppslagRestController(RegOppslagService regOppslagService) {
		this.regOppslagService = regOppslagService;
	}
	
	@PostMapping(value = REGISTEROPPSLAG_URI_PATH,consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ExceptionHandler({RegOppslagFunctionalException.class, RegOppslagTechnicalException.class})
	public @ResponseBody RegOppslagResponseTo getKomplettBrevdata(@RequestBody RegOppslagRequestTo requestBody)
			throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		return regOppslagService.hentBrevdataFraRegistre(requestBody);
	}
	
}
