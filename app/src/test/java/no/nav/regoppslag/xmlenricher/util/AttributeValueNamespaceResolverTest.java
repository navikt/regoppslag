package no.nav.regoppslag.xmlenricher.util;

import static no.nav.regoppslag.util.TestUtil.classpathToString;
import static no.nav.regoppslag.util.TestUtil.stringToDocument;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class AttributeValueNamespaceResolverTest {

	AttributeValueNamespaceResolver resolver = new AttributeValueNamespaceResolver();

	String MOTTAKER_XPATH_EXPRESSION = "/*[local-name()='brevdata']/*[local-name()='NAVFelles']//*[local-name()='mottaker']";

	@Test
	public void shouldReseolve() throws Exception{
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
	public void shouldReseolveTypeNamespaceSomethingElse() throws Exception{
		Document document = stringToDocument(classpathToString("brevdata/brevdata_namespace_person_not_xsi.xml"));

		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(MOTTAKER_XPATH_EXPRESSION);
		Node node = (Node) xPathExpression.evaluate(document, XPathConstants.NODE);
		assertThat(node.getAttributes().getNamedItem("xmlns:nav"), nullValue());
		assertThat(node.getAttributes().getNamedItem("something_else:type").getNodeValue(), is("nav:Person"));

		resolver.resolveNamespace(document, node);

		assertThat(node.getAttributes().getNamedItem("xmlns:nav").getNodeValue(), is("http://nav.no/dok/brevdata/felles/v1/NAVFelles"));
		assertThat(node.getAttributes().getNamedItem("something_else:type").getNodeValue(), is("nav:Person"));

	}




}