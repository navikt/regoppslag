package no.nav.regoppslag.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;

@ResponseStatus(value = INTERNAL_SERVER_ERROR)
@Getter
public class RegOppslagTechnicalException extends RuntimeException {

	private HttpStatusCode httpStatusCode = OK;

	public RegOppslagTechnicalException() {
	}

	public RegOppslagTechnicalException(String message) {
		super(message);
	}

	public RegOppslagTechnicalException(String message, Throwable cause) {
		super(message, cause);
	}

	public RegOppslagTechnicalException(String message, Throwable cause, HttpStatusCode httpStatusCode) {
		super(message, cause);
		this.httpStatusCode = httpStatusCode;
	}

	public RegOppslagTechnicalException(Throwable cause) {
		super(cause);
	}

}
