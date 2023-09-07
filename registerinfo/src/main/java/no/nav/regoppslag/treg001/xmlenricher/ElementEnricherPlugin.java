package no.nav.regoppslag.treg001.xmlenricher;

import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.treg001.xmlenricher.exceptions.InvalidElementException;
import no.nav.regoppslag.treg001.xmlenricher.exceptions.MissingKeyValueException;
import no.nav.regoppslag.treg001.xmlenricher.exceptions.RegistryServiceFunctionalException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Map;

/**
 * Enriches a specific {@link Element} with additional content.
 * Intended use is to use a specific key element within the Element content input to lookup additional registry content.
 * The {@link #processElement(Node, Map)} method will populate additional content within the content element node and return
 * either a copy of the element or a modified version og the same element to the caller.
 * All {@link ElementEnricherPlugin} classes must be registered with the {@link ElementEnricherPluginRegistry} to be picked up by the XmlEnricherEngine (TBEST001)
 * - alternative solution is that ElementEnricherPluginRegistry scans classpath for all {@link ElementEnricherPlugin} classes.
 */
public interface ElementEnricherPlugin {


	/**
	 * This method will enrich an xml element with additional registry data.
	 *
	 * @param content        is a complex element node of some specific type.
	 *                       Element must contain a key subelement to be used for lookup of registry data.
	 * @return Element of the same type as the input, but enriched with registry data.
	 * @NOTE If the element is already pre-populated, this method should not attempt to overwrite existing contents.
	 */
	Node processElement(Node content, Map<String, Object> propertyMap, String tema) throws RegOppslagSecurityException;

}
