package no.nav.regoppslag.consumer.pdl.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import no.nav.regoppslag.consumer.pdl.to.HentPerson;

import java.util.List;

@Data
@Builder
public class PDLHentPersonResponse {

	private List<PdlError> errors;
	private PDLHentPerson data;

	@Setter
	@Getter
	@ToString
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class PDLHentPerson {
		private HentPerson hentPerson;
	}

	@Data
	public static class PdlError {
		private String message;
		private PdlErrorExtensionTo extensions;
	}

	@Data
	static class PdlErrorExtensionTo {
		private String code;
		private ErrorDetails details;
		private String classification;
	}

	@Data
	static class ErrorDetails {
		private String type;
		private String cause;
		private String policy;
	}
}
