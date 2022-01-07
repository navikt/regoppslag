package no.nav.regoppslag.treg001.xmlenricher.exceptions;

/**
 * @author Hans Petter Simonsen - Miles
 */
public class MissingKeyValueException extends EnricherFunctionalException {
	public MissingKeyValueException(String message, Throwable cause) {
		super(message, cause);
	}

	public MissingKeyValueException(String message) {
		super(message);
	}
}
