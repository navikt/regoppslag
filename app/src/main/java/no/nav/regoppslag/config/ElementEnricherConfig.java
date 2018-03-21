package no.nav.regoppslag.config;

import no.nav.regoppslag.treg001.plugins.MottakerPlugin;
import no.nav.regoppslag.treg001.plugins.NavOrgenhetBesoksadressePlugin;
import no.nav.regoppslag.treg001.plugins.NavOrgenhetNavnPlugin;
import no.nav.regoppslag.treg001.plugins.NavOrgenhetPostadressePlugin;
import no.nav.regoppslag.treg001.plugins.SaksbehandlerPlugin;
import no.nav.regoppslag.xmlenricher.ElementEnricher;
import no.nav.regoppslag.xmlenricher.ElementEnricherPluginRegistry;
import no.nav.regoppslag.xmlenricher.SimplePluginRegistry;
import no.nav.regoppslag.xmlenricher.exceptions.DuplicatedElementSupportException;
import no.nav.regoppslag.xmlenricher.util.NamespacePrefixMapperHelper;
import no.nav.regoppslag.xmlenricher.util.RegisteroppslagNamespaceContext;
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
		RegisteroppslagNamespaceContext context = new RegisteroppslagNamespaceContext();
		NamespacePrefixMapperHelper jaxbhelper = new NamespacePrefixMapperHelper(context);
		ElementEnricherPluginRegistry registry = new SimplePluginRegistry(applicationContext, jaxbhelper);
		registry.registerPlugin(createExpression("//felles:mottaker", context), MottakerPlugin.class);
		registry.registerPlugin(createExpression("//felles:behandlendeEnhet", context), NavOrgenhetNavnPlugin.class);
		registry.registerPlugin(createExpression("//felles:signerendeBeslutter/saksbehandler:navAnsatt", context), SaksbehandlerPlugin.class);
		registry.registerPlugin(createExpression("//felles:signerendeBeslutter/saksbehandler:navEnhet", context), NavOrgenhetNavnPlugin.class);
		registry.registerPlugin(createExpression("//felles:signerendeSaksbehandler/saksbehandler:navAnsatt", context), SaksbehandlerPlugin.class);
		registry.registerPlugin(createExpression("//felles:signerendeSaksbehandler/saksbehandler:navEnhet", context), NavOrgenhetNavnPlugin.class);
		registry.registerPlugin(createExpression("//kontaktinformasjon:postadresse", context), NavOrgenhetPostadressePlugin.class);
		registry.registerPlugin(createExpression("//kontaktinformasjon:returadresse", context), NavOrgenhetPostadressePlugin.class);
		registry.registerPlugin(createExpression("//kontaktinformasjon:besoksadresse", context), NavOrgenhetBesoksadressePlugin.class);
		return registry;
	}

	private XPathExpression createExpression(String expression, RegisteroppslagNamespaceContext context) throws XPathExpressionException {
		XPath xPath = XPathFactory.newInstance().newXPath();
		xPath.setNamespaceContext(context);
		return xPath.compile(expression);
	}

	@Bean
	public ElementEnricher orchestrator(ElementEnricherPluginRegistry registry) {
		ElementEnricher elementEnricher = new ElementEnricher();
		elementEnricher.setRegistry(registry);
		return elementEnricher;
	}
	
}