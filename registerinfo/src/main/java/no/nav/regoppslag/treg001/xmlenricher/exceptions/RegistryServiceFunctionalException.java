package no.nav.regoppslag.treg001.xmlenricher.exceptions;

public class RegistryServiceFunctionalException extends EnricherFunctionalException {
	public RegistryServiceFunctionalException(String message, Throwable cause) {
		super(message, cause);
	}

	public RegistryServiceFunctionalException(String message) {
		super(message);
	}
}
