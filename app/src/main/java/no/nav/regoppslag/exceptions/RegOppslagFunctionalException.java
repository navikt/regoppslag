package no.nav.regoppslag.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
@Getter
public abstract class RegOppslagFunctionalException extends RuntimeException {

	private final String metricMessage;
	private final HttpStatus httpStatus;

	public RegOppslagFunctionalException(String message, String metricMessage, HttpStatus httpStatus) {
		super(message);
		this.metricMessage = metricMessage;
		this.httpStatus = httpStatus;
	}

	public RegOppslagFunctionalException(String message, HttpStatus httpStatus) {
		super(message);
		metricMessage = this.getClass().getSimpleName();
		this.httpStatus = httpStatus;
	}

	public RegOppslagFunctionalException(String message, Throwable cause, HttpStatus httpStatus) {
		super(message, cause);
		metricMessage = this.getClass().getSimpleName();
		this.httpStatus = httpStatus;
	}

	public RegOppslagFunctionalException(String message, Throwable cause, String metricMessage, HttpStatus httpStatus) {
		super(message, cause);
		this.metricMessage = metricMessage;
		this.httpStatus = httpStatus;
	}

	public RegOppslagFunctionalException(Throwable cause, HttpStatus httpStatus) {
		super(cause);
		metricMessage = this.getClass().getSimpleName();
		this.httpStatus = httpStatus;
	}

	public RegOppslagFunctionalException(Throwable cause, String metricMessage, HttpStatus httpStatus) {
		super(cause);
		this.metricMessage = metricMessage;
		this.httpStatus = httpStatus;
	}

}
