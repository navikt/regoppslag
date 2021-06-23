package no.nav.regoppslag.exceptions;

public class PdlFunctionalException extends RuntimeException {

    public PdlFunctionalException(String message) {
        super(message);
    }

    public PdlFunctionalException(String message, Throwable cause) {
        super(message, cause);
    }
}
