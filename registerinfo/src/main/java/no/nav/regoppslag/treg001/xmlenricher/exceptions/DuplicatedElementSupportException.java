package no.nav.regoppslag.treg001.xmlenricher.exceptions;

public class DuplicatedElementSupportException extends EnricherFunctionalException {
	public DuplicatedElementSupportException(String message, Throwable cause) {
		super(message, cause);
	}

	public DuplicatedElementSupportException(String message) {
		super(message);
	}
}
