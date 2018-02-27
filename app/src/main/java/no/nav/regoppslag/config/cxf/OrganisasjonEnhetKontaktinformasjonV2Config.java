package no.nav.regoppslag.config.cxf;

import static no.nav.regoppslag.ldap.LdapAdeoUserLookup.HENT_FULLT_NAVN;
import static no.nav.regoppslag.norg2.OrganisasjonEnhetKontaktinformasjonV2Consumer.HENT_ENHET_NAVN;

import no.nav.modig.security.ws.SystemSAMLOutInterceptor;
import no.nav.regoppslag.config.fasit.NavAppCertAlias;
import no.nav.regoppslag.config.fasit.OrganisasjonKontaktinformasjonV2Alias;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v2.binding.OrganisasjonEnhetKontaktinformasjonV2;
import org.apache.cxf.Bus;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.namespace.QName;
import java.util.Arrays;

/**
 * Spring config for OrganisasjonEnhetKontaktinformasjonV2 CXF Endpoint
 *
 * @author Ketill Fenne, Visma Consulting
 */
@Configuration
@EnableCaching
public class OrganisasjonEnhetKontaktinformasjonV2Config  extends AbstractCxfEndpointConfig {
	private static final String NAMESPACE = "http://nav.no/tjeneste/virksomhet/organisasjonEnhetKontaktinformasjon/v2/Binding";

	private static final QName ORG_KONTAKTINFO_V2_PORT_QNAME = new QName(NAMESPACE, "OrganisasjonEnhetKontaktinformasjon_v2Port");
	private static final QName ORG_KONTAKTINFO_V2_SERVICE_QNAME = new QName(NAMESPACE, "OrganisasjonEnhetKontaktinformasjon_v2");

	public static final String WSDL_URL = "tjenestespesifikasjon/no/nav/tjeneste/virksomhet/organisasjonEnhetKontaktinformasjon/v2/Binding.wsdl";

	@Bean
	public OrganisasjonEnhetKontaktinformasjonV2 organisasjonEnhetKontaktinformasjonV2(OrganisasjonKontaktinformasjonV2Alias organisasjonKontaktinformasjonV2Alias, NavAppCertAlias navAppCertAlias) {
		navAppCertAlias.postConstruct();

		setWsdlUrl(WSDL_URL);
		setEndpointName(ORG_KONTAKTINFO_V2_PORT_QNAME);
		setServiceName(ORG_KONTAKTINFO_V2_SERVICE_QNAME);
		setAdress(organisasjonKontaktinformasjonV2Alias.getEndpointurl());
		setReceiveTimeout(organisasjonKontaktinformasjonV2Alias.getReadtimeoutms());
		setConnectTimeout(organisasjonKontaktinformasjonV2Alias.getConnecttimeoutms());
		addOutInterceptor(new SystemSAMLOutInterceptor());
		addFeature(new WSAddressingFeature());
		return createPort(OrganisasjonEnhetKontaktinformasjonV2.class);
	}

	@Bean
	public CacheManager cacheManager() {
		// configure and return an implementation of Spring's CacheManager SPI
		SimpleCacheManager cacheManager = new SimpleCacheManager();
		cacheManager.setCaches(Arrays.asList(new ConcurrentMapCache(HENT_ENHET_NAVN)));
		return cacheManager;
	}
}
