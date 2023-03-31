package no.nav.regoppslag.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.GONE;

@Getter
@ResponseStatus(value = GONE)
public class UkjentAdressePersonErDoed extends RegOppslagFunctionalException {

	public UkjentAdressePersonErDoed(String message, HttpStatus httpStatus) {
		super(message, httpStatus);
	}

	public UkjentAdressePersonErDoed(String message, Throwable cause, String metricMessage, HttpStatus httpStatus) {
		super(message, cause, metricMessage, httpStatus);
	}
}
