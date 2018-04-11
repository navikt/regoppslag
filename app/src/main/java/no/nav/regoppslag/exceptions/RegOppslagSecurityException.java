package no.nav.regoppslag.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class RegOppslagSecurityException extends Exception {
	
	public RegOppslagSecurityException(String message) {
		super(message);
	}
	
	public RegOppslagSecurityException(Throwable cause) {
		super(cause);
	}
	
	public RegOppslagSecurityException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
