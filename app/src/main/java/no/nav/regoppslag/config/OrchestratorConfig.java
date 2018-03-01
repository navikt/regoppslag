package no.nav.regoppslag.config;

import no.nav.regoppslag.treg001.Orchestrator;
import no.nav.regoppslag.treg001.plugins.MottakerPlugin;
import no.nav.regoppslag.xmlenricher.ElementEnricherPluginRegistry;
import no.nav.regoppslag.xmlenricher.SimplePluginRegistry;
import no.nav.regoppslag.xmlenricher.exceptions.DuplicatedElementSupportException;
import org.springframework.context.annotation.Bean;

import javax.xml.namespace.QName;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
public class OrchestratorConfig {
	@Bean
	public Orchestrator orchestrator(ElementEnricherPluginRegistry registry) {
		Orchestrator orchestrator = new Orchestrator();
		orchestrator.setRegistry(registry);
		return orchestrator;
	}
	
	@Bean
	ElementEnricherPluginRegistry registry() throws DuplicatedElementSupportException {
		ElementEnricherPluginRegistry registry = new SimplePluginRegistry();
		registry.registerPlugin(new QName("mottaker"), MottakerPlugin.class);
		return registry;
	}
}
