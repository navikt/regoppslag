package no.nav.regoppslag.xmlenricher.exceptions;

/**
 * @author Hans Petter Simonsen - Miles
 */
public class EnricherFunctionalException extends Exception {
	public EnricherFunctionalException(String message, Throwable cause) {
		super(message, cause);
	}

	public EnricherFunctionalException(String message) {
		super(message);
	}
}
