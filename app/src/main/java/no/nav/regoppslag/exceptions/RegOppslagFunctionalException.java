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

	private final String metricMessage;

	public RegOppslagFunctionalException(String message, String metricMessage) {
		super(message);
		this.metricMessage = metricMessage;
	}

	public RegOppslagFunctionalException(String message) {
		super(message);
		metricMessage = this.getClass().getSimpleName();
	}

	public RegOppslagFunctionalException(String message, Throwable cause) {
		super(message, cause);
		metricMessage = this.getClass().getSimpleName();
	}

	public RegOppslagFunctionalException(String message, Throwable cause, String metricMessage) {
		super(message, cause);
		this.metricMessage = metricMessage;
	}

	public RegOppslagFunctionalException(Throwable cause) {
		super(cause);
		metricMessage = this.getClass().getSimpleName();
	}

	public RegOppslagFunctionalException(Throwable cause, String metricMessage) {
		super(cause);
		this.metricMessage = metricMessage;
	}
}
