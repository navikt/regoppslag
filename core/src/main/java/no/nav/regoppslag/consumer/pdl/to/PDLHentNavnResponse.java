package no.nav.regoppslag.consumer.pdl.to;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PDLHentNavnResponse {

	private PDLHentPerson data;
	private List<PDLError> errors;

	@Data
	public static class PDLHentPerson {
		private HentPerson hentPerson;
	}

	@Data
	public static class HentPerson {
		private List<PersonNavn> navn;
	}
}
