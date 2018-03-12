package no.nav.regoppslag.treg001;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.xmlenricher.ElementEnricherPlugin;
import no.nav.regoppslag.xmlenricher.ElementEnricherPluginRegistry;
import no.nav.regoppslag.xmlenricher.exceptions.MissingPluginException;
import no.nav.regoppslag.xmlenricher.exceptions.MultiExceptionHolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Hans Petter Simonsen - Miles
 */
@Slf4j
public class Orchestrator {

	private ElementEnricherPluginRegistry registry;

	public void setRegistry(ElementEnricherPluginRegistry registry) {
		this.registry = registry;
	}

	
	private Node findSingleNode(QName qname, Document xmlDocument)  {
		return xmlDocument.getElementsByTagNameNS(qname.getNamespaceURI(), qname.getLocalPart()).item(0);
	}


	public static class Tuple<A,B> {
		private A element;
		private B plugin;
		public Tuple(A element, B plugin) {
			this.element = element;
			this.plugin = plugin;
		}
		A getElement() {
			return element;
		}
		B getPlugin() {
			return plugin;
		}
	}

	public Document process(Document document) throws XPathExpressionException, MissingPluginException, MultiExceptionHolder {
		List<Tuple<Node, ElementEnricherPlugin>> processingList = new ArrayList<>();
		Set<QName> supportedElements = registry.getSupportedElements();
		for (QName xpath : supportedElements) {
			Node node = findSingleNode(xpath, document);
			if (node != null) {
				processingList.add(new Tuple<>(node, registry.getOrCreateElementEnricherPlugin(xpath)));
			}
		}

		final List<Throwable> unhandledErrors = new ArrayList<>();

		Flowable.fromIterable(processingList)
				.parallel()
				.runOn(Schedulers.computation())
				.map(tuple -> tuple.plugin.processElement(tuple.element))
				.sequential()
				.blockingSubscribe(
						onNextElement -> aggregate(document, onNextElement),
						(Throwable onError) -> unhandledErrors.add(onError),
						() -> log.debug("Processing completed successfully - context hopefully displayed in MDC")
				)
				;

		if (!unhandledErrors.isEmpty()) {
			MultiExceptionHolder errors = new MultiExceptionHolder("Errors in asynch prosessing");
			errors.getUnhandledErrors().addAll(unhandledErrors);
			throw errors;
		}
		return document;
	}

	private void aggregate(Document document, Node newElement) {
		Element element = (Element) newElement;
		// Find element in original XML, only one of each supported
		Node orgElem = document.getElementsByTagName(newElement.getNodeName()).item(0);
		// If plugin does in-place mutation, no aggregation is necessary.
		if (newElement.isSameNode(orgElem)) {
			return;
		}
		Node importNode = document.adoptNode(element);
		// Replace original element with new element.
		orgElem.getParentNode().insertBefore(importNode, orgElem);
		orgElem.getParentNode().removeChild(orgElem);
	}


}
