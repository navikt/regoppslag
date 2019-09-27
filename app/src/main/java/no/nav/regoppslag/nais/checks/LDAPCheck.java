package no.nav.regoppslag.nais.checks;

import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.regoppslag.nais.selftest.AbstractDependencyCheck;
import no.nav.regoppslag.nais.selftest.ApplicationNotReadyException;
import no.nav.regoppslag.nais.selftest.DependencyType;
import no.nav.regoppslag.nais.selftest.Importance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class LDAPCheck extends AbstractDependencyCheck {
	private final LdapTemplate ldapTemplate;
	
	@Inject
	public LDAPCheck(LdapTemplate ldapTemplate,
					 @Value("${ldap_url}") String ldapUrl,
					 MicrometerMetrics metrics) {
		super(DependencyType.LDAP, "LDAP", ldapUrl, Importance.WARNING, metrics);
		this.ldapTemplate = ldapTemplate;
	}
	
	@Override
	protected void doCheck() {
		try {
			ldapTemplate.lookup("");
		} catch (Exception e) {
			throw new ApplicationNotReadyException("Could not ping LDAP", e);
		}
	}
}