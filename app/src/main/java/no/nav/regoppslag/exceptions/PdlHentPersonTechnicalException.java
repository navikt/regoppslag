package no.nav.regoppslag.exceptions;

public class PdlHentPersonTechnicalException extends RuntimeException {

    public PdlHentPersonTechnicalException(String message) {
        super(message);
    }

    public PdlHentPersonTechnicalException(String message, Throwable cause) {
        super(message, cause);
    }
}
