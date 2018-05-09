package no.nav.regoppslag.nais.checks;

import no.nav.regoppslag.config.fasit.OrganisasjonV4Alias;
import no.nav.regoppslag.nais.checkcore.AbstractDependencyCheck;
import no.nav.regoppslag.nais.checkcore.ApplicationNotReadyException;
import no.nav.regoppslag.nais.checkcore.DependencyType;
import no.nav.regoppslag.nais.checkcore.Importance;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.OrganisasjonV4;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class OrganisasjonV4Check extends AbstractDependencyCheck {
	public static final String ORGANISASJON_V4 = "Organisasjon_V4";
	private final OrganisasjonV4 organisasjonV4;

	@Inject
	public OrganisasjonV4Check(OrganisasjonV4 organisasjonV4, OrganisasjonV4Alias organisasjonV4Alias) {
		super(DependencyType.SOAP, Importance.WARNING, ORGANISASJON_V4, organisasjonV4Alias.getEndpointurl());
		
		this.organisasjonV4 = organisasjonV4;
	}

	@Override
	protected void doCheck() {
		try {
			organisasjonV4.ping();
		} catch (Exception e) {
			throw new ApplicationNotReadyException("Could not ping OrganisasjonV4", e);
		}
	}
}