package no.nav.regoppslag.xmlenricher.util;

import static org.apache.cxf.common.util.StringUtils.isEmpty;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Slf4j
public class AttributeValueNamespaceResolver {
	private static final String NODE_NAME_TYPE="type";

	public void resolveNamespace(Document xmlDocument, Node xpathResult) throws XPathExpressionException {


		// Case of qualified attribute values, we're forced to add corresponding namespace declaration manually
		if (xpathResult != null && xpathResult.hasAttributes()) {
			for (int i = 0; i < xpathResult.getAttributes().getLength(); i++) {
				Node attr = xpathResult.getAttributes().item(i);

				if (NODE_NAME_TYPE.equals(attr.getLocalName())) {
					String prefix = getAttributeValuePrefix(attr);
					if (!isEmpty(prefix)) {
						Attr existingAttr = ((Element) xpathResult).getAttributeNode("xmlns:" + prefix);
						if (existingAttr == null || existingAttr.getValue().isEmpty()) {
							String attrValNsElement = xpathResult.lookupNamespaceURI(prefix);

							if (attrValNsElement == null) {
								log.debug("Fant ikke namespace på element med prefix={}. Prøver å søke globalt i dokumentet", prefix);
								attrValNsElement = xmlDocument.lookupNamespaceURI(prefix);
							}
							updateOrAddAttributeNSToElement(attrValNsElement, xpathResult, prefix, attr);
						}
					}
				}
			}
		}

	}

	private String getAttributeValuePrefix(Node attr){
		if (attr.getNodeValue()==null || !attr.getNodeValue().contains(":")){
			return "";
		}
		return attr.getNodeValue().substring(0, attr.getNodeValue().indexOf(":"));
	}

	private void updateOrAddAttributeNSToElement(String attrValNs, Node xpathResult, String prefix, Node attr){
		if (attrValNs != null) {
			((Element) xpathResult).setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + prefix, attrValNs);
			log.debug("Lagt til xmlns attributt for prefix={} og namespace={}", prefix, attrValNs);
		} else {
			log.warn("Kunne ikke finne namespace med prefix {} på elementet {} med attributt {}", prefix, xpathResult.getLocalName(), attr.getLocalName());
		}

	}
}
