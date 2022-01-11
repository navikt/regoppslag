package no.nav.regoppslag.treg001.xmlenricher;

import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.treg001.xmlenricher.exceptions.DuplicatedElementSupportException;
import no.nav.regoppslag.treg001.xmlenricher.exceptions.MissingPluginException;
import org.w3c.dom.TypeInfo;

import java.util.Set;

/**
 * @author Hans Petter Simonsen - Miles
 * Registry to hold supported {@link ElementEnricherPlugin} classes.
 * Registry also functions as a Factory class to instantiate {@link ElementEnricherPlugin} objects with the {@link #getOrCreateElementEnricherPlugin(String)} method.
 *
 * ElementEnricherPluginRegistry implementation may scan classpath for all {@link ElementEnricherPlugin} classes to provide automatic registration of pluginsPOC,
 * or the registry may be populated in a spring bean declaration.
 */
public interface ElementEnricherPluginRegistry {
	/**
	 * Registers a plugin class for a particular element type
	 * @param xpathExpression Element type supported by the plugin
	 * @param plugin A class implementing {@link ElementEnricherPlugin}
	 * @throws DuplicatedElementSupportException when there is more than one plugin class supporting the same {@link TypeInfo} element type
	 */
	void registerPlugin(String xpathExpression, Class<? extends ElementEnricherPlugin> plugin) throws DuplicatedElementSupportException;

	/**
	 * Creates a new instance of a plugin class for the specified {@link TypeInfo} element type
	 * @param xpathExpression asks for an instance of the plugin supporting this type
	 * @return an instance of a plugin supporting supportedElement
	 * @throws MissingPluginException when there exists no registered plugin for supportedElement
	 */
	ElementEnricherPlugin getOrCreateElementEnricherPlugin(String xpathExpression) throws MissingPluginException, RegOppslagTechnicalException;

	/**
	 * Lists all known supportedTypes that are backed by actual implementing classes of {@link ElementEnricherPlugin}
	 * @return all element types supported by registered {@link ElementEnricherPlugin} classes.
	 */
	Set<String> getSupportedElements();

}