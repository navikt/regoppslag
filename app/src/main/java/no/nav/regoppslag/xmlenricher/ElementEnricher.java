package no.nav.regoppslag.xmlenricher;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.xmlenricher.exceptions.MissingPluginException;
import no.nav.regoppslag.xmlenricher.exceptions.MultiExceptionHolder;
import no.nav.regoppslag.xmlenricher.util.Aggregate;
import no.nav.regoppslag.xmlenricher.util.Payload;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Hans Petter Simonsen - Miles
 */
@Slf4j
public class ElementEnricher {
	
	private ElementEnricherPluginRegistry registry;

	public void setRegistry(ElementEnricherPluginRegistry registry) {
		this.registry = registry;
	}

	private NamespaceContext namespaceContext;

	public void setNamespaceContext(NamespaceContext namespaceContext) {
		this.namespaceContext = namespaceContext;
	}


	private Node findSingleNode(String xpathExpression, Document xmlDocument) throws XPathExpressionException {
		XPath xPath = XPathFactory.newInstance().newXPath();
		xPath.setNamespaceContext(namespaceContext);
		XPathExpression expression = xPath.compile(xpathExpression);

		return (Node) expression.evaluate(xmlDocument, XPathConstants.NODE);
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

	public Document process(Document document, String dokumentTypeId) throws XPathExpressionException, MissingPluginException, MultiExceptionHolder {
		List<Payload> processingList = new ArrayList<>();
		Set<String> supportedElements = registry.getSupportedElements();
		for (String xpath : supportedElements) {
			Node node = findSingleNode(xpath, document);
			if (node != null) {
				processingList.add(new Payload(node, registry.getOrCreateElementEnricherPlugin(xpath),node));
			}
		}

		final List<Throwable> unhandledErrors = new ArrayList<>();

		Flowable.fromIterable(processingList)
				.parallel()
				.runOn(Schedulers.computation())
				.map(payload -> new Aggregate(payload.getPlugin().processElement(payload.getElement(), dokumentTypeId), payload.getElement()))
				.sequential()
				.blockingSubscribe(
						onNextElement -> aggregate(document, onNextElement),
						(Throwable onError) -> unhandledErrors.add(onError),
						() -> log.debug("Processing completed successfully - context hopefully displayed in MDC")
				);

		if (!unhandledErrors.isEmpty()) {
			MultiExceptionHolder errors = new MultiExceptionHolder("Errors in asynch prosessing");
			errors.getUnhandledErrors().addAll(unhandledErrors);
			throw errors;
		}
		return document;
	}

	private void aggregate(Document document, Aggregate aggregate) {
		Element element = (Element) aggregate.getNewNode();
		// Find element in original XML, only one of each supported
		Node orgElem = aggregate.getOrigNode();
		// If plugin does in-place mutation, no aggregation is necessary.
		if (aggregate.getNewNode().isSameNode(orgElem)) {
			return;
		}
		Node importNode = document.adoptNode(element);
		// Replace original element with new element.
		orgElem.getParentNode().insertBefore(importNode, orgElem);
		orgElem.getParentNode().removeChild(orgElem);
	}
}
