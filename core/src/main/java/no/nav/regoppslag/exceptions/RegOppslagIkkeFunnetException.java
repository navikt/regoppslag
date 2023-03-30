package no.nav.regoppslag.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@ResponseStatus(value = NOT_FOUND)
public class RegOppslagIkkeFunnetException extends RegOppslagFunctionalException {

	public RegOppslagIkkeFunnetException(String message, String metricMessage, HttpStatus httpStatus) {
		super(message, metricMessage, httpStatus);
	}

	public RegOppslagIkkeFunnetException(String message, HttpStatus httpStatus) {
		super(message, httpStatus);
	}

	public RegOppslagIkkeFunnetException(String message, Throwable cause, String metricMessage, HttpStatus httpStatus) {
		super(message, cause, metricMessage, httpStatus);
	}


}
