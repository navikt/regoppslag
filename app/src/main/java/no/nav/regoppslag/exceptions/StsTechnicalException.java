package no.nav.regoppslag.exceptions;


public class StsTechnicalException extends RuntimeException {
    public StsTechnicalException(String message) {
        super(message);
    }

    public StsTechnicalException(String message, Throwable cause) {
        super(message, cause);
    }
}
