package no.nav.regoppslag.treg001.xmlenricher.exceptions;

/**
 * @author Hans Petter Simonsen - Miles
 */
public class InvalidElementException extends EnricherFunctionalException {
	public InvalidElementException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidElementException(String message) {
		super(message);
	}
}
