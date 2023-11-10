package no.nav.regoppslag.exceptions;

import org.springframework.http.HttpStatusCode;

public class Norg2FunctionalException extends RegOppslagFunctionalException {

	public Norg2FunctionalException(Throwable cause, String metricMessage, HttpStatusCode httpStatusCode) {
		super(cause, metricMessage, httpStatusCode);
	}
}
