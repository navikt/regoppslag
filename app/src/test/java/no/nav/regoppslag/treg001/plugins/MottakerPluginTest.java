package no.nav.regoppslag.treg001.plugins;

import static no.nav.regoppslag.util.TestUtil.findSingleNode;
import static no.nav.regoppslag.util.TestUtil.loadDocument;
import static no.nav.regoppslag.util.TestUtil.writeXml;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import no.nav.dok.metaforcemal.jaxb2.gen.Mottaker;
import no.nav.regoppslag.consumer.personv3.PersonV3Consumer;
import no.nav.regoppslag.consumer.personv3.support.PersonV3Mapper;
import no.nav.regoppslag.xmlenricher.util.JaxbHelper;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personnavn;
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
public class MottakerPluginTest {
	public static final String BREVDATA1 = "src/test/resources/brevdata/eksempel1.xml";

	private static final String FORNAVN = "TOM";
	private static final String MELLOMNAVN = "MARVOLO";
	private static final String ETTERNAVN = "RIDDLE";

	private PersonV3Consumer personV3Consumer = Mockito.mock(PersonV3Consumer.class);
	private PersonV3Mapper personV3Mapper = new PersonV3Mapper();
	private MottakerPlugin mottakerPlugin = new MottakerPlugin(personV3Consumer, personV3Mapper);

	@Before
	public void setUp() {
		when(personV3Consumer.hentPerson(any(String.class))).thenReturn(createPerson(FORNAVN, null, ETTERNAVN));
	}
	@Test
	public void testMottakerPlugin() throws Exception {
		File xmlFile = new File(BREVDATA1);
		Document document = loadDocument(xmlFile);

		QName qName = new QName("http://nav.no/dok/pesysbrev/felles/v1/PesysFelles","mottaker");
		Node node = findSingleNode(qName, document);

		writeXml(node);

		Node processed = mottakerPlugin.processElement(node);
		writeXml(processed);

		JaxbHelper<Mottaker> mottakerJaxbHelper = new JaxbHelper<Mottaker>(Mottaker.class);
		Mottaker mottaker= mottakerJaxbHelper.unmarshal(processed);

		assertThat(mottaker.getNavn(), is(FORNAVN + " " + ETTERNAVN));
	}

	private Person createPerson(String fornavn, String mellomnavn, String etternavn) {
		Personnavn personnavn = new Personnavn();
		personnavn.setFornavn(fornavn);
		if (mellomnavn != null) {
			personnavn.setMellomnavn(mellomnavn);
			personnavn.setSammensattNavn(fornavn + " " + mellomnavn + " " + etternavn);
		} else {
			personnavn.setSammensattNavn(fornavn + " " + etternavn);
		}
		personnavn.setEtternavn(etternavn);
		Person person = new Person();
		person.setPersonnavn(personnavn);
		return person;
	}
}