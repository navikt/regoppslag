package no.nav.regoppslag.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
@Getter
public class RegOppslagTechnicalException extends RuntimeException {

	private final String metricMessage;
	private HttpStatus httpStatus = HttpStatus.OK;

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

	public RegOppslagTechnicalException(String message, Throwable cause, String metricMessage, HttpStatus httpStatus) {
		super(message, cause);
		this.metricMessage = metricMessage;
		this.httpStatus = httpStatus;
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
