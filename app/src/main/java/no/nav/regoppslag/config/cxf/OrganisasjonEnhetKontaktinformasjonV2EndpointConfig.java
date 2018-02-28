package no.nav.regoppslag.config.cxf;

import no.nav.modig.security.ws.SystemSAMLOutInterceptor;
import no.nav.regoppslag.config.fasit.NavAppCertAlias;
import no.nav.regoppslag.config.fasit.OrganisasjonEnhetKontaktinformasjonV2Alias;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v2.OrganisasjonEnhetKontaktinformasjonV2;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.namespace.QName;

/**
 * Spring config for OrganisasjonEnhetKontaktinformasjonV2 CXF Endpoint
 *
 * @author Ketill Fenne, Visma Consulting
 */
@Configuration
public class OrganisasjonEnhetKontaktinformasjonV2EndpointConfig extends AbstractCxfEndpointConfig {
	private static final String NAMESPACE = "http://nav.no/tjeneste/virksomhet/organisasjonEnhetKontaktinformasjon/v2/Binding";

	private static final QName ORG_KONTAKTINFO_V2_PORT_QNAME = new QName(NAMESPACE, "OrganisasjonEnhetKontaktinformasjon_v2Port");
	private static final QName ORG_KONTAKTINFO_V2_SERVICE_QNAME = new QName(NAMESPACE, "OrganisasjonEnhetKontaktinformasjon_v2");

	public static final String WSDL_URL = "tjenestespesifikasjon/no/nav/tjeneste/virksomhet/organisasjonEnhetKontaktinformasjon/v2/Binding.wsdl";

	@Bean
	public OrganisasjonEnhetKontaktinformasjonV2 organisasjonEnhetKontaktinformasjonV2(OrganisasjonEnhetKontaktinformasjonV2Alias organisasjonEnhetKontaktinformasjonV2Alias, NavAppCertAlias navAppCertAlias) {
		navAppCertAlias.postConstruct();

		setWsdlUrl(WSDL_URL);
		setEndpointName(ORG_KONTAKTINFO_V2_PORT_QNAME);
		setServiceName(ORG_KONTAKTINFO_V2_SERVICE_QNAME);
		setAdress(organisasjonEnhetKontaktinformasjonV2Alias.getEndpointurl());
		setReceiveTimeout(organisasjonEnhetKontaktinformasjonV2Alias.getReadtimeoutms());
		setConnectTimeout(organisasjonEnhetKontaktinformasjonV2Alias.getConnecttimeoutms());
		addOutInterceptor(new SystemSAMLOutInterceptor());
		addFeature(new WSAddressingFeature());
		return createPort(OrganisasjonEnhetKontaktinformasjonV2.class);
	}
}
