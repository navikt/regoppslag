package no.nav.regoppslag.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Optional;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class AppVersion {
	private final String version;

	@Inject
	AppVersion(ApplicationContext applicationContext) {
		version = applicationContext.getBeansWithAnnotation(SpringBootApplication.class)
				.entrySet().stream().findFirst().flatMap(stringObjectEntry -> {
					final String implementationVersion = stringObjectEntry.getValue().getClass().getPackage().getImplementationVersion();
					return Optional.ofNullable(implementationVersion);
				}).orElse("unknownVersion");
	}

	public String getVersion() {
		return version;
	}
}
