package no.nav.regoppslag.exceptions;

import lombok.Getter;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@ResponseStatus(value = UNAUTHORIZED)
@Getter
public class RegOppslagSecurityException extends RuntimeException {
	
	private String shortDescription = "RegOppslagSecurityException";
	
	public RegOppslagSecurityException(String message) {
		super(message);
	}
	
	public RegOppslagSecurityException(String message, String shortDescription) {
		super(message);
		this.shortDescription = shortDescription;
	}
	
	public RegOppslagSecurityException(Throwable cause) {
		super(cause);
	}
	
	public RegOppslagSecurityException(Throwable cause, String shortDescription) {
		super(cause);
		this.shortDescription = shortDescription;
	}
	
	public RegOppslagSecurityException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public RegOppslagSecurityException(String message, Throwable cause, String shortDescription) {
		super(message, cause);
		this.shortDescription = shortDescription;
	}
	
}
