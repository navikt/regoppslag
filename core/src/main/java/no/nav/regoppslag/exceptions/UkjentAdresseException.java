package no.nav.regoppslag.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@ResponseStatus(value = NOT_FOUND)
@Getter
public class UkjentAdresseException extends RegOppslagFunctionalException {

    public UkjentAdresseException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }

}
