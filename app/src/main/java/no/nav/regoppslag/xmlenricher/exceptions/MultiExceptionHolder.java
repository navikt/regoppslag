package no.nav.regoppslag.xmlenricher.exceptions;

import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Hans Petter Simonsen - Miles
 * @author Jarl Øystein Samseth, Visma Consulting
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
	
	public boolean hasFunctionExceptions() {
		return unhandledErrors.stream().anyMatch(error -> error instanceof RegOppslagFunctionalException);
	}
	
	public boolean hasSecurityExceptions() {
		return unhandledErrors.stream().anyMatch(error -> error instanceof RegOppslagSecurityException);
	}
	
	public String report() {
		if (!getUnhandledErrors().isEmpty()) {
			StringBuilder report = new StringBuilder();
			report.append(this.getMessage());
			report.append(" Antall feil: " + getUnhandledErrors().size() + ". ");
			report.append("Feilmeldinger: ");
			report.append(getAllMessages());
			return report.toString();
		}
		return "Ingen ubehandlet feil eksiterer.";
	}
	
	private String getAllMessages() {
		return getUnhandledErrors().stream().map(throwable -> throwable.getClass().getSimpleName()+"; "+throwable.getMessage()).collect(Collectors.joining(", ","[","]"));
	}
	
}
