package no.nav.regoppslag.treg001;

import no.nav.regoppslag.treg001.xmlenricher.ElementEnricher;
import no.nav.regoppslag.treg001.xmlenricher.ElementEnricherPluginRegistry;
import no.nav.regoppslag.treg001.xmlenricher.PluginBeanRegistry;
import no.nav.regoppslag.treg001.xmlenricher.exceptions.DuplicatedElementSupportException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

public class ElementEnricherConfig {

	@Bean
	public ElementEnricherPluginRegistry registry(ApplicationContext applicationContext) throws DuplicatedElementSupportException {
		ElementEnricherPluginRegistry registry = new PluginBeanRegistry(applicationContext);
		registry.registerPlugin("/*[local-name()='brevdata']/*[local-name()='NAVFelles']//*[local-name()='mottaker']", MottakerPlugin.class);
		registry.registerPlugin("/*[local-name()='brevdata']/*[local-name()='NAVFelles']//*[local-name()='sakspart']", SakspartPlugin.class);
		registry.registerPlugin("/*[local-name()='brevdata']/*[local-name()='NAVFelles']//*[local-name()='behandlendeEnhet']", NavOrgenhetNavnPlugin.class);
		registry.registerPlugin("/*[local-name()='brevdata']/*[local-name()='NAVFelles']//*[local-name()='signerendeBeslutter']/*[local-name()='navAnsatt']", SaksbehandlerPlugin.class);
		registry.registerPlugin("/*[local-name()='brevdata']/*[local-name()='NAVFelles']//*[local-name()='signerendeBeslutter']/*[local-name()='navEnhet']", NavOrgenhetNavnPlugin.class);
		registry.registerPlugin("/*[local-name()='brevdata']/*[local-name()='NAVFelles']//*[local-name()='signerendeSaksbehandler']/*[local-name()='navAnsatt']", SaksbehandlerPlugin.class);
		registry.registerPlugin("/*[local-name()='brevdata']/*[local-name()='NAVFelles']//*[local-name()='signerendeSaksbehandler']/*[local-name()='navEnhet']", NavOrgenhetNavnPlugin.class);
		registry.registerPlugin("/*[local-name()='brevdata']/*[local-name()='NAVFelles']//*[local-name()='kontaktinformasjon']/*[local-name()='postadresse']", NavOrgenhetPostadressePlugin.class);
		registry.registerPlugin("/*[local-name()='brevdata']/*[local-name()='NAVFelles']//*[local-name()='kontaktinformasjon']/*[local-name()='returadresse']", NavOrgenhetPostadressePlugin.class);
		registry.registerPlugin("/*[local-name()='brevdata']/*[local-name()='NAVFelles']//*[local-name()='kontaktinformasjon']/*[local-name()='besoksadresse']", NavOrgenhetBesoksadressePlugin.class);

		return registry;
	}

	@Bean
	public ElementEnricher orchestrator(ElementEnricherPluginRegistry registry) {
		return new ElementEnricher(registry);
	}

}