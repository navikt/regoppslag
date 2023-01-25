package no.nav.regoppslag.treg001.xmlenricher.exceptions;

public class MissingKeyValueException extends EnricherFunctionalException {
	public MissingKeyValueException(String message, Throwable cause) {
		super(message, cause);
	}

	public MissingKeyValueException(String message) {
		super(message);
	}
}
