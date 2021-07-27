package no.nav.regoppslag.exceptions;

public abstract class AbstractRegOppslagTechnicalException extends RuntimeException {

	public AbstractRegOppslagTechnicalException(String message) {
		super(message);
	}

	public AbstractRegOppslagTechnicalException(String message, Throwable cause) {
		super(message, cause);
	}

}
