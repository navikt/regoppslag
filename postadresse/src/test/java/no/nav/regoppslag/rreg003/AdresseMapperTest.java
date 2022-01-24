package no.nav.regoppslag.rreg003;

import lombok.SneakyThrows;
import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.regoppslag.consumer.pdl.PdlGraphQLConsumer;
import no.nav.regoppslag.consumer.pdl.to.PdlMottakerInfo;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.regoppslag.service.LandkodeService;
import no.nav.regoppslag.service.LandkodeServiceNorsk;
import no.nav.regoppslag.service.PostnummerService;
import no.nav.regoppslag.util.PDLResponseUtil;
import no.nav.regoppslag.util.TestDataUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static no.nav.regoppslag.metrics.MetricLabels.SERVICE_CODE_TREG002;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@ExtendWith(MockitoExtension.class)
public class AdresseMapperTest {

	private LandkodeService landkodeService;
	private LandkodeServiceNorsk landkodeServiceNorsk;
	private MapPDLResponse mapPDLResponse;
	@Mock
	private MicrometerMetrics metrics;
	private AdresseMapper adresseMapper;
	private PdlGraphQLConsumer pdlGraphQLConsumer;
	@InjectMocks
	private PostnummerService postnummerService;

	@BeforeEach
	public void setUp() throws IOException {
		pdlGraphQLConsumer = mock(PdlGraphQLConsumer.class);
		landkodeService = new LandkodeService();
		landkodeServiceNorsk = new LandkodeServiceNorsk();
		postnummerService.init();
		mapPDLResponse = new MapPDLResponse(postnummerService, landkodeService, pdlGraphQLConsumer);
		adresseMapper = new AdresseMapper(landkodeService, metrics, landkodeServiceNorsk);
	}

	@Test
	public void shouldMapWithNorskPostAdresse() {
		Adresse adresse = adresseMapper.map(TestDataUtil.createMottaker());
		//adresse.setLandkode(TestDataUtil.LANDKODE);

		assertThat(adresse.getAdresselinje1(), is(TestDataUtil.ADRESSELINJE1));
		assertThat(adresse.getAdresselinje2(), is(TestDataUtil.ADRESSELINJE2));
		assertThat(adresse.getAdresselinje3(), is(TestDataUtil.ADRESSELINJE3));
		assertThat(adresse.getLandkode(), is(TestDataUtil.LANDKODE));
		assertThat(adresse.getLand(), is(TestDataUtil.LANDNAVN));
		assertThat(adresse.getPostnummer(), is(TestDataUtil.POSTNUMMER));
		assertThat(adresse.getPoststed(), is(TestDataUtil.POSTSTED));
	}

	@Test
	public void shouldMapWithUtenlandskPostAdresse() {
		Adresse adresse = adresseMapper.map(TestDataUtil.createMottaker(false));
		//adresse.setLandkode(TestDataUtil.SVENSK_LANDKODE);

		assertThat(adresse.getAdresselinje1(), is(TestDataUtil.UTENLANDSK_ADRESSELINJE1));
		assertThat(adresse.getAdresselinje2(), is(TestDataUtil.UTENLANDSK_ADRESSELINJE2));
		assertThat(adresse.getAdresselinje3(), is(TestDataUtil.UTENLANDSK_ADRESSELINJE3));
		assertThat(adresse.getLandkode(), is(TestDataUtil.SVENSK_LANDKODE));
		assertThat(adresse.getLand(), is(TestDataUtil.SVENSK_LAND));
		assertThat(adresse.getPostnummer(), nullValue());
		assertThat(adresse.getPoststed(), nullValue());
	}

	@Test
	public void shouldMapPDLWithNorskPostAdresse() {
		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(PDLResponseUtil.createPdlHentPerson(PDLResponseUtil.createPersonnavn()), SERVICE_CODE_TREG002, PDLResponseUtil.TEMA);

		Adresse adresse = adresseMapper.mapFraPdl(mottakerInfo);
		assertEquals(PDLResponseUtil.FRITTFORMAT_ADRESSELINJE1, adresse.getAdresselinje1());
		assertEquals(PDLResponseUtil.FRITTFORMAT_ADRESSELINJE2, adresse.getAdresselinje2());
		assertNull(adresse.getAdresselinje3());
		assertEquals(PDLResponseUtil.LANDKODE_NORGE, adresse.getLandkode());
		assertEquals(PDLResponseUtil.FRITTFORMAT_POSTNUMMER, adresse.getPostnummer());
		assertEquals(PDLResponseUtil.POSTSTED, adresse.getPoststed());
	}

	@SneakyThrows
	@Test
	public void shouldMapPDLWithUtenlandskPostAdresse() {
		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(PDLResponseUtil.createPdlHentPersonUtenlandskAdresse(), SERVICE_CODE_TREG002, PDLResponseUtil.TEMA);
		Adresse adresse = adresseMapper.mapFraPdl(mottakerInfo);

		assertEquals(PDLResponseUtil.POSTBOKSNUMMERNAVN, adresse.getAdresselinje1());
		assertEquals(PDLResponseUtil.POSTKODE_AND_BYSTED, adresse.getAdresselinje2());
		assertEquals(PDLResponseUtil.REGION_DISTRIKTOMRAADE, adresse.getAdresselinje3());
		assertNull(adresse.getPostnummer());
		assertNull(adresse.getPoststed());
		assertEquals(TestDataUtil.SVENSK_LANDKODE, adresse.getLandkode());

	}

	@Test
	public void shouldMapWhenLandkodeIsNull() {
		Mottaker mottaker = TestDataUtil.createMottaker();
		NorskPostadresse norskPostadresse = TestDataUtil.createNorskPostadresse();
		norskPostadresse.setLand(null);
		mottaker.setMottakeradresse(norskPostadresse);
		Adresse adresse = adresseMapper.map(mottaker);

		assertThat(adresse.getAdresselinje1(), is(TestDataUtil.ADRESSELINJE1));
		assertThat(adresse.getAdresselinje2(), is(TestDataUtil.ADRESSELINJE2));
		assertThat(adresse.getAdresselinje3(), is(TestDataUtil.ADRESSELINJE3));
		assertThat(adresse.getLandkode(), is("???"));
		assertThat(adresse.getPostnummer(), is(TestDataUtil.POSTNUMMER));
		assertThat(adresse.getPoststed(), is(TestDataUtil.POSTSTED));
	}


}