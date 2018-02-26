package no.nav.regoppslag.xmlenricher.exceptions;

/**
 * @author Hans Petter Simonsen - Miles
 */
public class MissingPluginException extends EnricherFunctionalException {
	public MissingPluginException(String message, Throwable cause) {
		super(message, cause);
	}

	public MissingPluginException(String message) {
		super(message);
	}
}
