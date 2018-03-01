package no.nav.regoppslag.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class RegOppslagFunctionalException extends Exception {
	public RegOppslagFunctionalException() {
	}
	
	public RegOppslagFunctionalException(String message) {
		super(message);
	}
	
	public RegOppslagFunctionalException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public RegOppslagFunctionalException(Throwable cause) {
		super(cause);
	}
}
