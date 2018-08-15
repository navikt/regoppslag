package no.nav.regoppslag.nais.checks;

import no.nav.regoppslag.config.fasit.OrganisasjonEnhetKontaktinformasjonV1Alias;
import no.nav.regoppslag.nais.selftest.AbstractDependencyCheck;
import no.nav.regoppslag.nais.selftest.ApplicationNotReadyException;
import no.nav.regoppslag.nais.selftest.DependencyType;
import no.nav.regoppslag.nais.selftest.Importance;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.binding.OrganisasjonEnhetKontaktinformasjonV1;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class OrganisasjonEnhetKontaktinformasjonV1Check extends AbstractDependencyCheck {
	public static final String OrganisasjonEnhetKontaktinformasjonV1_label = "NORG2";
	private final OrganisasjonEnhetKontaktinformasjonV1 organisasjonEnhetKontaktinformasjonV1;

	@Inject
	public OrganisasjonEnhetKontaktinformasjonV1Check(OrganisasjonEnhetKontaktinformasjonV1 organisasjonEnhetKontaktinformasjonV1, OrganisasjonEnhetKontaktinformasjonV1Alias alias) {
		super(DependencyType.SOAP, OrganisasjonEnhetKontaktinformasjonV1_label, alias.getEndpointurl(), Importance.WARNING);

		this.organisasjonEnhetKontaktinformasjonV1 = organisasjonEnhetKontaktinformasjonV1;
	}

	@Override
	protected void doCheck() {
		try {
			organisasjonEnhetKontaktinformasjonV1.ping();
		} catch (Exception e) {
			throw new ApplicationNotReadyException("Could not ping NORG2. ErrorMessage="+e.getMessage(), e);
		}
	}
}