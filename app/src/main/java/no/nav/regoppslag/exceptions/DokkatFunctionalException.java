package no.nav.regoppslag.exceptions;

import lombok.Getter;

/**
 * @author Ugur Alpay Cenar, Visma Consulting
 */
@Getter
public class DokkatFunctionalException extends Exception {
	
	private String shortDescription = "DokkatFunctionalException";
	
	public DokkatFunctionalException() {
	}
	
	public DokkatFunctionalException(String message, Throwable cause, String shortDescription) {
		super(message, cause);
		this.shortDescription = shortDescription;
	}
	
}
