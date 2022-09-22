package no.nav.regoppslag.health;

import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.binding.OrganisasjonEnhetKontaktinformasjonV1;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class OrganisasjonEnhetKontaktinformasjonHealth implements HealthIndicator {

	private final OrganisasjonEnhetKontaktinformasjonV1 organisasjonEnhetKontaktinformasjonV1;

	public OrganisasjonEnhetKontaktinformasjonHealth(OrganisasjonEnhetKontaktinformasjonV1 organisasjonEnhetKontaktinformasjonV1) {
		this.organisasjonEnhetKontaktinformasjonV1 = organisasjonEnhetKontaktinformasjonV1;
	}

	@Override
	public Health health() {
		try {
			organisasjonEnhetKontaktinformasjonV1.ping();
			return Health.up().build();
		} catch (Exception e) {
			return Health.down(e).build();
		}
	}
}
