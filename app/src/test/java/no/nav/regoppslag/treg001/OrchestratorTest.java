package no.nav.regoppslag.treg001;

import static no.nav.regoppslag.util.TestUtil.loadDocument;
import static no.nav.regoppslag.util.TestUtil.writeXml;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import no.nav.dok.metaforcemal.jaxb2.gen.Felles;
import no.nav.regoppslag.plugins.FailingPlugin;
import no.nav.regoppslag.plugins.MottakerPlugin1;
import no.nav.regoppslag.plugins.SignerendeSaksbehandlerPlugin;
import no.nav.regoppslag.xmlenricher.ElementEnricherPluginRegistry;
import no.nav.regoppslag.xmlenricher.SimplePluginRegistry;
import no.nav.regoppslag.xmlenricher.exceptions.MultiExceptionHolder;
import no.nav.regoppslag.xmlenricher.util.JaxbHelper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.io.File;

/**
 * @author Hans Petter Simonsen - Miles
 */
public class OrchestratorTest {

	public static final String BREVDATA1 = "src/test/resources/brevdata/eksempel1.xml";

	@Rule
	public ExpectedException expected = ExpectedException.none();

	@Test
	public void shouldProcessDocumentWithPlugin1() throws Exception, MultiExceptionHolder {
		File xmlFile = new File(BREVDATA1);
		Document document = loadDocument(xmlFile);

		ElementEnricherPluginRegistry registry = new SimplePluginRegistry();

		QName qname = new QName("http://nav.no/dok/pesysbrev/felles/v1/PesysFelles", "mottaker");
		registry.registerPlugin(qname, MottakerPlugin1.class);

		Orchestrator orchestrator = new Orchestrator();
		orchestrator.setRegistry(registry);

		Document processed = orchestrator.process(document);

		writeXml(processed);

		Node fellesElement = processed.getElementsByTagNameNS("http://nav.no/dok/pesysbrev/v1/000073", "felles").item(0);
		JaxbHelper<Felles> fellesJaxbHelper = new JaxbHelper<Felles>(Felles.class);
		Felles felles = fellesJaxbHelper.unmarshal(fellesElement);

		assertThat(felles.getMottaker().getNavn(), is("Test Testesen"));
	}

	@Test
	public void shouldProcessDocumentWithPlugin1And2() throws Exception, MultiExceptionHolder {
		File xmlFile = new File(BREVDATA1);
		Document document = loadDocument(xmlFile);

		ElementEnricherPluginRegistry registry = new SimplePluginRegistry();

		QName qname1 = new QName("http://nav.no/dok/pesysbrev/felles/v1/PesysFelles", "mottaker");
		registry.registerPlugin(qname1, MottakerPlugin1.class);

		QName qname2 = new QName("http://nav.no/dok/pesysbrev/felles/v1/PesysFelles", "signerendeSaksbehandler");
		registry.registerPlugin(qname2, SignerendeSaksbehandlerPlugin.class);

		Orchestrator orchestrator = new Orchestrator();
		orchestrator.setRegistry(registry);

		Document processed = orchestrator.process(document);

		writeXml(processed);

		Node fellesElement = processed.getElementsByTagNameNS("http://nav.no/dok/pesysbrev/v1/000073", "felles").item(0);

		JaxbHelper<Felles> fellesJaxbHelper = new JaxbHelper<Felles>(Felles.class);
		Felles felles = fellesJaxbHelper.unmarshal(fellesElement);

		assertThat(felles.getMottaker().getNavn(), is("Test Testesen"));
		assertThat(felles.getSignerendeSaksbehandler().getNavn(), is("Flittige Frida"));


	}

	@Test(expected = MultiExceptionHolder.class)
	public void shouldHandleFailingPlugin() throws Exception, MultiExceptionHolder {
		File xmlFile = new File(BREVDATA1);
		Document document = loadDocument(xmlFile);

		ElementEnricherPluginRegistry registry = new SimplePluginRegistry();

		QName qname1 = new QName("http://nav.no/dok/pesysbrev/felles/v1/PesysFelles", "mottaker");
		registry.registerPlugin(qname1, FailingPlugin.class);

		Orchestrator orchestrator = new Orchestrator();
		orchestrator.setRegistry(registry);

		Document processed = orchestrator.process(document);

		writeXml(processed);
	}

}
