package no.nav.regoppslag.config.cxf;

import no.nav.regoppslag.config.fasit.NavAppCertAlias;
import no.nav.regoppslag.config.fasit.PersonV3Alias;
import no.nav.regoppslag.config.security.CustomSamlTokenOutInterceptor;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.springframework.context.annotation.Bean;

import javax.xml.namespace.QName;

/**
 * Spring config for PersonV3 CXF Endpoint
 *
 * @author Olav Røstvold Thorsen, Visma Consulting
 */
public class PersonV3EndpointConfig extends AbstractCxfEndpointConfig {

	private static final String NAMESPACE = "http://nav.no/tjeneste/virksomhet/person/v3/Binding";

	private static final QName PERSON_V3_PORT_QNAME = new QName(NAMESPACE, "Person_v3Port");
	private static final QName PERSON_V3_SERVICE_QNAME = new QName(NAMESPACE, "Person_v3");

	public static final String WSDL_URL = "wsdl/no/nav/tjeneste/virksomhet/person/v3/Binding.wsdl";

	@Bean
	public PersonV3 personV3(PersonV3Alias personV3Alias, NavAppCertAlias navAppCertAlias) {
		
		navAppCertAlias.postConstruct();

		setWsdlUrl(WSDL_URL);
		setEndpointName(PERSON_V3_PORT_QNAME);
		setServiceName(PERSON_V3_SERVICE_QNAME);
		setAdress(personV3Alias.getEndpointurl());
		setReceiveTimeout(personV3Alias.getReadtimeoutms());
		setConnectTimeout(personV3Alias.getConnecttimeoutms());
		addFeature(new WSAddressingFeature());
		addOutInterceptor(new CustomSamlTokenOutInterceptor());
		
		return createPort(PersonV3.class);
	}

}