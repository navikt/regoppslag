package no.nav.regoppslag.exceptions;

import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ResponseStatusException;

import static no.nav.regoppslag.util.NavHeaders.NAV_REASON_CODE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Getter
public class FeilGrunnetHoeytVolumWorkaroundException extends ResponseStatusException {

	private final String reasonCode;

	public FeilGrunnetHoeytVolumWorkaroundException(String message, String reasonCode) {
		super(BAD_REQUEST, message);
		this.reasonCode = reasonCode;
	}

	@Override
	public HttpHeaders getHeaders() {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add(NAV_REASON_CODE, reasonCode);
		return httpHeaders;
	}
}
