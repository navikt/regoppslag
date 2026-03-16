package no.nav.regoppslag.consumer.pdl.to;

import lombok.Value;

@Value
public class PDLError {
	String message;
	ErrorExtensions extensions;

	@Value
	public static class ErrorExtensions {
		String code;
		ErrorDetails details;
		String classification;
	}

	@Value
	public static class ErrorDetails {
		String type;
		String cause;
		String policy;
	}
}
