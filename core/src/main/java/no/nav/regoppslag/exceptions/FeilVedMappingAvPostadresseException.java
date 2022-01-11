package no.nav.regoppslag.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class FeilVedMappingAvPostadresseException extends RegOppslagFunctionalException {
	public FeilVedMappingAvPostadresseException(String message, HttpStatus httpStatus) {
		super(message, httpStatus);
	}
}
