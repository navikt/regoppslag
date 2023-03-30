package no.nav.regoppslag.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ResponseStatus(value = BAD_REQUEST)
public class RegOppslagParsingException extends RegOppslagFunctionalException {

	public RegOppslagParsingException(String message, Throwable cause, HttpStatus httpStatus) {
		super(message, cause, httpStatus);
	}
}
