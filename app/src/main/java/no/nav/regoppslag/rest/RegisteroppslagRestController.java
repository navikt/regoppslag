package no.nav.regoppslag.rest;

import no.nav.regoppslag.config.sts.STSClientService;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.service.RegOppslagService;
import no.nav.regoppslag.treg001.RegOppslagRequestTo;
import no.nav.regoppslag.treg001.RegOppslagResponseTo;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
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
	
	private RegOppslagService regOppslagService;
	
	@Inject
	private STSClientService stsClientService;
	
	@Inject
	public RegisteroppslagRestController(RegOppslagService regOppslagService) {
		this.regOppslagService = regOppslagService;
	}
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ExceptionHandler({RegOppslagFunctionalException.class, RegOppslagTechnicalException.class})
	public RegOppslagResponseTo getKomplettBrevdata(@RequestBody RegOppslagRequestTo requestBody)
			throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		regOppslagService.hentBrevdataFraRegistre(requestBody);
		return RegOppslagResponseTo.builder().brevdata(requestBody.getBrevdata()).build();
	}
	
	
	@RequestMapping(value = "/token", method = RequestMethod.GET)
	public String pingTest(){
		return stsClientService.echo();
	}
}
