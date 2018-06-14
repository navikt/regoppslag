package no.nav.regoppslag.config;

import no.nav.regoppslag.treg001.MottakerPlugin;
import no.nav.regoppslag.treg001.NavOrgenhetBesoksadressePlugin;
import no.nav.regoppslag.treg001.NavOrgenhetNavnPlugin;
import no.nav.regoppslag.treg001.NavOrgenhetPostadressePlugin;
import no.nav.regoppslag.treg001.SaksbehandlerPlugin;
import no.nav.regoppslag.treg001.SakspartPlugin;
import no.nav.regoppslag.xmlenricher.ElementEnricher;
import no.nav.regoppslag.xmlenricher.ElementEnricherPluginRegistry;
import no.nav.regoppslag.xmlenricher.PluginBeanRegistry;
import no.nav.regoppslag.xmlenricher.exceptions.DuplicatedElementSupportException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
public class ElementEnricherConfig {
	@Bean
	public ElementEnricherPluginRegistry registry(ApplicationContext applicationContext) throws DuplicatedElementSupportException, XPathExpressionException {
		ElementEnricherPluginRegistry registry = new PluginBeanRegistry(applicationContext);
		registry.registerPlugin(createExpression("/*[local-name()='brevdata']/*[local-name()='NAVFelles']//*[local-name()='mottaker']"), MottakerPlugin.class);
		registry.registerPlugin(createExpression("/*[local-name()='brevdata']/*[local-name()='NAVFelles']//*[local-name()='sakspart']"), SakspartPlugin.class);
		registry.registerPlugin(createExpression("/*[local-name()='brevdata']/*[local-name()='NAVFelles']//*[local-name()='behandlendeEnhet']/*[local-name()='navEnhet']"), NavOrgenhetNavnPlugin.class);
		registry.registerPlugin(createExpression("/*[local-name()='brevdata']/*[local-name()='NAVFelles']//*[local-name()='signerendeBeslutter']/*[local-name()='navAnsatt']"), SaksbehandlerPlugin.class);
		registry.registerPlugin(createExpression("/*[local-name()='brevdata']/*[local-name()='NAVFelles']//*[local-name()='signerendeBeslutter']/*[local-name()='navEnhet']"), NavOrgenhetNavnPlugin.class);
		registry.registerPlugin(createExpression("/*[local-name()='brevdata']/*[local-name()='NAVFelles']//*[local-name()='signerendeSaksbehandler']/*[local-name()='navAnsatt']"), SaksbehandlerPlugin.class);
		registry.registerPlugin(createExpression("/*[local-name()='brevdata']/*[local-name()='NAVFelles']//*[local-name()='signerendeSaksbehandler']/*[local-name()='navEnhet']"), NavOrgenhetNavnPlugin.class);
		registry.registerPlugin(createExpression("/*[local-name()='brevdata']/*[local-name()='NAVFelles']//*[local-name()='kontaktinformasjon']/*[local-name()='postadresse']"), NavOrgenhetPostadressePlugin.class);
		registry.registerPlugin(createExpression("/*[local-name()='brevdata']/*[local-name()='NAVFelles']//*[local-name()='kontaktinformasjon']/*[local-name()='returadresse']"), NavOrgenhetPostadressePlugin.class);
		registry.registerPlugin(createExpression("/*[local-name()='brevdata']/*[local-name()='NAVFelles']//*[local-name()='kontaktinformasjon']/*[local-name()='besoksadresse']"), NavOrgenhetBesoksadressePlugin.class);

		return registry;
	}

	private XPathExpression createExpression(String expression) throws XPathExpressionException {
		XPath xPath = XPathFactory.newInstance().newXPath();
		return xPath.compile(expression);
	}

	@Bean
	public ElementEnricher orchestrator(ElementEnricherPluginRegistry registry) {
		ElementEnricher elementEnricher = new ElementEnricher();
		elementEnricher.setRegistry(registry);
		return elementEnricher;
	}
	
}