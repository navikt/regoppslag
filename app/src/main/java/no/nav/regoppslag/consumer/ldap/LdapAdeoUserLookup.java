package no.nav.regoppslag.consumer.ldap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;

import javax.naming.directory.Attribute;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
public class LdapAdeoUserLookup {

	public static final String DESCRIPTION = "description";
	public static final String DISPLAYNAME = "displayname";
	public static final String HENT_FULLT_NAVN = "hentFulltNavn";

	private final LdapTemplate ldapTemplate;
	private final String userBaseDn;

	public LdapAdeoUserLookup(LdapTemplate ldapTemplate, String userBaseDn) {
		this.ldapTemplate = ldapTemplate;
		this.userBaseDn = userBaseDn;
	}

	/**
	 * Gets the full name based on lookup in AD
	 *
	 * @param adeoIdent The NAV user ident
	 * @return The full name of the user or null if not found.
	 */
	@Cacheable(HENT_FULLT_NAVN)
	public String hentFulltNavn(final String adeoIdent)  {
		LdapQuery cn = LdapQueryBuilder.query()
				.base(userBaseDn)
				.filter(new EqualsFilter("cn", adeoIdent));
		List<String> search = doSearch(cn);
		if (search != null && !search.isEmpty()) {
			return search.get(0);
		} else {
			log.warn("Fant ikke navn for adeoIdent={}", adeoIdent);
			return null;
		}
	}

	private List<String> doSearch(LdapQuery cn) {
		return ldapTemplate.search(cn, (AttributesMapper<String>) attributes -> {
			// Description contains most consistent naming format
			Attribute description = attributes.get(DESCRIPTION);
			if (description != null) {
				return (String) description.get();
			}
			Attribute dname = attributes.get(DISPLAYNAME);
			if (dname != null) {
				return (String) dname.get();
			}
			return null;
		});
	}
}
