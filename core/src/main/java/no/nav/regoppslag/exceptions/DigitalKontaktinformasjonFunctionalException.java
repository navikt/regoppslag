package no.nav.regoppslag.exceptions;


import org.springframework.http.HttpStatus;

public class DigitalKontaktinformasjonFunctionalException extends RegOppslagFunctionalException {

	public DigitalKontaktinformasjonFunctionalException(String message, Throwable cause, String metricMessage, HttpStatus httpStatus) {
		super(message, cause, metricMessage, httpStatus);
	}
}
