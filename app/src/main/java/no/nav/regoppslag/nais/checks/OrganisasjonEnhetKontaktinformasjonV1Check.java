package no.nav.regoppslag.nais.checks;

import no.nav.regoppslag.config.fasit.OrganisasjonEnhetKontaktinformasjonV1Alias;
import no.nav.regoppslag.nais.naiscontract.support.AbstractNaisIsReadyTest;
import no.nav.regoppslag.nais.naiscontract.support.ApplicationNotReadyException;
import no.nav.regoppslag.nais.naiscontract.support.Ping;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.binding.OrganisasjonEnhetKontaktinformasjonV1;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 * Not checked
 */
public class OrganisasjonEnhetKontaktinformasjonV1Check extends AbstractNaisIsReadyTest {
	public static final String OrganisasjonEnhetKontaktinformasjonV1_label = "OrganisasjonEnhetKontaktinformasjonV1";
	private final OrganisasjonEnhetKontaktinformasjonV1 organisasjonEnhetKontaktinformasjonV1;

	@Inject
	public OrganisasjonEnhetKontaktinformasjonV1Check(OrganisasjonEnhetKontaktinformasjonV1 organisasjonEnhetKontaktinformasjonV1, OrganisasjonEnhetKontaktinformasjonV1Alias alias) {
		super(Ping.Type.Soap,
				OrganisasjonEnhetKontaktinformasjonV1_label,
				alias.getEndpointurl(),
				alias.getDescription() == null ? OrganisasjonEnhetKontaktinformasjonV1_label : alias.getDescription());
		this.organisasjonEnhetKontaktinformasjonV1 = organisasjonEnhetKontaktinformasjonV1;
	}
	
	@Override
	public boolean isVital() {
		return true;
	}
	
	@Override
	protected void doCheck() {
		try {
			organisasjonEnhetKontaktinformasjonV1.ping();
		} catch (Exception e) {
			throw new ApplicationNotReadyException("Could not ping OrganisasjonEnhetKontaktinformasjonV1", e);
		}
	}
}