package no.nav.regoppslag.consumer.digdirkrr;

import java.util.List;

public record PostPersonerRequest(List<String> personidenter) {

	public PostPersonerRequest(String personident) {
		this(List.of(personident));
	}
}
