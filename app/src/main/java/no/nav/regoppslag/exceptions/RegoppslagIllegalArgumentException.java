package no.nav.regoppslag.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class RegoppslagIllegalArgumentException extends RegOppslagFunctionalException {

	public RegoppslagIllegalArgumentException(String message, HttpStatus httpStatus) {
		super(message, httpStatus);
	}

	public RegoppslagIllegalArgumentException(String message, Throwable cause, String metricMessage, HttpStatus httpStatus) {
		super(message, cause, metricMessage, httpStatus);
	}
}
