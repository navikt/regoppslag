package no.nav.regoppslag.nais.checkcore;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@RequiredArgsConstructor
public final class ReadyResult {
	private final String message;
	private final List<DependencyCheckResult> criticalSelftests;
}
