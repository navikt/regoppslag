package no.nav.regoppslag.treg001.xmlenricher.exceptions;

public class EnricherFunctionalException extends Exception {
	public EnricherFunctionalException(String message, Throwable cause) {
		super(message, cause);
	}

	public EnricherFunctionalException(String message) {
		super(message);
	}
}
