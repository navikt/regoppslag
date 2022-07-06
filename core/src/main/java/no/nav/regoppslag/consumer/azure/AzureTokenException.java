package no.nav.regoppslag.consumer.azure;

public class AzureTokenException extends RuntimeException {
	public AzureTokenException(String message, Throwable cause) {
		super(message, cause);
	}
}
