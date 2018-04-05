package no.nav.regoppslag.xmlenricher;

import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.xmlenricher.exceptions.DuplicatedElementSupportException;
import no.nav.regoppslag.xmlenricher.exceptions.MissingPluginException;
import no.nav.regoppslag.xmlenricher.util.NamespacePrefixMapperHelper;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import javax.xml.xpath.XPathExpression;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Hans Petter Simonsen - Miles
 */
public class SimplePluginRegistry implements ElementEnricherPluginRegistry {

	private final Map<XPathExpression, Class<? extends ElementEnricherPlugin>> pluginMap = new HashMap<>();

	private final ApplicationContext applicationContext;

	private final NamespacePrefixMapperHelper jaxbNamspaceHelper;

	public SimplePluginRegistry(ApplicationContext applicationContext, NamespacePrefixMapperHelper jaxbNamspaceHelper) {
		this.applicationContext = applicationContext;
		this.jaxbNamspaceHelper = jaxbNamspaceHelper;
	}

	public NamespacePrefixMapperHelper getJaxbNamespaceHelper() {
		return jaxbNamspaceHelper;
	}

	@Override
	public void registerPlugin(XPathExpression supportedElement, Class<? extends ElementEnricherPlugin> plugin) throws DuplicatedElementSupportException {
		pluginMap.put(supportedElement, plugin);
	}

	@Override
	public ElementEnricherPlugin getOrCreateElementEnricherPlugin(XPathExpression supportedElement) throws MissingPluginException, RegOppslagTechnicalException {
		if (pluginMap.containsKey(supportedElement)) {
			try {
				return applicationContext.getBean(pluginMap.get(supportedElement));
			} catch (BeansException e) {
				throw new RegOppslagTechnicalException("Error getting bean for " + supportedElement, e);
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
