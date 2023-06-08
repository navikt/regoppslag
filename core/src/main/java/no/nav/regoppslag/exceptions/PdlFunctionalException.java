package no.nav.regoppslag.exceptions;

import org.springframework.http.HttpStatusCode;

public class PdlFunctionalException extends RegOppslagFunctionalException {

    public PdlFunctionalException(String message, HttpStatusCode httpStatusCode) {
        super(message, httpStatusCode);
    }

    public PdlFunctionalException(String message, Throwable cause, String metricMessage, HttpStatusCode httpStatusCode) {
        super(message, cause, metricMessage, httpStatusCode);
    }
}
