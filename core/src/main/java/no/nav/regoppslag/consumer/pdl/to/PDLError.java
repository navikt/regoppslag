package no.nav.regoppslag.consumer.pdl.to;

import lombok.Data;

@Data
public class PDLError {
	private String message;
	private ErrorExtensions extensions;

	@Data
	public static class ErrorExtensions {
		private String code;
		private ErrorDetails details;
		private String classification;
	}

	@Data
	public static class ErrorDetails {
		private String type;
		private String cause;
		private String policy;
	}
}