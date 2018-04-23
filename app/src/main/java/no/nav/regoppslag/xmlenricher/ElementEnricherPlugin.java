package no.nav.regoppslag.xmlenricher;

import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.xmlenricher.exceptions.InvalidElementException;
import no.nav.regoppslag.xmlenricher.exceptions.MissingKeyValueException;
import no.nav.regoppslag.xmlenricher.exceptions.RegistryServiceFunctionalException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Map;

/**
 * @author Hans Petter Simonsen - Miles
 * Enriches a specific {@link Element} with additional content.
 * Intended use is to use a specific key element within the Element content input to lookup additional registry content.
 * The {@link #processElement(Node, String)} method will populate additional content within the content element node and return
 * either a copy of the element or a modified version og the same element to the caller.
 * All {@link ElementEnricherPlugin} classes must be registered with the {@link ElementEnricherPluginRegistry} to be picked up by the XmlEnricherEngine (TBEST001)
 *  - alternative solution is that ElementEnricherPluginRegistry scans classpath for all {@link ElementEnricherPlugin} classes.
 *
 */
public interface ElementEnricherPlugin {



	/**
	 * This method will enrich an xml element with additional registry data.
	 * @NOTE If the element is already pre-populated, this method should not attempt to overwrite existing contents.
	 *
	 * @param content is a complex element node of some specific type.
	 *       Element must contain a key subelement to be used for lookup of registry data.
	 * @param dokumentTypeId is the DokumentType for the content to be produced.
	 * @return Element of the same type as the input, but enriched with registry data.
	 * @throws InvalidElementException if element is not of correct type
	 * @throws MissingKeyValueException if lookup key subelement is empty or missing
	 * @throws RegistryServiceFunctionalException if the registry data lookupservice fails with a functional exception. Exception cause will contain the root exception.
	 */
	Node processElement(Node content, Map<String, Object> propertyMap) throws RegOppslagFunctionalException, RegOppslagTechnicalException, RegOppslagSecurityException;

}
