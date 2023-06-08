package no.nav.regoppslag.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;

@ResponseStatus(value = INTERNAL_SERVER_ERROR)
@Getter
public class RegOppslagTechnicalException extends RuntimeException {

	private final String metricMessage;
	private HttpStatusCode httpStatusCode = OK;

	public RegOppslagTechnicalException() {
		this.metricMessage = this.getClass().getSimpleName();
	}

	public RegOppslagTechnicalException(String message) {
		super(message);
		this.metricMessage = this.getClass().getSimpleName();
	}

	public RegOppslagTechnicalException(String message, String metricMessage) {
		super(message);
		this.metricMessage = metricMessage;
	}

	public RegOppslagTechnicalException(String message, Throwable cause, String metricMessage) {
		super(message, cause);
		this.metricMessage = metricMessage;
	}

	public RegOppslagTechnicalException(String message, Throwable cause, String metricMessage, HttpStatusCode httpStatusCode) {
		super(message, cause);
		this.metricMessage = metricMessage;
		this.httpStatusCode = httpStatusCode;
	}

	public RegOppslagTechnicalException(String message, Throwable cause) {
		super(message, cause);
		this.metricMessage = this.getClass().getSimpleName();
	}

	public RegOppslagTechnicalException(Throwable cause) {
		super(cause);
		this.metricMessage = this.getClass().getSimpleName();
	}

	public RegOppslagTechnicalException(Throwable cause, String metricMessage) {
		super(cause);
		this.metricMessage = metricMessage;
	}

}
