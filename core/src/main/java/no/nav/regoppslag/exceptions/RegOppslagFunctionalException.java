package no.nav.regoppslag.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class RegOppslagFunctionalException extends RuntimeException {

	private final String metricMessage;
	private final HttpStatusCode httpStatusCode;

	public RegOppslagFunctionalException(String message, String metricMessage, HttpStatusCode httpStatusCode) {
		super(message);
		this.metricMessage = metricMessage;
		this.httpStatusCode = httpStatusCode;
	}

	public RegOppslagFunctionalException(String message, HttpStatusCode httpStatusCode) {
		super(message);
		metricMessage = this.getClass().getSimpleName();
		this.httpStatusCode = httpStatusCode;
	}

	public RegOppslagFunctionalException(String message, Throwable cause, HttpStatusCode httpStatusCode) {
		super(message, cause);
		metricMessage = this.getClass().getSimpleName();
		this.httpStatusCode = httpStatusCode;
	}

	public RegOppslagFunctionalException(String message, Throwable cause, String metricMessage, HttpStatusCode httpStatusCode) {
		super(message, cause);
		this.metricMessage = metricMessage;
		this.httpStatusCode = httpStatusCode;
	}

	public RegOppslagFunctionalException(Throwable cause, HttpStatusCode httpStatusCode) {
		super(cause);
		metricMessage = this.getClass().getSimpleName();
		this.httpStatusCode = httpStatusCode;
	}

	public RegOppslagFunctionalException(Throwable cause, String metricMessage, HttpStatusCode httpStatusCode) {
		super(cause);
		this.metricMessage = metricMessage;
		this.httpStatusCode = httpStatusCode;
	}

}
