package no.nav.regoppslag.treg001.util;

import no.nav.dok.brevdata.felles.v1.navfelles.NavEnhet;
import no.nav.regoppslag.treg001.xmlenricher.util.AttributeValueNamespaceResolver;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import static no.nav.regoppslag.util.TestUtil.classpathToString;
import static no.nav.regoppslag.util.TestUtil.stringToDocument;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AttributeValueNamespaceResolverTest {

	AttributeValueNamespaceResolver resolver = new AttributeValueNamespaceResolver();

	String MOTTAKER_XPATH_EXPRESSION = "/*[local-name()='brevdata']/*[local-name()='NAVFelles']//*[local-name()='mottaker']";

	@Test
	public void shouldResolveEnhet() throws Exception {
		Document document = stringToDocument(classpathToString("__files/treg001/treg001_norg2_request.xml"));

		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile("/*[local-name()='brevdata']/*[local-name()='NAVFelles']/*[local-name()='signerendeSaksbehandler']/*[local-name()='navEnhet']");
		Node node = (Node) xPathExpression.evaluate(document, XPathConstants.NODE);

		JAXBContext context = JAXBContext.newInstance(NavEnhet.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		JAXBElement<NavEnhet> unmarshal = unmarshaller.unmarshal(node, NavEnhet.class);
		NavEnhet value = unmarshal.getValue();
		String enhetsId = value.getEnhetsId();
		assertEquals("0136", enhetsId);
	}

	@Test
	public void shouldResolve() throws Exception{
		Document document = stringToDocument(classpathToString("brevdata/brevdata_namespace_person.xml"));

		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(MOTTAKER_XPATH_EXPRESSION);
		Node node = (Node) xPathExpression.evaluate(document, XPathConstants.NODE);
		assertThat(node.getAttributes().getNamedItem("xmlns:nav"), nullValue());
		assertThat(node.getAttributes().getNamedItem("xsi:type").getNodeValue(), is("nav:Person"));

		resolver.resolveNamespace(document, node);

		assertThat(node.getAttributes().getNamedItem("xmlns:nav").getNodeValue(), is("http://nav.no/dok/brevdata/felles/v1/NAVFelles"));
		assertThat(node.getAttributes().getNamedItem("xsi:type").getNodeValue(), is("nav:Person"));

	}

	@Test
	public void shouldResolveTypeNamespaceSomethingElse() throws Exception{
		Document document = stringToDocument(classpathToString("brevdata/brevdata_namespace_person_not_xsi.xml"));

		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(MOTTAKER_XPATH_EXPRESSION);
		Node node = (Node) xPathExpression.evaluate(document, XPathConstants.NODE);
		assertNull(node.getAttributes().getNamedItem("xmlns:nav"));
		assertEquals("nav:Person", node.getAttributes().getNamedItem("something_else:type").getNodeValue());

		resolver.resolveNamespace(document, node);

		assertEquals("http://nav.no/dok/brevdata/felles/v1/NAVFelles", node.getAttributes().getNamedItem("xmlns:nav").getNodeValue());
		assertEquals("nav:Person", node.getAttributes().getNamedItem("something_else:type").getNodeValue());

	}




}