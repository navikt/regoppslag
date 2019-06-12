package no.nav.regoppslag.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
@Getter
public class RegOppslagTechnicalException extends Exception {

	private final String shortDescription;
	private HttpStatus httpStatus = HttpStatus.OK;

	public RegOppslagTechnicalException() {
		this.shortDescription = this.getClass().getSimpleName();
	}

	public RegOppslagTechnicalException(String message) {
		super(message);
		this.shortDescription = this.getClass().getSimpleName();
	}

	public RegOppslagTechnicalException(String message, String shortDescription) {
		super(message);
		this.shortDescription = shortDescription;
	}

	public RegOppslagTechnicalException(String message, Throwable cause, String shortDescription) {
		super(message, cause);
		this.shortDescription = shortDescription;
	}


	public RegOppslagTechnicalException(String message, Throwable cause, String shortDescription, HttpStatus httpStatus) {
		super(message, cause);
		this.shortDescription = shortDescription;
		this.httpStatus = httpStatus;
	}

	public RegOppslagTechnicalException(String message, Throwable cause) {
		super(message, cause);
		this.shortDescription = this.getClass().getSimpleName();
	}

	public RegOppslagTechnicalException(Throwable cause) {
		super(cause);
		this.shortDescription = this.getClass().getSimpleName();
	}


	public RegOppslagTechnicalException(Throwable cause, String shortDescription) {
		super(cause);
		this.shortDescription = shortDescription;
	}

}
