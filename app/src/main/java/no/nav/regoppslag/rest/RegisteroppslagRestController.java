package no.nav.regoppslag.rest;

import no.nav.regoppslag.treg001.RegOppslagRequestTo;
import no.nav.regoppslag.treg001.RegOppslagResponseTo;
import no.nav.regoppslag.xmlenricher.exceptions.MultiExceptionHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */

@RestController
public class RegisteroppslagRestController {
	
	@PostMapping(consumes = {"application/JSON"}, produces = {"application/JSON"})
	@ExceptionHandler(MultiExceptionHolder.class)
	public @ResponseBody RegOppslagResponseTo getKomplettBrevdata(@RequestBody RegOppslagRequestTo requestBody) {
		//TODO: kalle på orcestrator
		return RegOppslagResponseTo.builder().brevdata(requestBody.getBrevdata()).build();
	}
}
