package no.nav.regoppslag.xmlenricher;

import no.nav.regoppslag.xmlenricher.exceptions.DuplicatedElementSupportException;
import no.nav.regoppslag.xmlenricher.exceptions.MissingPluginException;

import javax.xml.xpath.XPathExpression;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Hans Petter Simonsen - Miles
 */
public class SimplePluginRegistry implements ElementEnricherPluginRegistry {
	private Map<XPathExpression, Class<? extends ElementEnricherPlugin>> pluginMap = new HashMap<>();

	@Override
	public void registerPlugin(XPathExpression supportedElement, Class<? extends ElementEnricherPlugin> plugin) throws DuplicatedElementSupportException {
		pluginMap.put(supportedElement, plugin);

	}

	@Override
	public ElementEnricherPlugin getOrCreateElementEnricherPlugin(XPathExpression supportedElement) throws MissingPluginException {
		if (pluginMap.containsKey(supportedElement)) {
			try {
				return pluginMap.get(supportedElement).newInstance();
			} catch (InstantiationException|IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		} else {
			throw new MissingPluginException("Missing plugin for xpath " + supportedElement);
		}
	}

	@Override
	public Set<XPathExpression> getSupportedElements() {
		return pluginMap.keySet();
	}
}
