package no.nav.regoppslag.exceptions;

import org.springframework.http.HttpStatus;

public class PdlFunctionalException extends RegOppslagFunctionalException {

    public PdlFunctionalException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }

    public PdlFunctionalException(String message, Throwable cause, String metricMessage, HttpStatus httpStatus) {
        super(message, cause, metricMessage, httpStatus);
    }
}
