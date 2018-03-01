package no.nav.regoppslag.xmlenricher;

import no.nav.regoppslag.xmlenricher.exceptions.DuplicatedElementSupportException;
import no.nav.regoppslag.xmlenricher.exceptions.MissingPluginException;
import org.w3c.dom.TypeInfo;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathExpression;
import java.util.Set;

/**
 * @author Hans Petter Simonsen - Miles
 * Registry to hold supported {@link ElementEnricherPlugin} classes.
 * Registry also functions as a Factory class to instantiate {@link ElementEnricherPlugin} objects with the {@link #getOrCreateElementEnricherPlugin(XPathExpression)} method.
 *
 * ElementEnricherPluginRegistry implementation may scan classpath for all {@link ElementEnricherPlugin} classes to provide automatic registration of pluginsPOC,
 * or the registry may be populated in a spring bean declaration.
 */
public interface ElementEnricherPluginRegistry {
	/**
	 * Registers a plugin class for a particular element type
	 * @param supportedElement Element type supported by the plugin
	 * @param plugin A class implementing {@link ElementEnricherPlugin}
	 * @throws DuplicatedElementSupportException when there is more than one plugin class supporting the same {@link TypeInfo} element type
	 */
//	void registerPlugin(XPathExpression supportedElement, Class<? extends ElementEnricherPlugin> plugin) throws DuplicatedElementSupportException;
	void registerPlugin(QName supportedElement, Class<? extends ElementEnricherPlugin> plugin) throws DuplicatedElementSupportException;

	/**
	 * Creates a new instance of a plugin class for the specified {@link TypeInfo} element type
	 * @param supportedElement asks for an instance of the plugin supporting this type
	 * @return an instance of a plugin supporting supportedElement
	 * @throws MissingPluginException when there exists no registered plugin for supportedElement
	 */
//	ElementEnricherPlugin getOrCreateElementEnricherPlugin(XPathExpression supportedElement) throws MissingPluginException;
	ElementEnricherPlugin getOrCreateElementEnricherPlugin(QName supportedElement) throws MissingPluginException;

	/**
	 * Lists all known supportedTypes that are backed by actual implementing classes of {@link ElementEnricherPlugin}
	 * @return all element types supported by registered {@link ElementEnricherPlugin} classes.
	 */
//	Set<XPathExpression> getSupportedElements();
	Set<QName> getSupportedElements();
}