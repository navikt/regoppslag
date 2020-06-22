package no.nav.regoppslag.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@ResponseStatus(value = HttpStatus.GONE)
public class UkjentAdressePersonErDoed extends RegOppslagFunctionalException {
    public UkjentAdressePersonErDoed(String message) {
        super(message);
    }
}
