package no.nav.regoppslag.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
@ResponseStatus(value = HttpStatus.FAILED_DEPENDENCY)
public class RegOppslagTechnicalException extends Exception {
	public RegOppslagTechnicalException() {
	}
	
	public RegOppslagTechnicalException(String message) {
		super(message);
	}
	
	public RegOppslagTechnicalException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public RegOppslagTechnicalException(Throwable cause) {
		super(cause);
	}
}
