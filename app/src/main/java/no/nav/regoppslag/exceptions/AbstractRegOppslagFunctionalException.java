package no.nav.regoppslag.exceptions;

public abstract class AbstractRegOppslagFunctionalException extends RuntimeException {

    public AbstractRegOppslagFunctionalException(String message) {
        super(message);
    }

    public AbstractRegOppslagFunctionalException(String message, Throwable cause) {
        super(message, cause);
    }
}
