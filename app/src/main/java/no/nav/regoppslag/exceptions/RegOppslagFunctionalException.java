package no.nav.regoppslag.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
@Getter
public class RegOppslagFunctionalException extends Exception {
	
	private String shortDescription = "RegOppslagFunctionalException";
	
	public RegOppslagFunctionalException() {
	}
	
	public RegOppslagFunctionalException(String message, String shortDescription) {
		super(message);
		this.shortDescription = shortDescription;
	}
	
	public RegOppslagFunctionalException(String message) {
		super(message);
	}
	
	public RegOppslagFunctionalException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public RegOppslagFunctionalException(String message, Throwable cause, String shortDescription) {
		super(message, cause);
		this.shortDescription = shortDescription;
	}
	
	public RegOppslagFunctionalException(Throwable cause) {
		super(cause);
	}
	
	public RegOppslagFunctionalException(Throwable cause, String shortDescription) {
		super(cause);
		this.shortDescription = shortDescription;
	}
}
