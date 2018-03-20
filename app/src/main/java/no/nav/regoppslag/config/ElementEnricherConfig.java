package no.nav.regoppslag.config;

import no.nav.regoppslag.treg001.plugins.NavOrgenhetBesoksadressePlugin;
import no.nav.regoppslag.xmlenricher.ElementEnricher;
import no.nav.regoppslag.treg001.plugins.MottakerPlugin;
import no.nav.regoppslag.treg001.plugins.NavOrgenhetNavnPlugin;
import no.nav.regoppslag.treg001.plugins.NavOrgenhetPostadressePlugin;
import no.nav.regoppslag.treg001.plugins.SaksbehandlerPlugin;
import no.nav.regoppslag.xmlenricher.ElementEnricherPluginRegistry;
import no.nav.regoppslag.xmlenricher.SimplePluginRegistry;
import no.nav.regoppslag.xmlenricher.exceptions.DuplicatedElementSupportException;
import no.nav.regoppslag.xmlenricher.util.RegisteroppslagNamespaceContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import javax.xml.namespace.NamespaceContext;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
public class ElementEnricherConfig {
	@Bean
	public ElementEnricherPluginRegistry registry(ApplicationContext applicationContext) throws DuplicatedElementSupportException {
		ElementEnricherPluginRegistry registry = new SimplePluginRegistry(applicationContext);
		registry.registerPlugin("//felles:mottaker", MottakerPlugin.class);
		registry.registerPlugin("//felles:behandlendeEnhet", NavOrgenhetNavnPlugin.class);
		registry.registerPlugin("//felles:signerendeBeslutter/saksbehandler:navAnsatt", SaksbehandlerPlugin.class);
		registry.registerPlugin("//felles:signerendeBeslutter/saksbehandler:navEnhet", NavOrgenhetNavnPlugin.class);
		registry.registerPlugin("//felles:signerendeSaksbehandler/saksbehandler:navAnsatt", SaksbehandlerPlugin.class);
		registry.registerPlugin("//felles:signerendeSaksbehandler/saksbehandler:navEnhet", NavOrgenhetNavnPlugin.class);
		registry.registerPlugin("//kontaktinformasjon:postadresse", NavOrgenhetPostadressePlugin.class);
		registry.registerPlugin("//kontaktinformasjon:returadresse", NavOrgenhetPostadressePlugin.class);
		registry.registerPlugin("//kontaktinformasjon:besoksadresse", NavOrgenhetBesoksadressePlugin.class);
		return registry;
	}
	
	@Bean
	public ElementEnricher orchestrator(ElementEnricherPluginRegistry registry) {
		ElementEnricher elementEnricher = new ElementEnricher();
		NamespaceContext namespaceContext = new RegisteroppslagNamespaceContext();
		elementEnricher.setRegistry(registry);
		elementEnricher.setNamespaceContext(namespaceContext);
		return elementEnricher;
	}
	
}