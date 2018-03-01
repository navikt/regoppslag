package no.nav.regoppslag.rest;

import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.service.RegOppslagService;
import no.nav.regoppslag.treg001.RegOppslagRequest;
import no.nav.regoppslag.treg001.RegOppslagResponse;
import org.springframework.http.MediaType;
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
	public static final String REGISTEROPPSLAG_URI_PATH = "/REST/registeroppslag";
	
	private RegOppslagService regOppslagService;
	
	@Inject
	public RegisteroppslagRestController(RegOppslagService regOppslagService) {
		this.regOppslagService = regOppslagService;
	}
	
	@PostMapping(value = REGISTEROPPSLAG_URI_PATH,consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ExceptionHandler({RegOppslagFunctionalException.class, RegOppslagTechnicalException.class})
	public @ResponseBody RegOppslagResponse getKomplettBrevdata(@RequestBody RegOppslagRequest requestBody)
			throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		return regOppslagService.hentBrevdataFraRegistre(requestBody);
	}
}
