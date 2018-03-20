package no.nav.regoppslag.pluginsPOC;

import static no.nav.regoppslag.util.TestUtil.findSingleNode;
import static no.nav.regoppslag.util.TestUtil.loadDocument;
import static no.nav.regoppslag.util.TestUtil.writeXml;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import no.nav.dok.metaforcemal.jaxb2.gen.Mottaker;
import no.nav.dok.metaforcemal.jaxb2.gen.NorskPostadresse;
import no.nav.regoppslag.xmlenricher.util.JaxbHelper;
import no.nav.regoppslag.xmlenricher.util.UniversalNamespaceCache;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;

/**
 * @author Hans Petter Simonsen - Miles
 */
public class MottakerPlugin1Test {
	public static final String BREVDATA1 = "src/test/resources/brevdata/eksempel1.xml";
	private static final String DOKUMENTTYPEID = "I000003";

	@Test
	public void testPlugin1() throws Exception {
		File xmlFile = new File(BREVDATA1);
		Document document = loadDocument(xmlFile);

		String expression1 = "//felles:mottaker";
		XPath xPath = XPathFactory.newInstance().newXPath();
		NamespaceContext namespaceContext = new UniversalNamespaceCache(document, false);
		xPath.setNamespaceContext(namespaceContext);
		XPathExpression xPathExpression = xPath.compile(expression1);

		Node node = findSingleNode(xPathExpression, document);

		writeXml(node);

		MottakerPlugin1 plugin1 = new MottakerPlugin1();
		Node processed = plugin1.processElement(node, DOKUMENTTYPEID);
		writeXml(processed);

		JaxbHelper<Mottaker> mottakerJaxbHelper = new JaxbHelper<Mottaker>(Mottaker.class);
		Mottaker mottaker = mottakerJaxbHelper.unmarshal(processed);

		assertThat(mottaker.getNavn(), is("Test Testesen"));
		assertThat(((NorskPostadresse)mottaker.getAdresse()).getAdresselinje1(), is("Heimegata 2"));
	}
}