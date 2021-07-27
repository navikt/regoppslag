package no.nav.regoppslag.exceptions;

public class RegoppslagIllegalArgumentException extends AbstractRegOppslagFunctionalException{
	public RegoppslagIllegalArgumentException(String message) {
		super(message);
	}

	public RegoppslagIllegalArgumentException(String message, Throwable cause) {
		super(message, cause);
	}
}
