package no.nav.regoppslag.nais.checks;

import no.nav.regoppslag.config.fasit.OrganisasjonV4Alias;
import no.nav.regoppslag.nais.selftest.support.AbstractSelftest;
import no.nav.regoppslag.nais.selftest.support.ApplicationNotReadyException;
import no.nav.regoppslag.nais.selftest.support.Ping;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.OrganisasjonV4;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class OrganisasjonV4Check extends AbstractSelftest {
	public static final String ORGANISASJON_V4 = "Organisasjon_v4";
	private final OrganisasjonV4 organisasjonV4;

	@Inject
	public OrganisasjonV4Check(OrganisasjonV4 organisasjonV4, OrganisasjonV4Alias organisasjonV4Alias) {
		super(Ping.Type.Soap,
				ORGANISASJON_V4,
				organisasjonV4Alias.getEndpointurl(),
				organisasjonV4Alias.getDescription() == null ? ORGANISASJON_V4 : organisasjonV4Alias.getDescription());
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