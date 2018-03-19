package no.nav.regoppslag.config;

import no.nav.regoppslag.treg001.Orchestrator;
import no.nav.regoppslag.treg001.plugins.MottakerPlugin;
import no.nav.regoppslag.treg001.plugins.NavOrgenhetNavnPlugin;
import no.nav.regoppslag.treg001.plugins.NavOrgenhetPlugin;
import no.nav.regoppslag.treg001.plugins.SaksbehandlerPlugin;
import no.nav.regoppslag.xmlenricher.ElementEnricherPluginRegistry;
import no.nav.regoppslag.xmlenricher.SimplePluginRegistry;
import no.nav.regoppslag.xmlenricher.exceptions.DuplicatedElementSupportException;
import no.nav.regoppslag.xmlenricher.util.RegisteroppslagNamespaceContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
public class OrchestratorConfig {
	@Bean
	public ElementEnricherPluginRegistry registry(ApplicationContext applicationContext) throws DuplicatedElementSupportException {
		ElementEnricherPluginRegistry registry = new SimplePluginRegistry(applicationContext);
		registry.registerPlugin("//felles:mottaker", MottakerPlugin.class);
		registry.registerPlugin("//felles:signerendeBeslutter/saksbehandler:navAnsatt", SaksbehandlerPlugin.class);
		registry.registerPlugin("//felles:signerendeBeslutter/saksbehandler:navEnhet", NavOrgenhetNavnPlugin.class);
		registry.registerPlugin("//felles:signerendeSaksbehandler/saksbehandler:navAnsatt", SaksbehandlerPlugin.class);
		registry.registerPlugin("//felles:signerendeSaksbehandler/saksbehandler:navEnhet", NavOrgenhetNavnPlugin.class);
		registry.registerPlugin("//kontaktinformasjon:postadresse", NavOrgenhetPlugin.class);
		//		registry.registerPlugin(new QName("http://nav.no/dok/pesysbrev/felles/v1/PesysFelles", "signerendeBeslutter"), NavOrgenhetNavnPlugin.class);
//		registry.registerPlugin(new QName("http://nav.no/dok/pesysbrev/felles/v1/PesysFelles", "kontaktinformasjon"), NavOrgenhetPlugin.class);
		return registry;
	}
	
	@Bean
	public Orchestrator orchestrator(ElementEnricherPluginRegistry registry) {
		Orchestrator orchestrator = new Orchestrator();
		NamespaceContext namespaceContext = new RegisteroppslagNamespaceContext();
		orchestrator.setRegistry(registry);
		orchestrator.setNamespaceContext(namespaceContext);
		return orchestrator;
	}
	
}