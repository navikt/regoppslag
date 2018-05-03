package no.nav.regoppslag.nais.checks;

import no.nav.regoppslag.nais.checkcore.AbstractDependencyCheck;
import no.nav.regoppslag.nais.checkcore.ApplicationNotReadyException;
import no.nav.regoppslag.nais.checkcore.DependencyType;
import no.nav.regoppslag.nais.checkcore.Importance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class LDAPCheck extends AbstractDependencyCheck {
	public static final String LDAP_LABEL = "LDAP";
	private final LdapTemplate ldapTemplate;
	
	@Inject
	public LDAPCheck(LdapTemplate ldapTemplate,
					 @Value("${ldap_url}") String ldapUrl) {
		super(DependencyType.LDAP, Importance.CRITICAL, LDAP_LABEL, ldapUrl);
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