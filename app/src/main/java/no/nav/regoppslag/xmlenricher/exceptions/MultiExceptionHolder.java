package no.nav.regoppslag.xmlenricher.exceptions;

import lombok.Getter;
import lombok.Setter;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;

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
	public MultiExceptionHolder(String message) {
		super(message);
	}

	private List<Throwable> unhandledErrors = new ArrayList<>();

	public boolean hasFunctionalExceptions() {
		return unhandledErrors.stream().anyMatch(error -> error instanceof RegOppslagFunctionalException);
	}

	public String report() {
		if (!getUnhandledErrors().isEmpty()) {
			StringBuilder report = new StringBuilder();
			report.append(this.getMessage()).append("\n\rAntall feil: " + getUnhandledErrors().size() + ".\n\r").append("Feilmeldinger: ").append(getAllMessages());
			return report.toString();
		}
		return "No unhandled errors exist.";
	}

	private String getAllMessages() {
		return getUnhandledErrors().stream().map(throwable -> throwable.getClass().getSimpleName() + "; " + throwable.getMessage()).collect(Collectors.joining(", ", "[", "]"));
	}

}
