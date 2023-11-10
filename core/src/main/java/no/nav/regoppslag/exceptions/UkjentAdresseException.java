package no.nav.regoppslag.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@ResponseStatus(value = NOT_FOUND)
@Getter
public class UkjentAdresseException extends RegOppslagFunctionalException {

    public UkjentAdresseException(String message, HttpStatusCode httpStatusCode) {
        super(message, httpStatusCode);
    }

}
