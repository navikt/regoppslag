package no.nav.regoppslag.xmlenricher;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagIkkeFunnetException;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;
import no.nav.regoppslag.exceptions.UkjentAdressePersonErDoed;
import no.nav.regoppslag.treg001.support.SpraakKodeMapper;
import no.nav.regoppslag.xmlenricher.exceptions.MissingPluginException;
import no.nav.regoppslag.xmlenricher.util.Aggregate;
import no.nav.regoppslag.xmlenricher.util.AttributeValueNamespaceResolver;
import no.nav.regoppslag.xmlenricher.util.Payload;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static no.nav.regoppslag.treg001.support.PluginUtil.createNewSecurityContext;
import static no.nav.regoppslag.treg001.support.PluginUtil.securityContextIsUsedForAuthentication;
import static no.nav.regoppslag.util.MDCConstants.CALL_ID;
import static no.nav.regoppslag.util.MDCConstants.CONSUMER_ID;
import static no.nav.regoppslag.util.MDCConstants.USER_ID;
import static no.nav.regoppslag.xmlenricher.util.ValueMapKeys.DOKUMENTTYPEID;
import static no.nav.regoppslag.xmlenricher.util.ValueMapKeys.MAALFORM;
import static org.springframework.http.HttpStatus.GONE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * @author Hans Petter Simonsen - Miles
 */
@Component
@Slf4j
public class ElementEnricher {

	private ElementEnricherPluginRegistry registry;
	private AttributeValueNamespaceResolver attributeValueNamespaceResolver;
	private static final String TREG001_FUN_FEIL = "TREG001 Funksjonell feil: {}";

	public ElementEnricher(ElementEnricherPluginRegistry registry) {
		this.registry = registry;
		attributeValueNamespaceResolver = new AttributeValueNamespaceResolver();
	}

	public void setRegistry(ElementEnricherPluginRegistry registry) {
		this.registry = registry;
	}

	private static Node findSingleNode(String xpathExpression, Document document) throws XPathExpressionException {

		XPath xPath = XPathFactory.newInstance().newXPath();

		XPathExpression xPathExpression = xPath.compile(xpathExpression);
		return (Node) xPathExpression.evaluate(document, XPathConstants.NODE);
	}

	public Document process(Document document, String dokumentTypeId, String tema) throws XPathExpressionException, MissingPluginException, RegOppslagTechnicalException, RegOppslagFunctionalException, RegOppslagSecurityException {

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
							if (securityContextIsUsedForAuthentication(payload)) {
								SecurityContextHolder.setContext(createNewSecurityContext(authentication, true));
							}

							MDC.put(CONSUMER_ID, consumerId);
							MDC.put(USER_ID, userId);
							MDC.put(CALL_ID, callId);

							Map<String, Object> valueMap = new HashMap<>();
							valueMap.put(DOKUMENTTYPEID.name(), dokumentTypeId);
							valueMap.put(MAALFORM.name(), new SpraakKodeMapper());

							return new Aggregate(payload.getPlugin()
									.processElement(payload.getElement(), valueMap, tema), payload.getOrgNode());
						}
				)
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

	private void handleException(Throwable e) throws RegOppslagFunctionalException, RegOppslagTechnicalException, RegOppslagSecurityException {
		if (e instanceof RegOppslagFunctionalException && GONE.equals(((RegOppslagFunctionalException) e).getHttpStatus())) {
			log.error(format(TREG001_FUN_FEIL, e.getMessage()), e);
			throw new UkjentAdressePersonErDoed(e.getLocalizedMessage(), e, "TREG001", ((RegOppslagFunctionalException) e).getHttpStatus());
		} else if (e instanceof RegOppslagFunctionalException | e instanceof NullPointerException) {
			log.warn(format(TREG001_FUN_FEIL, e.getMessage()));
			if (NOT_FOUND.equals(((RegOppslagFunctionalException) e).getHttpStatus())) {
				throw new RegOppslagIkkeFunnetException(e.getLocalizedMessage(), e, "TREG001", ((RegOppslagFunctionalException) e).getHttpStatus());
			}
			throw new RegoppslagIllegalArgumentException(e.getMessage(), e, "TREG001", ((RegOppslagFunctionalException) e).getHttpStatus());
		} else if (e instanceof RegOppslagSecurityException) {
			throw (RegOppslagSecurityException) e;
		} else if (e instanceof RegOppslagTechnicalException) {
			throw new RegOppslagTechnicalException(e.getMessage(), e, "TREG001", ((RegOppslagTechnicalException) e).getHttpStatus());
		} else {
			throw new RegOppslagTechnicalException(e, e.getClass().getSimpleName());
		}
	}

}
