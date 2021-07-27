package no.nav.regoppslag.exceptions;

public class DigitalKontaktinformasjonTechnicalException extends AbstractRegOppslagTechnicalException {
	public DigitalKontaktinformasjonTechnicalException(String message) {
		super(message);
	}

	public DigitalKontaktinformasjonTechnicalException(String message, Throwable cause) {
		super(message, cause);
	}
}
