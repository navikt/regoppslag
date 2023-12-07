package no.nav.regoppslag.exceptions;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@ResponseStatus(value = FORBIDDEN)
public class RegOppslagIngenTilgangException extends RegOppslagFunctionalException {

	public RegOppslagIngenTilgangException(String message, HttpStatusCode httpStatusCode) {
		super(message, httpStatusCode);
	}
}
