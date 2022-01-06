package no.nav.regoppslag.xmlenricher.util;

import static org.apache.cxf.common.util.StringUtils.isEmpty;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *  I tilfeller der attributtverdier i xml-elementer er kvalifiserte, f.eks {@code xsi:type="nav:Person"}, er det ikke alltid
 *  nødvendige namespace-deklarasjoner ligger med på element-nivå. Dette fungerer oftest greit når man marshaller hele xml dokumentet,
 *  fordi namespace-deklarasjonene ofte ligger på toppen på rotelementet og arves av underelementene. Når man skal marshalle frittstående
 *  enkeltelementer som er løskoblet fra rot-elementet, må alle nødvendige namespace-deklarasjoner ligge med.
 *  Denne klassen løser et subsett av disse tilfellene ved å legge på namespace-deklarasjoner som mangler,
 *  f.eks {@code xmlns:nav="http://nav.no/dok/brevdata/felles/v1/NAVFelles"}.
 *  Denne klassen leter etter attributter med localname 'type' på elementnoden.
 *  * den går _ikke_ rekursivt gjennom underelementer
 *  * den behandler _ikke_ andre kvalifiserte attributtverdier enn for attributtet xsi:type
 *  Dersom namespacet ikke allerede er deklarert på elementet søker vi gjennom elementet, dernest gjennom xml dokumentet for å
 *  finne tilhørende namespace for prefixet og legger dette til på elementet.
 *
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Slf4j
public class AttributeValueNamespaceResolver {
	private static final String NODE_NAME_TYPE="type";

	/**
	 *  Denne metoden leter etter attributter med localname 'type' på elementnoden {@code xpathResult}.
	 *  * den går _ikke_ rekursivt gjennom underelementer
	 *  * den behandler _ikke_ andre kvalifiserte attributtverdier enn for attributtet {@code xsi:type}
	 *  Dersom namespacet ikke allerede er deklarert på elementet søker vi gjennom elementet {@code xpathResult}, dernest
	 *  gjennom xml dokumentet {@code xmlDocument} for å finne tilhørende namespace for prefixet og legger dette til på elementet.
	 * @param xmlDocument Hele xml dokumentet
	 * @param xpathResult En enkel xml node fra xml dokumentet
	 */
	public void resolveNamespace(Document xmlDocument, Node xpathResult){

		// Case of qualified attribute values, we're forced to add corresponding namespace declaration manually
		if (xpathResult != null && xpathResult.hasAttributes()) {
			for (int i = 0; i < xpathResult.getAttributes().getLength(); i++) {
				Node attr = xpathResult.getAttributes().item(i);

				if (NODE_NAME_TYPE.equals(attr.getLocalName())) {
					String prefix = getAttributeValuePrefix(attr);
					if (!StringUtils.isEmpty(prefix)) {
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
		return attr.getNodeValue().substring(0, attr.getNodeValue().indexOf(':'));
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
