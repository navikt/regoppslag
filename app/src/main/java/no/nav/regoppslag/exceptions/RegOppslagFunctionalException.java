package no.nav.regoppslag.exceptions;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
public class RegOppslagFunctionalException extends Exception {
	public RegOppslagFunctionalException() {
	}
	
	public RegOppslagFunctionalException(String message) {
		super(message);
	}
	
	public RegOppslagFunctionalException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public RegOppslagFunctionalException(Throwable cause) {
		super(cause);
	}
}
