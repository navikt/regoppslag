package no.nav.regoppslag.util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;

/**
 * @author Hans Petter Simonsen - Miles
 */
public class TestUtil {
	public static Document loadDocument(File xmlFile) throws IOException, ParserConfigurationException, SAXException {
		FileInputStream fileIS = new FileInputStream(xmlFile);
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setNamespaceAware(true);
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document xmlDocument = builder.parse(fileIS);
		return xmlDocument;
	}

	public static Node findSingleNode(XPathExpression xpathExpression, Node xmlDocument) throws XPathExpressionException {
		Node node = (Node) xpathExpression.evaluate(xmlDocument, XPathConstants.NODE);
		return node;
	}


	
	public static void writeXml(Node doc) throws TransformerException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		//for pretty print
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		DOMSource source = new DOMSource(doc);
		
		StreamResult console = new StreamResult(System.out);

		transformer.transform(source, console);
	}
	
	public static Document stringToDocument(String xml) throws ParserConfigurationException, IOException, SAXException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setNamespaceAware(true);
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		StringReader str = new StringReader(xml);
		return builder.parse(new InputSource(str));
	}
}
