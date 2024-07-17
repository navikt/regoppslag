package no.nav.regoppslag.exceptions;

import lombok.Getter;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@ResponseStatus(value = INTERNAL_SERVER_ERROR)
@Getter
public class RegOppslagTechnicalException extends RuntimeException {

	private final boolean retryable;

	public RegOppslagTechnicalException(String message, Throwable cause) {
		this(message, cause, true);
	}

	public RegOppslagTechnicalException(Throwable cause) {
		this(cause.getMessage(), cause, true);
	}

	public RegOppslagTechnicalException(String message, Throwable cause, boolean retryable) {
		super(message, cause);
		this.retryable = retryable;
	}
}
