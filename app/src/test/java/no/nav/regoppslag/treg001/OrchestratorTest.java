package no.nav.regoppslag.treg001;

import static no.nav.regoppslag.util.TestUtil.loadDocument;
import static no.nav.regoppslag.util.TestUtil.writeXml;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.nav.dok.metaforcemal.jaxb2.gen.Felles;
import no.nav.regoppslag.pluginsPOC.FailingPlugin;
import no.nav.regoppslag.pluginsPOC.MottakerPlugin1;
import no.nav.regoppslag.pluginsPOC.SignerendeSaksbehandlerPlugin;
import no.nav.regoppslag.treg001.plugins.SaksbehandlerPlugin;
import no.nav.regoppslag.xmlenricher.ElementEnricherPluginRegistry;
import no.nav.regoppslag.xmlenricher.SimplePluginRegistry;
import no.nav.regoppslag.xmlenricher.exceptions.MultiExceptionHolder;
import no.nav.regoppslag.xmlenricher.util.JaxbHelper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.context.ApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.io.File;

/**
 * @author Hans Petter Simonsen - Miles
 */
@Ignore
public class OrchestratorTest {

	public static final String BREVDATA1 = "src/test/resources/brevdata/eksempel1.xml";
	private static final String DOKUMENTTYPEID = "I000003";

	@Rule
	public ExpectedException expected = ExpectedException.none();
	
	ApplicationContext applicationContext=mock(ApplicationContext.class);
	
	@Before
	public void setup(){
		when(applicationContext.getBean("mottaker")).thenReturn(new MottakerPlugin1());
		when(applicationContext.getBean("signerendeSaksbehandler")).thenReturn(new SaksbehandlerPlugin());
	}
	

	@Test
	public void shouldProcessDocumentWithPlugin1() throws Exception, MultiExceptionHolder {
		File xmlFile = new File(BREVDATA1);
		Document document = loadDocument(xmlFile);

		ElementEnricherPluginRegistry registry = new SimplePluginRegistry(applicationContext);

		String xpath1 = "felles:ottaker";
		registry.registerPlugin(xpath1, MottakerPlugin1.class);

		Orchestrator orchestrator = new Orchestrator();
		orchestrator.setRegistry(registry);

		Document processed = orchestrator.process(document, DOKUMENTTYPEID);

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

		ElementEnricherPluginRegistry registry = new SimplePluginRegistry(applicationContext);

		String xpath1 = "felles:mottaker";
		registry.registerPlugin(xpath1, MottakerPlugin1.class);

		String xpath2 = "felles:signerendeSaksbehandler";
		registry.registerPlugin(xpath2, SignerendeSaksbehandlerPlugin.class);

		Orchestrator orchestrator = new Orchestrator();
		orchestrator.setRegistry(registry);

		Document processed = orchestrator.process(document, DOKUMENTTYPEID);

		writeXml(processed);

		Node fellesElement = processed.getElementsByTagNameNS("http://nav.no/dok/pesysbrev/v1/000073", "felles").item(0);

		JaxbHelper<Felles> fellesJaxbHelper = new JaxbHelper<Felles>(Felles.class);
		Felles felles = fellesJaxbHelper.unmarshal(fellesElement);

		assertThat(felles.getMottaker().getNavn(), is("Test Testesen"));
		assertThat(felles.getSignerendeSaksbehandler().getNavAnsatt().getNavn(), is("Flittige Frida"));


	}

	@Test(expected = MultiExceptionHolder.class)
	public void shouldHandleFailingPlugin() throws Exception, MultiExceptionHolder {
		File xmlFile = new File(BREVDATA1);
		Document document = loadDocument(xmlFile);

		ElementEnricherPluginRegistry registry = new SimplePluginRegistry(applicationContext);

		String xpath1 = "felles:mottaker";
		registry.registerPlugin(xpath1, FailingPlugin.class);

		Orchestrator orchestrator = new Orchestrator();
		orchestrator.setRegistry(registry);

		Document processed = orchestrator.process(document, DOKUMENTTYPEID);

		writeXml(processed);
	}
}
