package no.nav.regoppslag.rest;

import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.service.RegOppslagService;
import no.nav.regoppslag.treg001.Orchestrator;
import no.nav.regoppslag.treg001.RegOppslagRequestTo;
import no.nav.regoppslag.treg001.RegOppslagResponseTo;
import no.nav.regoppslag.xmlenricher.exceptions.MultiExceptionHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */

@RestController
public class RegisteroppslagRestController {
	
	private RegOppslagService regOppslagService;
	
	@Inject
	public RegisteroppslagRestController(RegOppslagService regOppslagService) {
		this.regOppslagService = regOppslagService;
	}
	
	@PostMapping(consumes = {"application/JSON"}, produces = {"application/JSON"})
	@ExceptionHandler({MultiExceptionHolder.class,RegOppslagFunctionalException.class, RegOppslagTechnicalException.class})
	public @ResponseBody RegOppslagResponseTo getKomplettBrevdata(@RequestBody RegOppslagRequestTo requestBody) throws RegOppslagFunctionalException, RegOppslagTechnicalException, MultiExceptionHolder {
		regOppslagService.hentBrevdataFraRegistre(requestBody);
		return RegOppslagResponseTo.builder().brevdata(requestBody.getBrevdata()).build();
	}
}
