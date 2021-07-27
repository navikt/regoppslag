package no.nav.regoppslag.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
@Getter
public class IngenGyldigEnumVerdiForSpraakKodeException extends AbstractRegOppslagTechnicalException {

	public IngenGyldigEnumVerdiForSpraakKodeException(String message) {
		super(message);
	}
}
