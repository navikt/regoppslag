package no.nav.regoppslag.xmlenricher.exceptions;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hans Petter Simonsen - Miles
 */
public class MultiExceptionHolder extends Throwable {
	public MultiExceptionHolder(String message) {
		super(message);
	}

	List<Throwable> unhandledErrors = new ArrayList<>();

	public List<Throwable> getUnhandledErrors() {
		return unhandledErrors;
	}

	public void setUnhandledErrors(List<Throwable> unhandledErrors) {
		this.unhandledErrors = unhandledErrors;
	}
}
