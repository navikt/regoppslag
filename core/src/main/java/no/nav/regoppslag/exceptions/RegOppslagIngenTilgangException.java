package no.nav.regoppslag.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ResponseStatus(value = FORBIDDEN)
public class RegOppslagIngenTilgangException extends RegOppslagFunctionalException {
	public RegOppslagIngenTilgangException(String message, HttpStatus httpStatus) {
		super(message, httpStatus);
	}
}
