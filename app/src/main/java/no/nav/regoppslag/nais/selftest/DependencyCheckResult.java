package no.nav.regoppslag.nais.selftest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Builder
@Data
public class DependencyCheckResult {
	private String endpoint;
	private Result result;
	private String address;
	private String errorMessage;
	private DependencyType type;
	private Importance importance;
	private String responseTime;

	@JsonIgnore
	private Throwable throwable;

}
