package no.nav.regoppslag.xmlenricher;

import static no.nav.regoppslag.treg001.support.PluginUtil.createNewSecurityContext;
import static no.nav.regoppslag.treg001.support.PluginUtil.securityContextIsUsedForAuthentication;
import static no.nav.regoppslag.xmlenricher.util.ValueMapKeys.DOKUMENTTYPEID;
import static no.nav.regoppslag.xmlenricher.util.ValueMapKeys.PREFIXMAPPER;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import io.reactivex.Flowable;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.xmlenricher.exceptions.MissingPluginException;
import no.nav.regoppslag.xmlenricher.util.Aggregate;
import no.nav.regoppslag.xmlenricher.util.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	
	public Document process(Document document, String dokumentTypeId) throws XPathExpressionException, MissingPluginException, RegOppslagTechnicalException, RegOppslagFunctionalException, RegOppslagSecurityException {
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		NamespacePrefixMapper prefixMapper = registry.getJaxbNamespaceHelper();
		
		List<Payload> processingList = new ArrayList<>();
		Set<XPathExpression> supportedElements = registry.getSupportedElements();
		for (XPathExpression xpath : supportedElements) {
			Node node = findSingleNode(xpath, document);
			if (node != null) {
				processingList.add(new Payload(node, registry.getOrCreateElementEnricherPlugin(xpath), node));
			}
		}
		
		
		final List<Throwable> unhandledErrors = Collections.synchronizedList(new ArrayList<>());
		Flowable.fromIterable(processingList)
				.parallel()
				.runOn(Schedulers.io())
				.map(payload -> {
							SecurityContextHolder.setContext(createNewSecurityContext(authentication, securityContextIsUsedForAuthentication(payload)));
							Map<String, Object> valueMap = new HashMap<>();
							valueMap.put(DOKUMENTTYPEID.name(), dokumentTypeId);
							valueMap.put(PREFIXMAPPER.name(), prefixMapper);
							return new Aggregate(payload.getPlugin()
									.processElement(payload.getElement(), valueMap), payload.getElement());
						}
				)
				.sequential()
				.blockingSubscribe(
						onNextElement -> aggregate(document, onNextElement),
						unhandledErrors::add
				);
		
		if (!unhandledErrors.isEmpty()) {
			if (unhandledErrors.get(0) instanceof CompositeException) {
				handleException(((CompositeException) unhandledErrors.get(0)).getExceptions().get(0));
			} else {
				handleException(unhandledErrors.get(0));
			}
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
	
	private void handleException(Throwable e) throws RegOppslagFunctionalException, RegOppslagTechnicalException, RegOppslagSecurityException {
		if (e instanceof RegOppslagFunctionalException) {
			throw new RegOppslagFunctionalException(e, ((RegOppslagFunctionalException) e).getShortDescription());
		} else if (e instanceof RegOppslagSecurityException) {
			throw new RegOppslagSecurityException(e, ((RegOppslagSecurityException) e).getShortDescription());
		} else if (e instanceof RegOppslagTechnicalException) {
			throw new RegOppslagTechnicalException(e, ((RegOppslagTechnicalException) e).getShortDescription());
		} else {
			throw new RegOppslagTechnicalException(e, e.getClass().getSimpleName());
		}
	}
	
}
