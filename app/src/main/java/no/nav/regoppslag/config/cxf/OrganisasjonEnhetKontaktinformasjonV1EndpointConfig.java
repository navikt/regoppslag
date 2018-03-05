package no.nav.regoppslag.config.cxf;

import no.nav.modig.security.ws.SystemSAMLOutInterceptor;
import no.nav.regoppslag.config.fasit.NavAppCertAlias;
import no.nav.regoppslag.config.fasit.OrganisasjonEnhetKontaktinformasjonV1Alias;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.binding.OrganisasjonEnhetKontaktinformasjonV1;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.namespace.QName;

/**
 * Spring config for OrganisasjonEnhetKontaktinformasjonV1 CXF Endpoint
 *
 * @author Ketill Fenne, Visma Consulting
 */
@Configuration
public class OrganisasjonEnhetKontaktinformasjonV1EndpointConfig extends AbstractCxfEndpointConfig {
	private static final String NAMESPACE = "http://nav.no/tjeneste/virksomhet/organisasjonEnhetKontaktinformasjon/v1/Binding";

	private static final QName ORG_KONTAKTINFO_V2_PORT_QNAME = new QName(NAMESPACE, "OrganisasjonEnhetKontaktinformasjon_v1Port");
	private static final QName ORG_KONTAKTINFO_V2_SERVICE_QNAME = new QName(NAMESPACE, "OrganisasjonEnhetKontaktinformasjon_v1");

	public static final String WSDL_URL = "tjenestespesifikasjon/no/nav/tjeneste/virksomhet/organisasjonEnhetKontaktinformasjon/v1/Binding.wsdl";

	@Bean
	public OrganisasjonEnhetKontaktinformasjonV1 organisasjonEnhetKontaktinformasjonV1(OrganisasjonEnhetKontaktinformasjonV1Alias organisasjonEnhetKontaktinformasjonV1Alias, NavAppCertAlias navAppCertAlias) {
		navAppCertAlias.postConstruct();

		setWsdlUrl(WSDL_URL);
		setEndpointName(ORG_KONTAKTINFO_V2_PORT_QNAME);
		setServiceName(ORG_KONTAKTINFO_V2_SERVICE_QNAME);
		setAdress(organisasjonEnhetKontaktinformasjonV1Alias.getEndpointurl());
		setReceiveTimeout(organisasjonEnhetKontaktinformasjonV1Alias.getReadtimeoutms());
		setConnectTimeout(organisasjonEnhetKontaktinformasjonV1Alias.getConnecttimeoutms());
		addOutInterceptor(new SystemSAMLOutInterceptor());
		addFeature(new WSAddressingFeature());
		return createPort(OrganisasjonEnhetKontaktinformasjonV1.class);
	}
}
