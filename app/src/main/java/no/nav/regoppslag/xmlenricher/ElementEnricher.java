package no.nav.regoppslag.xmlenricher;

import static no.nav.regoppslag.treg001.support.PluginUtil.createNewSecurityContext;
import static no.nav.regoppslag.treg001.support.PluginUtil.securityContextIsUsedForAuthentication;
import static no.nav.regoppslag.util.MDCConstants.CALL_ID;
import static no.nav.regoppslag.util.MDCConstants.CONSUMER_ID;
import static no.nav.regoppslag.util.MDCConstants.USER_ID;
import static no.nav.regoppslag.xmlenricher.util.ValueMapKeys.DOKUMENTTYPEID;
import static no.nav.regoppslag.xmlenricher.util.ValueMapKeys.MAALFORM;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.treg001.support.Maalform;
import no.nav.regoppslag.xmlenricher.exceptions.MissingPluginException;
import no.nav.regoppslag.xmlenricher.util.Aggregate;
import no.nav.regoppslag.xmlenricher.util.NamespaceResolver;
import no.nav.regoppslag.xmlenricher.util.Payload;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
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
	private NamespaceResolver namespaceResolver = new NamespaceResolver();

	public void setRegistry(ElementEnricherPluginRegistry registry) {
		this.registry = registry;
	}

	public Document process(Document document, String dokumentTypeId) throws XPathExpressionException, MissingPluginException, RegOppslagTechnicalException, RegOppslagFunctionalException, RegOppslagSecurityException {

		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		final String consumerId = MDC.get(CONSUMER_ID);
		final String userId = MDC.get(USER_ID);
		final String callId = MDC.get(CALL_ID);

		List<Payload> processingList = new ArrayList<>();
		Set<String> supportedElementsXpathExpressions = registry.getSupportedElements();
		for (String xpathExpression : supportedElementsXpathExpressions) {
			Node node = namespaceResolver.resolveNamespace(xpathExpression, document);
			if (node != null) {
				Node clonedNode=node.cloneNode(true);
				processingList.add(new Payload(clonedNode, registry.getOrCreateElementEnricherPlugin(xpathExpression), node));
			}
		}


		final List<Throwable> unhandledError = new ArrayList<>();
		List<Aggregate> aggregateList = new ArrayList<>();
		Flowable.fromIterable(processingList)
				.parallel()
				.runOn(Schedulers.io())
				.map(payload -> {
							if (securityContextIsUsedForAuthentication(payload)) {
								SecurityContextHolder.setContext(createNewSecurityContext(authentication, true));
							}

							MDC.put(CONSUMER_ID, consumerId);
							MDC.put(USER_ID, userId);
							MDC.put(CALL_ID, callId);

							Map<String, Object> valueMap = new HashMap<>();
							valueMap.put(DOKUMENTTYPEID.name(), dokumentTypeId);
							valueMap.put(MAALFORM.name(), new Maalform());

							return new Aggregate(payload.getPlugin()
									.processElement(payload.getElement(), valueMap), payload.getOrgNode());
						}
				)
				.sequential()
				.blockingSubscribe(
						onNextElement -> aggregateList.add(onNextElement),
						error -> unhandledError.add(error)
				);


		if (!unhandledError.isEmpty()) {
			handleException(unhandledError.get(0));
		}

		aggregateList.forEach(element -> aggregate(document, element));

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
			throw (RegOppslagFunctionalException) e;
		} else if (e instanceof RegOppslagSecurityException) {
			throw (RegOppslagSecurityException) e;
		} else if (e instanceof RegOppslagTechnicalException) {
			throw (RegOppslagTechnicalException) e;
		} else {
			throw new RegOppslagTechnicalException(e, e.getClass().getSimpleName());
		}
	}

}
