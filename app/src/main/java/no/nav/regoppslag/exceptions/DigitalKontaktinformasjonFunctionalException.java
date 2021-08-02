package no.nav.regoppslag.exceptions;


import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DigitalKontaktinformasjonFunctionalException extends RegOppslagFunctionalException {

	public DigitalKontaktinformasjonFunctionalException(String message, Throwable cause, HttpStatus httpStatus) {
		super(message, cause, httpStatus);
	}
}
