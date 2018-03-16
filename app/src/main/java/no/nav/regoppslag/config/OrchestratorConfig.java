package no.nav.regoppslag.config;

import no.nav.regoppslag.treg001.Orchestrator;
import no.nav.regoppslag.treg001.plugins.MottakerPlugin;
import no.nav.regoppslag.treg001.plugins.NavOrgenhetNavnPlugin;
import no.nav.regoppslag.treg001.plugins.NavOrgenhetPlugin;
import no.nav.regoppslag.treg001.plugins.SaksbehandlerPlugin;
import no.nav.regoppslag.xmlenricher.ElementEnricherPluginRegistry;
import no.nav.regoppslag.xmlenricher.SimplePluginRegistry;
import no.nav.regoppslag.xmlenricher.exceptions.DuplicatedElementSupportException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import javax.xml.namespace.QName;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
public class OrchestratorConfig {
	@Bean
	public ElementEnricherPluginRegistry registry(ApplicationContext applicationContext) throws DuplicatedElementSupportException {
		ElementEnricherPluginRegistry registry = new SimplePluginRegistry(applicationContext);
		registry.registerPlugin(new QName("http://nav.no/dok/pesysbrev/felles/v1/PesysFelles", "mottaker"), MottakerPlugin.class);
		registry.registerPlugin(new QName("http://nav.no/dok/pesysbrev/felles/v1/PesysFelles", "behandlendeEnhet"), NavOrgenhetNavnPlugin.class);
		registry.registerPlugin(new QName("http://nav.no/dok/pesysbrev/felles/v1/SaksBehandler", "navAnsatt"), SaksbehandlerPlugin.class);
		registry.registerPlugin(new QName("http://nav.no/dok/pesysbrev/felles/v1/PesysFelles", "navEnhet"), SaksbehandlerPlugin.class);
		registry.registerPlugin(new QName("http://nav.no/dok/pesysbrev/felles/v1/PesysFelles", "signerendeBeslutter"), SaksbehandlerPlugin.class);
		registry.registerPlugin(new QName("http://nav.no/dok/pesysbrev/felles/v1/PesysFelles", "kontaktinformasjon"), NavOrgenhetPlugin.class);
		return registry;
	}
	
	@Bean
	public Orchestrator orchestrator(ElementEnricherPluginRegistry registry) {
		Orchestrator orchestrator = new Orchestrator();
		orchestrator.setRegistry(registry);
		return orchestrator;
	}
	
}