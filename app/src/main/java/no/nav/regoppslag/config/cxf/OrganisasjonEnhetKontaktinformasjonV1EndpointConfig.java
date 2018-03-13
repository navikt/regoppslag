package no.nav.regoppslag.config.cxf;

import no.nav.regoppslag.config.fasit.NavAppCertAlias;
import no.nav.regoppslag.config.fasit.OrganisasjonEnhetKontaktinformasjonV1Alias;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.binding.OrganisasjonEnhetKontaktinformasjonV1;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.springframework.context.annotation.Bean;

import javax.xml.namespace.QName;

/**
 * Spring config for OrganisasjonEnhetKontaktinformasjonV1 CXF Endpoint
 *
 * @author Ketill Fenne, Visma Consulting
 */
public class OrganisasjonEnhetKontaktinformasjonV1EndpointConfig extends AbstractCxfEndpointConfig {
	private static final String NAMESPACE = "http://nav.no/tjeneste/virksomhet/organisasjonEnhetKontaktinformasjon/v1/Binding";

	private static final QName ORG_KONTAKTINFO_V1_PORT_QNAME = new QName(NAMESPACE, "OrganisasjonEnhetKontaktinformasjon_v1Port");
	private static final QName ORG_KONTAKTINFO_V1_SERVICE_QNAME = new QName(NAMESPACE, "OrganisasjonEnhetKontaktinformasjon_v1");

	public static final String WSDL_URL = "wsdl/no/nav/tjeneste/virksomhet/organisasjonEnhetKontaktinformasjon/v1/Binding.wsdl";

	@Bean
	public OrganisasjonEnhetKontaktinformasjonV1 organisasjonEnhetKontaktinformasjonV1(OrganisasjonEnhetKontaktinformasjonV1Alias organisasjonEnhetKontaktinformasjonV1Alias, NavAppCertAlias navAppCertAlias) {
		navAppCertAlias.postConstruct();

		setWsdlUrl(WSDL_URL);
		setEndpointName(ORG_KONTAKTINFO_V1_PORT_QNAME);
		setServiceName(ORG_KONTAKTINFO_V1_SERVICE_QNAME);
		setAdress(organisasjonEnhetKontaktinformasjonV1Alias.getEndpointurl());
		setReceiveTimeout(organisasjonEnhetKontaktinformasjonV1Alias.getReadtimeoutms());
		setConnectTimeout(organisasjonEnhetKontaktinformasjonV1Alias.getConnecttimeoutms());
		addFeature(new WSAddressingFeature());
		
		OrganisasjonEnhetKontaktinformasjonV1 organisasjonEnhetKontaktinformasjonV1 = createPort(OrganisasjonEnhetKontaktinformasjonV1.class);
		configureSTSSamlToken(organisasjonEnhetKontaktinformasjonV1);
		return organisasjonEnhetKontaktinformasjonV1;
	}
}
