package no.nav.regoppslag.consumer.pdl.to;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PDLHentNavnResponse {

	private PDLHentPerson data;
	private List<PdlError> errors;

	@Data
	public static class PDLHentPerson {
		private HentPerson hentPerson;
	}

	@Data
	public static class HentPerson {
		private List<PersonNavn> navn;
	}

	@Data
	public static class PdlError {
		private String message;
		private PDLHentPersonResponse.PdlErrorExtensionTo extensions;
	}

	@Data
	static class PdlErrorExtensionTo {
		private String code;
		private PDLHentPersonResponse.ErrorDetails details;
		private String classification;
	}

	@Data
	static class ErrorDetails {
		private String type;
		private String cause;
		private String policy;
	}
}
