package no.nav.regoppslag.consumer.ldap;

import static no.nav.regoppslag.metrics.PrometheusLabels.CACHE_COUNTER;
import static no.nav.regoppslag.metrics.PrometheusLabels.CACHE_MISS;
import static no.nav.regoppslag.metrics.PrometheusLabels.LDAP;
import static no.nav.regoppslag.metrics.PrometheusLabels.SERVICE_CODE_TREG001;
import static no.nav.regoppslag.metrics.PrometheusMetrics.getConsumerId;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestCounter;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestLatency;

import io.prometheus.client.Histogram;
import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
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
	@Retryable(include = Exception.class, exclude = {RegOppslagFunctionalException.class}, maxAttempts = 5, backoff = @Backoff(delay = 200))
	public String hentFulltNavn(final String adeoIdent) throws RegOppslagFunctionalException, RegOppslagTechnicalException {
		
		requestCounter.labels(SERVICE_CODE_TREG001, HENT_FULLT_NAVN, CACHE_COUNTER, getConsumerId(), CACHE_MISS).inc();
		
		try {
			requestTimer = requestLatency.labels(SERVICE_CODE_TREG001, LDAP, HENT_FULLT_NAVN).startTimer();
			
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
				throw new RegOppslagFunctionalException("Ldap.hentFulltNavn finner ikke bruker med ident:" + adeoIdent, BRUKER_IKKE_FUNNET);
			} else {
				return search.get(0);
			}
			
		} finally {
			requestTimer.observeDuration();
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
