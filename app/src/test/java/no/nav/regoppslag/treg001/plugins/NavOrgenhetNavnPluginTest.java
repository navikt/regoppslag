package no.nav.regoppslag.treg001.plugins;

import static no.nav.regoppslag.util.TestUtil.findSingleNode;
import static no.nav.regoppslag.util.TestUtil.loadDocument;
import static no.nav.regoppslag.util.TestUtil.writeXml;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import no.nav.dok.metaforcemal.jaxb2.gen.NavEnhet;
import no.nav.regoppslag.consumer.norg2.OrganisasjonEnhetKontaktinformasjonV1Consumer;
import no.nav.regoppslag.consumer.norg2.support.Norg2Mapper;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.xmlenricher.util.JaxbHelper;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.Organisasjonsenhet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.io.File;

@RunWith(SpringJUnit4ClassRunner.class)
public class NavOrgenhetNavnPluginTest {
	public static final String BREVDATA1 = "src/test/resources/brevdata/eksempel1.xml";

	private static final String NAV_ENHET_NAVN = "NAV Husnes";
	private static final String DOKUMENTTYPEID = "I000003";

	private OrganisasjonEnhetKontaktinformasjonV1Consumer norgConsumer = Mockito.mock(OrganisasjonEnhetKontaktinformasjonV1Consumer.class);
	private Norg2Mapper norg2Mapper = new Norg2Mapper();
	private NavOrgenhetNavnPlugin norgPlugin = new NavOrgenhetNavnPlugin(norgConsumer, norg2Mapper);

	@Before
	public void setUp() throws RegOppslagFunctionalException {
		when(norgConsumer.hentKontaktinformasjonForEnhet(any(String.class))).thenReturn(createEnhet(NAV_ENHET_NAVN));
	}
	@Test
	public void testNavOrgenhetNavnPlugin() throws Exception {
		File xmlFile = new File(BREVDATA1);
		Document document = loadDocument(xmlFile);

		QName qName = new QName("http://nav.no/dok/pesysbrev/felles/v1/PesysFelles","kontaktinformasjon");
		Node node = findSingleNode(qName, document);

		writeXml(node);

		Node processed = norgPlugin.processElement(node, DOKUMENTTYPEID);
		writeXml(processed);

		JaxbHelper<NavEnhet> enhetJaxbHelper = new JaxbHelper<NavEnhet>(NavEnhet.class);
		NavEnhet navEnhet = enhetJaxbHelper.unmarshal(processed);

		assertThat(navEnhet.getEnhetsNavn(), is(NAV_ENHET_NAVN));
	}

	private Organisasjonsenhet createEnhet(String navEnhetNavn) {
		Organisasjonsenhet enhet = new Organisasjonsenhet();
		enhet.setEnhetNavn(navEnhetNavn);
		return enhet;
	}
}