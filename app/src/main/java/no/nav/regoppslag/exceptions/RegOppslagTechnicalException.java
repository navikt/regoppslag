package no.nav.regoppslag.exceptions;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
public class RegOppslagTechnicalException extends Exception {
	public RegOppslagTechnicalException() {
	}
	
	public RegOppslagTechnicalException(String message) {
		super(message);
	}
	
	public RegOppslagTechnicalException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public RegOppslagTechnicalException(Throwable cause) {
		super(cause);
	}
}
