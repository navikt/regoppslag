package no.nav.regoppslag.consumer.pdl.to;

import java.util.List;

public record PDLHentNavnResponse(no.nav.regoppslag.consumer.pdl.to.PDLHentNavnResponse.PDLHentPerson data,
								  List<PDLError> errors) {

	public record PDLHentPerson(HentPerson hentPerson) {
	}

	public record HentPerson(List<PersonNavn> navn) {
	}
}
