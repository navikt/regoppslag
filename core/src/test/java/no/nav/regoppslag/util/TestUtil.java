package no.nav.regoppslag.util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

public class TestUtil {

	public static String classpathToString(String path){
		try (InputStream resourceAsStream = TestUtil.class.getClassLoader().getResourceAsStream(path)) {
			return new String(resourceAsStream.readAllBytes());
		}  catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Document loadDocument(File xmlFile) throws IOException, ParserConfigurationException, SAXException {
		FileInputStream fileIS = new FileInputStream(xmlFile);
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setNamespaceAware(true);
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		return builder.parse(fileIS);
	}

	public static Node findSingleNode(XPathExpression xpathExpression, Node xmlDocument) throws XPathExpressionException {
		return (Node) xpathExpression.evaluate(xmlDocument, XPathConstants.NODE);
	}

	public static Document stringToDocument(String xml) throws ParserConfigurationException, IOException, SAXException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setNamespaceAware(true);
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		StringReader str = new StringReader(xml);
		return builder.parse(new InputSource(str));
	}
}
