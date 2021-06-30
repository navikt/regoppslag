package no.nav.regoppslag.pdl;

import no.nav.regoppslag.consumer.pdl.PdlGraphQLConsumer;
import no.nav.regoppslag.consumer.pdl.PdlMottakerInfo;
import no.nav.regoppslag.consumer.pdl.pdlresponse.MapPDLResponse;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.regoppslag.service.PostnummerService;
import no.nav.regoppslag.util.PDLResponseUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static no.nav.regoppslag.consumer.pdl.pdlresponse.PDLConstant.POSTADRESSE_INNLAND;
import static no.nav.regoppslag.metrics.MetricLabels.SERVICE_CODE_TREG002;
import static no.nav.regoppslag.util.PDLResponseUtil.ADRESSENAVN_1;
import static no.nav.regoppslag.util.PDLResponseUtil.COADRESSENAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.DOEDSDATO;
import static no.nav.regoppslag.util.PDLResponseUtil.FOEDSELDATO;
import static no.nav.regoppslag.util.PDLResponseUtil.FULTTNAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.LANDKODE_NORGE;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTNUMMER;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTSTED;
import static no.nav.regoppslag.util.PDLResponseUtil.createPdlHentPersonWithPersonDoedOgAdvokatSomKontakt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MapPDLResponseTest {

	private PdlGraphQLConsumer pdlGraphQLConsumer;
	private MapPDLResponse mapPDLResponse;
	private PostnummerService postnummerService;
	private MicrometerMetrics metrics;

	@BeforeEach
	public void setUp() {
		pdlGraphQLConsumer = mock(PdlGraphQLConsumer.class);
		postnummerService = new PostnummerService();
		mapPDLResponse = new MapPDLResponse(postnummerService, metrics);
	}

	@Test
	public void shouldMapMidlretidigKontaktWithVegadresse() {
		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(PDLResponseUtil.createPdlHentPersonWithVegadresse());
		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(PDLResponseUtil.createPdlHentPersonWithVegadresse(), SERVICE_CODE_TREG002);

		assertEquals(mottakerInfo.getFoedselsdato(), FOEDSELDATO);
		assertNull(mottakerInfo.getDoedsdato());
		assertEquals(mottakerInfo.getNavn(), FULTTNAVN);
		assertEquals(mottakerInfo.getPostadresse().getAdresselinje1(), ADRESSENAVN_1);
		assertNull(mottakerInfo.getPostadresse().getAdresselinje2());
		assertEquals(mottakerInfo.getPostadresse().getAdresseType(), POSTADRESSE_INNLAND);
		assertEquals(mottakerInfo.getPostadresse().getPostnummer(), POSTNUMMER);
		assertEquals(mottakerInfo.getPostadresse().getPoststed(), POSTSTED);
		assertEquals(mottakerInfo.getPostadresse().getLandkode(), LANDKODE_NORGE);
	}

	@Test
	public void shouldMapMottkerInfoForDoedWithAdvokatSomKontkat() {
		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(createPdlHentPersonWithPersonDoedOgAdvokatSomKontakt());
		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(createPdlHentPersonWithPersonDoedOgAdvokatSomKontakt(), SERVICE_CODE_TREG002);

		assertEquals(mottakerInfo.getFoedselsdato(), FOEDSELDATO);
		assertEquals(mottakerInfo.getDoedsdato(), DOEDSDATO);
		assertEquals(mottakerInfo.getNavn(), FULTTNAVN);
		assertEquals(mottakerInfo.getPostadresse().getAdresselinje1(), COADRESSENAVN);
		assertEquals(mottakerInfo.getPostadresse().getAdresselinje2(), ADRESSENAVN_1);
		assertEquals(mottakerInfo.getPostadresse().getAdresseType(), POSTADRESSE_INNLAND);
		assertEquals(mottakerInfo.getPostadresse().getPostnummer(), POSTNUMMER);
		assertEquals(mottakerInfo.getPostadresse().getPoststed(), POSTSTED);
		assertEquals(mottakerInfo.getPostadresse().getLandkode(), LANDKODE_NORGE);
	}


}
