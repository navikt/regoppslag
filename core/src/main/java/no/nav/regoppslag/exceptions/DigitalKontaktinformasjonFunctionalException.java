package no.nav.regoppslag.exceptions;


import org.springframework.http.HttpStatusCode;

public class DigitalKontaktinformasjonFunctionalException extends RegOppslagFunctionalException {

	public DigitalKontaktinformasjonFunctionalException(String message, Throwable cause, HttpStatusCode httpStatus) {
		super(message, cause, httpStatus);
	}
}
