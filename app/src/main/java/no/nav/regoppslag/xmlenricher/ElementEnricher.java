package no.nav.regoppslag.xmlenricher;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import io.reactivex.Flowable;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.xmlenricher.exceptions.MissingPluginException;
import no.nav.regoppslag.xmlenricher.exceptions.MultiExceptionHolder;
import no.nav.regoppslag.xmlenricher.util.Aggregate;
import no.nav.regoppslag.xmlenricher.util.Payload;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.Collections;
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

	private Node findSingleNode(XPathExpression xpathExpression, Document xmlDocument) throws XPathExpressionException {
		return (Node) xpathExpression.evaluate(xmlDocument, XPathConstants.NODE);
	}

	public Document process(Document document, String dokumentTypeId) throws XPathExpressionException, MissingPluginException, MultiExceptionHolder, RegOppslagTechnicalException {

		NamespacePrefixMapper prefixMapper = registry.getJaxbNamespaceHelper();

		List<Payload> processingList = new ArrayList<>();
		Set<XPathExpression> supportedElements = registry.getSupportedElements();
		for (XPathExpression xpath : supportedElements) {
			Node node = findSingleNode(xpath, document);
			if (node != null) {
				processingList.add(new Payload(node, registry.getOrCreateElementEnricherPlugin(xpath),node));
			}
		}

		final List<Throwable> unhandledErrors = Collections.synchronizedList(new ArrayList<>());

		Flowable.fromIterable(processingList)
				.parallel()
				.runOn(Schedulers.computation())
				.map(payload -> new Aggregate(payload.getPlugin().processElement(payload.getElement(), dokumentTypeId, prefixMapper), payload.getElement()))
				.sequentialDelayError()
				.blockingSubscribe(
						onNextElement -> aggregate(document, onNextElement),
						throwable -> unhandledErrors.add(throwable)
				);

		if (!unhandledErrors.isEmpty()) {
			MultiExceptionHolder errors = new MultiExceptionHolder("Feil i asynk prosessering ");
			if (unhandledErrors.get(0) instanceof CompositeException) {
				errors.getUnhandledErrors().addAll(((CompositeException) unhandledErrors.get(0)).getExceptions());
			} else {
				errors.getUnhandledErrors().addAll(unhandledErrors);
			}
			throw errors;
		}
		return document;
	}

	private void aggregate(Document document, Aggregate aggregate) {
		// Find element in original XML, only one of each supported
		Node orgElem = aggregate.getOrigNode();
		// If plugin does in-place mutation, no aggregation is necessary.
		if (aggregate.getNewNode().isSameNode(orgElem)) {
			return;
		}
		Element element = (Element) aggregate.getNewNode();
		Node importNode = document.adoptNode(element);
		// Replace original element with new element.
		orgElem.getParentNode().insertBefore(importNode, orgElem);
		orgElem.getParentNode().removeChild(orgElem);
	}
}
