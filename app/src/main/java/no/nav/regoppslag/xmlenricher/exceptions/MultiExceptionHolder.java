package no.nav.regoppslag.xmlenricher.exceptions;

import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Hans Petter Simonsen - Miles
 * @author Jarl Øystein Samseth, Visma Consulting
 */
public class MultiExceptionHolder extends Throwable {
	private boolean hasFunctionalExceptions=false;
	public MultiExceptionHolder(String message) {
		super(message);
	}

	List<Throwable> unhandledErrors = new ArrayList<>();

	public List<Throwable> getUnhandledErrors() {
		return unhandledErrors;
	}

	public void setUnhandledErrors(List<Throwable> unhandledErrors) {
		this.unhandledErrors = unhandledErrors;
		setFunctionalExceptionFlag();
	}
	
	private void setFunctionalExceptionFlag() {
		if (unhandledErrors.stream().anyMatch(error -> error instanceof RegOppslagFunctionalException)) {
			hasFunctionalExceptions=true;
		}
	}
	
	public boolean hasFunctionExceptions() {
		return hasFunctionalExceptions;
	}
	
		
 	public String report() {
		if (!getUnhandledErrors().isEmpty()) {
			StringBuilder report = new StringBuilder();
			report.append("Antall feil: " + getUnhandledErrors().size() + ".\n\r");
			report.append(getAllMessages()); //Er dette godt nok for brukeren? Bør det også legges ved initCause?
			return report.toString();
		}
		return "No unhandled errors exist.";
	}
	
	private String getAllMessages() {
		return getUnhandledErrors().stream().map(Throwable::getMessage).collect(Collectors.joining(", \n\r","[","]"));
	}
	
	//All Messages med FirstCause exceptionClass og nummerering
	//All stacktraces
}
