package no.nav.regoppslag.rest;

import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.service.RegOppslagService;
import no.nav.regoppslag.treg001.RegOppslagRequestTo;
import no.nav.regoppslag.treg001.RegOppslagResponseTo;
import org.springframework.http.MediaType;
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
	public RegisteroppslagRestController(RegOppslagService regOppslagService) {
		this.regOppslagService = regOppslagService;
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/kompletterBrevdata", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public RegOppslagResponseTo validerOgKompletterBrevdata(@RequestBody RegOppslagRequestTo requestBody)
			throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		return regOppslagService.hentBrevdataFraRegistre(requestBody);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/hentMottakerOgAddresse", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public RegOppslagResponseTo hentMottakerOgAddresse(@RequestBody RegOppslagRequestTo requestBody)
			throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		return regOppslagService.hentBrevdataFraRegistre(requestBody);
	}
	
}
