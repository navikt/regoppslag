package no.nav.regoppslag.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class IngenGyldigEnumVerdiForSpraakKodeException extends RegOppslagFunctionalException {

	public IngenGyldigEnumVerdiForSpraakKodeException(String message, HttpStatus httpStatus) {
		super(message, httpStatus);
	}
}
