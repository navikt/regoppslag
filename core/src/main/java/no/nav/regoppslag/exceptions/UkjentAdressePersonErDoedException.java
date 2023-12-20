package no.nav.regoppslag.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.GONE;

@Getter
@ResponseStatus(value = GONE)
public class UkjentAdressePersonErDoedException extends RegOppslagFunctionalException {

	public UkjentAdressePersonErDoedException(String message, HttpStatusCode httpStatusCode) {
		super(message, httpStatusCode);
	}
}
