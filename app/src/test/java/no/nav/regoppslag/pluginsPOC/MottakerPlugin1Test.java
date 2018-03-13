package no.nav.regoppslag.pluginsPOC;

import static no.nav.regoppslag.util.TestUtil.findSingleNode;
import static no.nav.regoppslag.util.TestUtil.loadDocument;
import static no.nav.regoppslag.util.TestUtil.writeXml;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import no.nav.dok.metaforcemal.jaxb2.gen.Mottaker;
import no.nav.dok.metaforcemal.jaxb2.gen.NorskPostadresse;
import no.nav.regoppslag.xmlenricher.util.JaxbHelper;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
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

		QName qName = new QName("http://nav.no/dok/pesysbrev/felles/v1/PesysFelles","mottaker");
		Node node = findSingleNode(qName, document);

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