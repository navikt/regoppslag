package no.nav.regoppslag.treg001.xmlenricher;

import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.Person;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.treg001.MapPdlForTreg001;
import no.nav.regoppslag.treg001.MottakerPlugin;
import no.nav.regoppslag.treg001.KompletterBrevdataService;
import no.nav.regoppslag.treg001.xmlenricher.util.JaxbHelper;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.SECONDS;
import static no.nav.regoppslag.treg001.KompletterBrevdataService.stringToDocument;
import static no.nav.regoppslag.util.TestUtil.classpathToString;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ElementEnricherTest {

	@Test
	void retainsInheritedNamespacesInAnIsolatedDocument() throws Exception {
		Document document = stringToDocument(classpathToString("brevdata/brevdata_namespace_person.xml"));
		MapPdlForTreg001 mapper = mock(MapPdlForTreg001.class);
		when(mapper.getMottakerFraPdl(any(), eq("123"))).thenAnswer(invocation -> {
			Mottaker mottaker = invocation.getArgument(0);
			assertInstanceOf(Person.class, mottaker);
			assertEquals("11111111111", mottaker.getId());
			return mottaker;
		});
		ElementEnricherPluginRegistry registry = mock(ElementEnricherPluginRegistry.class);
		String xpath = "/*[local-name()='brevdata']/*[local-name()='NAVFelles']//*[local-name()='mottaker']";
		when(registry.getSupportedElements()).thenReturn(Set.of(xpath));
		MottakerPlugin plugin = new MottakerPlugin(mapper);
		when(registry.getOrCreateElementEnricherPlugin(xpath)).thenReturn((node, values) -> {
			assertEquals("http://nav.no/dok/brevdata/felles/v1/NAVFelles", node.lookupNamespaceURI("nav"));
			assertEquals("http://www.w3.org/2001/XMLSchema-instance", node.lookupNamespaceURI("xsi"));
			return plugin.processElement(node, values);
		});

		Document enriched = new ElementEnricher(registry).process(document, "123");
		Document reparsed = stringToDocument(KompletterBrevdataService.documentToString(enriched));
		Node mottakerNode = reparsed.getElementsByTagNameNS("http://nav.no/dok/urbrev/felles/ur_felles", "mottaker").item(0);
		assertNotNull(mottakerNode);
		Mottaker mottaker = new JaxbHelper<>(Mottaker.class).unmarshal(mottakerNode);
		assertInstanceOf(Person.class, mottaker);
		assertEquals("11111111111", mottaker.getId());
	}

	@Test
	void failsFastWhenAnotherPluginIsStillRunning() throws Exception {
		assumeTrue(Runtime.getRuntime().availableProcessors() > 1);
		ElementEnricherPluginRegistry registry = mock(ElementEnricherPluginRegistry.class);
		ElementEnricherPlugin failingPlugin = mock(ElementEnricherPlugin.class);
		ElementEnricherPlugin slowPlugin = mock(ElementEnricherPlugin.class);
		CountDownLatch slowStarted = new CountDownLatch(1);
		CountDownLatch finishSlowPlugin = new CountDownLatch(1);
		Document document = stringToDocument("<brevdata><a/><b/></brevdata>");

		when(registry.getSupportedElements()).thenReturn(Set.of("/*/a", "/*/b"));
		when(registry.getOrCreateElementEnricherPlugin("/*/a")).thenReturn(failingPlugin);
		when(registry.getOrCreateElementEnricherPlugin("/*/b")).thenReturn(slowPlugin);
		when(failingPlugin.processElement(any(), any())).thenAnswer(invocation -> {
			assertTrue(slowStarted.await(5, SECONDS));
			assertNotSame(document, invocation.getArgument(0, Node.class).getOwnerDocument());
			throw new RegOppslagTechnicalException(new IllegalStateException("Plugin failed"));
		});
		when(slowPlugin.processElement(any(), any())).thenAnswer(invocation -> {
			slowStarted.countDown();
			assertTrue(finishSlowPlugin.await(5, SECONDS));
			return invocation.getArgument(0);
		});

		CompletableFuture<RegOppslagTechnicalException> result = CompletableFuture.supplyAsync(
				() -> assertThrows(RegOppslagTechnicalException.class, () -> new ElementEnricher(registry).process(document, "123")));
		try {
			assertTrue(result.get(5, SECONDS).getMessage().contains("Plugin failed"));
		} finally {
			finishSlowPlugin.countDown();
		}
	}
}
