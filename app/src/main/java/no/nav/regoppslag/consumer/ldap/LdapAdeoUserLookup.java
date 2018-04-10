package no.nav.regoppslag.consumer.ldap;

import static no.nav.regoppslag.metrics.PrometheusLabels.CACHE_HIT;
import static no.nav.regoppslag.metrics.PrometheusLabels.CACHE_MISS;
import static no.nav.regoppslag.metrics.PrometheusLabels.SERVICE_CODE_TREG001;
import static no.nav.regoppslag.metrics.PrometheusMetrics.cacheCounter;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestLatency;

import io.prometheus.client.Histogram;
import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import org.springframework.cache.annotation.Cacheable;
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
	
	private final LdapTemplate ldapTemplate;
	private final String userBaseDn;
	private Histogram.Timer requestTimer;

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
	@Retryable(include = Exception.class, exclude = {RegOppslagFunctionalException.class }, maxAttempts = 5, backoff = @Backoff(delay = 200))
	public String hentFulltNavn(final String adeoIdent) throws RegOppslagFunctionalException {
		
		cacheCounter.labels(HENT_FULLT_NAVN, "LDAP", CACHE_HIT).dec();
		cacheCounter.labels(HENT_FULLT_NAVN, "LDAP", CACHE_MISS).inc();

		requestTimer = requestLatency.labels(SERVICE_CODE_TREG001, "LDAP", HENT_FULLT_NAVN).startTimer();

		LdapQuery cn = LdapQueryBuilder.query()
				.base(userBaseDn)
				.filter(new EqualsFilter("cn", adeoIdent));
		List<String> search = doSearch(cn);

		requestTimer.observeDuration();
		if (search == null || search.isEmpty()) {
			throw new RegOppslagFunctionalException("Ldap.hentFulltNavn finner ikke bruker med ident:" + adeoIdent);
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
