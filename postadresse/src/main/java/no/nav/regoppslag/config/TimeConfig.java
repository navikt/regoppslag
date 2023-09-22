package no.nav.regoppslag.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class TimeConfig {

	public static final ZoneId OSLO_ZONE = ZoneId.of("Europe/Oslo");

	@Bean
	public Clock systemOsloNorwayClock() {
		return Clock.system(OSLO_ZONE);
	}
}
