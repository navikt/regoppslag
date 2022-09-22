package no.nav.regoppslag.health;

import no.nav.regoppslag.config.fasit.DokumenttypeInfoV3Alias;
import no.nav.regoppslag.config.fasit.ServiceuserAlias;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Component
public class DokumenttypeInfoHealth implements HealthIndicator {

	private final RestTemplate restTemplate;

	@Autowired
	public DokumenttypeInfoHealth(RestTemplateBuilder restTemplateBuilder,
								  DokumenttypeInfoV3Alias dokumenttypeInfoV3Alias,
								  ServiceuserAlias serviceuserAlias) {
		this.restTemplate = restTemplateBuilder
				.rootUri(dokumenttypeInfoV3Alias.getUrl())
				.basicAuthentication(serviceuserAlias.getUsername(), serviceuserAlias.getPassword())
				.setConnectTimeout(Duration.ofMillis(dokumenttypeInfoV3Alias.getConnecttimeoutms()))
				.setReadTimeout(Duration.ofMillis(dokumenttypeInfoV3Alias.getReadtimeoutms()))
				.build();
	}

	@Override
	public Health health() {
		try {
			restTemplate.getForObject("/ping", String.class);
			return Health.up().build();
		} catch (Exception e) {
			return Health.down(e).build();
		}
	}
}
