package no.nav.regoppslag.exceptions;

import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ResponseStatusException;

import static no.nav.regoppslag.util.NavHeaders.NAV_REASON_CODE;
import static org.springframework.http.HttpStatus.CONFLICT;

public class FalskIdentitetException extends ResponseStatusException {

	public static final String FALSK_IDENTITET_REASON_CODE = "falsk_identitet";
	public static final String FALSK_IDENTITET_FEILMELDING = "Kan ikke hente postadresse fordi identiteten er markert som falsk i PDL";

	public FalskIdentitetException() {
		super(CONFLICT, FALSK_IDENTITET_FEILMELDING);
	}

	@Override
	public HttpHeaders getHeaders() {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add(NAV_REASON_CODE, FALSK_IDENTITET_REASON_CODE);
		return httpHeaders;
	}
}
