package no.nav.regoppslag.nais.checks;

import no.nav.regoppslag.config.fasit.PersonV3Alias;
import no.nav.regoppslag.nais.checkcore.AbstractDependencyCheck;
import no.nav.regoppslag.nais.checkcore.ApplicationNotReadyException;
import no.nav.regoppslag.nais.checkcore.DependencyType;
import no.nav.regoppslag.nais.checkcore.Importance;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class PersonV3Check extends AbstractDependencyCheck {
	public static final String PERSONV3_LABEL = "Person_V3";
	private final PersonV3 personV3;

	@Inject
	public PersonV3Check(PersonV3 personV3, PersonV3Alias personV3Alias) {
		super(DependencyType.SOAP, Importance.CRITICAL, PERSONV3_LABEL, personV3Alias.getEndpointurl());
		this.personV3 = personV3;
	}
	

	@Override
	protected void doCheck() {
		try {
			personV3.ping();
		} catch (Exception e) {
			throw new ApplicationNotReadyException("Could not ping PersonV3", e);
		}
	}
}