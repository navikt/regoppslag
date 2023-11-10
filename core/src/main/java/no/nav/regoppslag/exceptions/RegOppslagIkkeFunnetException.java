package no.nav.regoppslag.exceptions;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@ResponseStatus(value = NOT_FOUND)
public class RegOppslagIkkeFunnetException extends RegOppslagFunctionalException {

	public RegOppslagIkkeFunnetException(String message, String metricMessage, HttpStatusCode httpStatusCode) {
		super(message, metricMessage, httpStatusCode);
	}

	public RegOppslagIkkeFunnetException(String message, HttpStatusCode httpStatusCode) {
		super(message, httpStatusCode);
	}

	public RegOppslagIkkeFunnetException(String message, Throwable cause, String metricMessage, HttpStatusCode httpStatusCode) {
		super(message, cause, metricMessage, httpStatusCode);
	}
}
