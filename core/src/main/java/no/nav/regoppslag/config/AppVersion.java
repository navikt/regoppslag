package no.nav.regoppslag.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AppVersion {
	private final String version;

	@Autowired
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
