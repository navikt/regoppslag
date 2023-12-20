package no.nav.regoppslag.exceptions;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ResponseStatus(value = BAD_REQUEST)
public class RegoppslagIllegalArgumentException extends RegOppslagFunctionalException {

	public RegoppslagIllegalArgumentException(String message, HttpStatusCode httpStatusCode) {
		super(message, httpStatusCode);
	}

	public RegoppslagIllegalArgumentException(String message, Throwable cause, HttpStatusCode httpStatusCode) {
		super(message, cause, httpStatusCode);
	}
}
