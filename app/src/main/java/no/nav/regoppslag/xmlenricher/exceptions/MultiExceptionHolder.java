package no.nav.regoppslag.xmlenricher.exceptions;

import lombok.Getter;
import lombok.Setter;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Hans Petter Simonsen - Miles
 * @author Jarl Øystein Samseth, Visma Consulting
 */
@Getter
@Setter
public class MultiExceptionHolder extends Throwable {
	private boolean hasFunctionalExceptions = false;
	
	public MultiExceptionHolder(String message) {
		super(message);
	}
	
	private List<Throwable> unhandledErrors = new ArrayList<>();
	
	public boolean hasFunctionalExceptions() {
		return unhandledErrors.stream().anyMatch(error -> error instanceof RegOppslagFunctionalException);
	}
	
	public boolean hasSecurityExceptions() {
		return unhandledErrors.stream().anyMatch(error -> error instanceof RegOppslagSecurityException);
	}
	
	public String report() {
		if (!getUnhandledErrors().isEmpty()) {
			StringBuilder report = new StringBuilder();
			report.append(this.getMessage())
					.append(" Antall feil: " + getUnhandledErrors().size() + ". ")
					.append("Feilmeldinger: ")
					.append(getAllMessages());
			return report.toString();
		}
		return "Ingen ubehandlet feil eksiterer.";
	}
	
	private String getAllMessages() {
		return getUnhandledErrors().stream()
				.map(throwable -> throwable.getClass().getSimpleName() + "; " + throwable.getMessage())
				.collect(Collectors.joining(", ", "[", "]"));
	}
	
}


