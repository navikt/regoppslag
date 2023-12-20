package no.nav.regoppslag.exceptions;

import org.springframework.http.HttpStatusCode;

public class Norg2FunctionalException extends RegOppslagFunctionalException {

	public Norg2FunctionalException(String message, Throwable cause, HttpStatusCode httpStatusCode) {
		super(message, cause, httpStatusCode);
	}
}
