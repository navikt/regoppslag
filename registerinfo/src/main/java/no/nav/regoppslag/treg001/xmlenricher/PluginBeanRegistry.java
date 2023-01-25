package no.nav.regoppslag.treg001.xmlenricher;

import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.treg001.xmlenricher.exceptions.DuplicatedElementSupportException;
import no.nav.regoppslag.treg001.xmlenricher.exceptions.MissingPluginException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PluginBeanRegistry implements ElementEnricherPluginRegistry {

	private final Map<String, Class<? extends ElementEnricherPlugin>> pluginMap = new HashMap<>();

	private final ApplicationContext applicationContext;

	public PluginBeanRegistry(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public void registerPlugin(String xpathExpression, Class<? extends ElementEnricherPlugin> plugin) throws DuplicatedElementSupportException {
		pluginMap.put(xpathExpression, plugin);
	}

	@Override
	public ElementEnricherPlugin getOrCreateElementEnricherPlugin(String xpathExpression) throws MissingPluginException, RegOppslagTechnicalException {
		if (pluginMap.containsKey(xpathExpression)) {
			try {
				return applicationContext.getBean(pluginMap.get(xpathExpression));
			} catch (BeansException e) {
				throw new RegOppslagTechnicalException("Error getting bean for " + xpathExpression, e);
			}
		} else {
			throw new MissingPluginException("Missing plugin for xpath " + xpathExpression);
		}
	}

	@Override
	public Set<String> getSupportedElements() {
		return pluginMap.keySet();
	}
}
