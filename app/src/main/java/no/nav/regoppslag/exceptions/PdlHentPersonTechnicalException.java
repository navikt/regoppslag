package no.nav.regoppslag.exceptions;

public class PdlHentPersonTechnicalException extends AbstractRegOppslagTechnicalException {

    public PdlHentPersonTechnicalException(String message) {
        super(message);
    }

    public PdlHentPersonTechnicalException(String message, Throwable cause) {
        super(message, cause);
    }
}
