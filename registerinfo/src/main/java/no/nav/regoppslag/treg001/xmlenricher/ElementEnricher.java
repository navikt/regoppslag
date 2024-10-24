package no.nav.regoppslag.treg001.xmlenricher;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.exceptions.UndeliverableException;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.exceptions.FeilGrunnetHoeytVolumWorkaroundException;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagIkkeFunnetException;
import no.nav.regoppslag.exceptions.RegOppslagIngenTilgangException;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;
import no.nav.regoppslag.exceptions.UkjentAdresseException;
import no.nav.regoppslag.exceptions.UkjentAdressePersonErDoedException;
import no.nav.regoppslag.treg001.xmlenricher.exceptions.MissingPluginException;
import no.nav.regoppslag.treg001.xmlenricher.util.Aggregate;
import no.nav.regoppslag.treg001.xmlenricher.util.AttributeValueNamespaceResolver;
import no.nav.regoppslag.treg001.xmlenricher.util.Payload;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static no.nav.regoppslag.treg001.xmlenricher.util.ValueMapKeys.DOKUMENTTYPEID;
import static no.nav.regoppslag.util.MDCConstants.CALL_ID;
import static no.nav.regoppslag.util.MDCConstants.CONSUMER_ID;
import static no.nav.regoppslag.util.MDCConstants.USER_ID;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@Component
public class ElementEnricher {

	@Setter
	private ElementEnricherPluginRegistry registry;
	private final AttributeValueNamespaceResolver attributeValueNamespaceResolver;

	public ElementEnricher(ElementEnricherPluginRegistry registry) {
		this.registry = registry;
		this.attributeValueNamespaceResolver = new AttributeValueNamespaceResolver();
		// https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0#error-handling
		RxJavaPlugins.setErrorHandler(e -> {
			if (e instanceof UndeliverableException) {
				// Kan komme f.eks på grunn av adressebeskyttelse
				e = e.getCause();
				log.warn("Kunne ikke fullføre flow, sannsynligvis pga tilgang til ressurs", e);
				return;
			}
			if (e instanceof IOException) {
				// Nettverksproblem
				return;
			}
			if (e instanceof InterruptedException) {
				// Blokkende kode ble interrupted
				return;
			}
			log.warn("Klarte ikke fullføre flow, forstår ikke hva som er galt", e);
		});
	}

	private static Node findSingleNode(String xpathExpression, Document document) throws XPathExpressionException {

		XPath xPath = XPathFactory.newInstance().newXPath();

		XPathExpression xPathExpression = xPath.compile(xpathExpression);
		return (Node) xPathExpression.evaluate(document, XPathConstants.NODE);
	}

	public Document process(Document document, String dokumentTypeId) throws XPathExpressionException, MissingPluginException, RegOppslagTechnicalException, RegOppslagFunctionalException, RegOppslagSecurityException {

		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		final String consumerId = MDC.get(CONSUMER_ID);
		final String userId = MDC.get(USER_ID);
		final String callId = MDC.get(CALL_ID);

		List<Payload> processingList = new ArrayList<>();
		Set<String> supportedElementsXpathExpressions = registry.getSupportedElements();
		for (String xpathExpression : supportedElementsXpathExpressions) {
			Node node = findSingleNode(xpathExpression, document);
			attributeValueNamespaceResolver.resolveNamespace(document, node);
			if (node != null) {
				Node clonedNode = node.cloneNode(true);
				processingList.add(new Payload(clonedNode, registry.getOrCreateElementEnricherPlugin(xpathExpression), node));
			}
		}

		final List<Throwable> unhandledError = new ArrayList<>();
		List<Aggregate> aggregateList = new ArrayList<>();
		Flowable.fromIterable(processingList)
				.parallel()
				.runOn(Schedulers.io())
				.map(payload -> {
							setSecurityContext(authentication);

							MDC.put(CONSUMER_ID, consumerId);
							MDC.put(USER_ID, userId);
							MDC.put(CALL_ID, callId);

							Map<String, Object> valueMap = new HashMap<>();
							valueMap.put(DOKUMENTTYPEID.name(), dokumentTypeId);

							return new Aggregate(payload.getPlugin()
									.processElement(payload.getElement(), valueMap), payload.getOrgNode());
						}
				)
				.doOnError(throwable -> SecurityContextHolder.clearContext())
				.doOnComplete(SecurityContextHolder::clearContext)
				.sequential()
				.blockingSubscribe(
						aggregateList::add,
						unhandledError::add
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
		// If plugin doe\MottakerPluginTests in-place mutation, no aggregation is necessary.
		if (aggregate.getNewNode().isSameNode(orgElem)) {
			return;
		}
		Element element = (Element) aggregate.getNewNode();
		Node importNode = document.adoptNode(element);
		// Replace original element with new element.
		orgElem.getParentNode().insertBefore(importNode, orgElem);
		orgElem.getParentNode().removeChild(orgElem);
	}

	private void handleException(Throwable e) throws RegOppslagSecurityException {
		switch (e) {
			case UkjentAdressePersonErDoedException err -> throw err;
			case RegOppslagIngenTilgangException err -> throw err;
			case RegOppslagFunctionalException err -> {
				if (NOT_FOUND.equals(err.getHttpStatusCode())) {
					throw new RegOppslagIkkeFunnetException(err.getLocalizedMessage(), err, err.getHttpStatusCode());
				}
				throw new RegoppslagIllegalArgumentException(err.getMessage(), err, err.getHttpStatusCode());
			}
			case UkjentAdresseException exception -> throw exception;
			case FeilGrunnetHoeytVolumWorkaroundException ex -> throw ex;
			case RegOppslagSecurityException err -> throw err;
			case RegOppslagTechnicalException err -> throw err;
			case null, default -> throw new RegOppslagTechnicalException(e);
		}
	}

	private static void setSecurityContext(Authentication authentication) {
		// Setter securityContext for nye tråder
		SecurityContext newThreadSecurityContext = SecurityContextHolder.createEmptyContext();
		newThreadSecurityContext.setAuthentication(authentication);
		SecurityContextHolder.setContext(newThreadSecurityContext);
	}

}
