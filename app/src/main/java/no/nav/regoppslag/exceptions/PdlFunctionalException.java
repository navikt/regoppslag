package no.nav.regoppslag.exceptions;

public class PdlFunctionalException extends AbstractRegOppslagFunctionalException {

    public PdlFunctionalException(String message) {
        super(message);
    }

    public PdlFunctionalException(String message, Throwable cause) {
        super(message, cause);
    }
}
