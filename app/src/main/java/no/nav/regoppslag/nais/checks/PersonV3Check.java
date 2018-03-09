package no.nav.regoppslag.nais.checks;

import no.nav.regoppslag.config.fasit.PersonV3Alias;
import no.nav.regoppslag.nais.selftest.support.AbstractSelftest;
import no.nav.regoppslag.nais.selftest.support.ApplicationNotReadyException;
import no.nav.regoppslag.nais.selftest.support.Ping;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class PersonV3Check extends AbstractSelftest {
	public static final String PERSON_V3 = "Person_V3";
	private final PersonV3 personV3;

	@Inject
	public PersonV3Check(PersonV3 personV3, PersonV3Alias personV3Alias) {
		super(Ping.Type.Soap,
				PERSON_V3,
				personV3Alias.getEndpointurl(),
				personV3Alias.getDescription() == null ? PERSON_V3 : personV3Alias.getDescription());
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