package no.nav.regoppslag.pdl;

import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class MapPDLUtils {
	public static <T> T requireNonNull(T obj, String message) {
		if (obj == null)
			throw new RegoppslagIllegalArgumentException(message, BAD_REQUEST);
		return obj;
	}
}
