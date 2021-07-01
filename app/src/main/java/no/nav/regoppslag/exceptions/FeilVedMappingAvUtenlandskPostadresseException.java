package no.nav.regoppslag.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class FeilVedMappingAvUtenlandskPostadresseException extends AbstractRegOppslagFunctionalException {

    public FeilVedMappingAvUtenlandskPostadresseException(String message){
        super(message);
    }
}
