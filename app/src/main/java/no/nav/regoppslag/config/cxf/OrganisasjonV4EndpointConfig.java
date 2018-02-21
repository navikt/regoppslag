package no.nav.regoppslag.config.cxf;

import no.nav.modig.security.ws.SystemSAMLOutInterceptor;
import no.nav.regoppslag.fasit.NavAppCertAlias;
import no.nav.regoppslag.fasit.OrganisasjonV4Alias;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.OrganisasjonV4;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.namespace.QName;

/**
 * @author Paul Magne Lunde, Visma Consulting
 */
@Configuration
public class OrganisasjonV4EndpointConfig extends AbstractCxfEndpointConfig {

	private static final String NAMESPACE = "http://nav.no/tjeneste/virksomhet/organisasjon/v4/Binding";

	private static final QName ORGANISASJON_V4_PORT_QNAME = new QName(NAMESPACE, "Organisasjon_v4Port");
	private static final QName ORGANISASJON_V4_SERVICE_QNAME = new QName(NAMESPACE, "Organisasjon_v4");

	public static final String WSDL_URL = "wsdl/no/nav/tjeneste/virksomhet/organisasjon/v4/Binding.wsdl";

	@Bean
	public OrganisasjonV4 organisasjonV4(OrganisasjonV4Alias organisasjonV4Alias, NavAppCertAlias navAppCertAlias) {
		navAppCertAlias.postConstruct();

		setWsdlUrl(WSDL_URL);
		setEndpointName(ORGANISASJON_V4_PORT_QNAME);
		setServiceName(ORGANISASJON_V4_SERVICE_QNAME);
		setAdress(organisasjonV4Alias.getEndpointurl());
		setReceiveTimeout(organisasjonV4Alias.getReadtimeoutms());
		setConnectTimeout(organisasjonV4Alias.getConnecttimeoutms());
		addOutInterceptor(new SystemSAMLOutInterceptor());
		addFeature(new WSAddressingFeature());
		return createPort(OrganisasjonV4.class);
	}
}
