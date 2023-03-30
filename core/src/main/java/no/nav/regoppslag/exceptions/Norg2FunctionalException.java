package no.nav.regoppslag.exceptions;

import org.springframework.http.HttpStatus;

public class Norg2FunctionalException extends RegOppslagFunctionalException {

	public Norg2FunctionalException(Throwable cause, String metricMessage, HttpStatus httpStatus) {
		super(cause, metricMessage, httpStatus);
	}
}
