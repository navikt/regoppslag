package no.nav.regoppslag.consumer.ldap;

import static no.nav.regoppslag.metrics.MetricLabels.DOK_CONSUMER;
import static no.nav.regoppslag.metrics.MetricLabels.PROCESS_CODE;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.metrics.Metrics;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.ldap.ServiceUnavailableException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

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
	public static final String BRUKER_IKKE_FUNNET = "LDAP - Bruker ikke funnet";
	
	private final LdapTemplate ldapTemplate;
	private final String userBaseDn;
	private MicrometerMetrics metrics;

	public LdapAdeoUserLookup(LdapTemplate ldapTemplate, String userBaseDn, MicrometerMetrics metrics) {
		this.ldapTemplate = ldapTemplate;
		this.userBaseDn = userBaseDn;
		this.metrics = metrics;
	}
	
	/**
	 * Gets the full name based on lookup in AD
	 *
	 * @param adeoIdent The NAV user ident
	 * @return The full name of the user or null if not found.
	 */
	@Cacheable(value = HENT_FULLT_NAVN, key = "#adeoIdent")
	@Retryable(include = Exception.class, exclude = {RegOppslagFunctionalException.class}, maxAttempts = 5, backoff = @Backoff(delay = 200))
	@Metrics(value = DOK_CONSUMER, extraTags = {PROCESS_CODE, HENT_FULLT_NAVN}, percentiles = {0.5, 0.95}, histogram = true)
	public String hentFulltNavn(final String adeoIdent) throws RegOppslagFunctionalException, RegOppslagTechnicalException {

		metrics.cacheMiss(HENT_FULLT_NAVN);

		LdapQuery cn = LdapQueryBuilder.query()
				.base(userBaseDn)
				.filter(new EqualsFilter("cn", adeoIdent));

		List<String> search;

		try {
			search = doSearch(cn);
		} catch (ServiceUnavailableException e) {
			log.warn("Feil ved kall mot LDAP", e);
			throw new RegOppslagTechnicalException(String.format("Noe gikk galt ved kall mot LDAP.hentFulltNavn. Feilmelding=%s, AdeoIdent=%s", e
					.getMessage(), adeoIdent), "LDAP - Teknisk feil");
		}

		if (search == null || search.isEmpty()) {
			throw new RegOppslagFunctionalException(String.format("Ldap.hentFulltNavn finner ikke bruker med ident=%s", adeoIdent), BRUKER_IKKE_FUNNET);
		} else {
			return search.get(0);
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
