package no.nav.regoppslag.xmlenricher;

import no.nav.regoppslag.xmlenricher.exceptions.DuplicatedElementSupportException;
import no.nav.regoppslag.xmlenricher.exceptions.MissingPluginException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathExpression;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Hans Petter Simonsen - Miles
 */
public class SimplePluginRegistry implements ElementEnricherPluginRegistry {

	private Map<String, Class<? extends ElementEnricherPlugin>> pluginMap = new HashMap<>();

	private ApplicationContext applicationContext;
	
	public SimplePluginRegistry(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
	
	@Override
		public void registerPlugin(String supportedElement, Class<? extends ElementEnricherPlugin> plugin) throws DuplicatedElementSupportException {
		pluginMap.put(supportedElement, plugin);

	}

	@Override
	public ElementEnricherPlugin getOrCreateElementEnricherPlugin(String supportedElement) throws MissingPluginException {
		if (pluginMap.containsKey(supportedElement)) {
			try {
				return applicationContext.getBean(pluginMap.get(supportedElement));
			} catch (BeansException e) {
				throw new RuntimeException(e);
			}
		} else {
			throw new MissingPluginException("Missing plugin for xpath " + supportedElement);
		}
	}

	@Override
	public Set<String> getSupportedElements() {
		return pluginMap.keySet();
	}
}
