package no.nav.regoppslag.nais.selftest;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Data
@Builder
public class SelftestResult {

	private String appName;
	private String version;
	private Result result;
	private List<DependencyCheckResult> dependencyCheckResults;
}
