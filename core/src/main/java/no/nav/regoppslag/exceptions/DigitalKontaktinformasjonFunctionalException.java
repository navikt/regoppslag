package no.nav.regoppslag.exceptions;


import org.springframework.http.HttpStatusCode;

public class DigitalKontaktinformasjonFunctionalException extends RegOppslagFunctionalException {

	public DigitalKontaktinformasjonFunctionalException(String message, Throwable cause, String metricMessage, HttpStatusCode httpStatus) {
		super(message, cause, metricMessage, httpStatus);
	}
}
