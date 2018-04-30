package no.nav.regoppslag.nais.checkcore;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Builder
@Data
public class DependencyCheckResult {
	private String endpoint;
	private String address;
	private String errorMessage;
	private List<Importance> importance;
	private Result result;
	private DependencyType type;
}
