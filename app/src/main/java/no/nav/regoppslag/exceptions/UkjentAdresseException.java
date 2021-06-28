package no.nav.regoppslag.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class UkjentAdresseException extends AbstractRegOppslagFunctionalException {
    public UkjentAdresseException(String message) {
        super(message);
    }
}
