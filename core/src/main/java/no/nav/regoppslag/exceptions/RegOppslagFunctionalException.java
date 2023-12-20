package no.nav.regoppslag.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class RegOppslagFunctionalException extends RuntimeException {

	private final HttpStatusCode httpStatusCode;

	public RegOppslagFunctionalException(String message, HttpStatusCode httpStatusCode) {
		super(message);
		this.httpStatusCode = httpStatusCode;
	}

	public RegOppslagFunctionalException(String message, Throwable cause, HttpStatusCode httpStatusCode) {
		super(message, cause);
		this.httpStatusCode = httpStatusCode;
	}

	public RegOppslagFunctionalException(Throwable cause, HttpStatusCode httpStatusCode) {
		super(cause);
		this.httpStatusCode = httpStatusCode;
	}

}
