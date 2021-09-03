package no.nav.regoppslag.treg002;

import lombok.SneakyThrows;
import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.regoppslag.api.HentMottakerOgAdresseResponse;
import no.nav.regoppslag.consumer.pdl.map.MapPDLResponse;
import no.nav.regoppslag.consumer.pdl.to.PdlMottakerInfo;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.regoppslag.service.LandkodeService;
import no.nav.regoppslag.service.PostnummerService;
import no.nav.regoppslag.util.TestDataUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static no.nav.regoppslag.metrics.MetricLabels.SERVICE_CODE_TREG002;
import static no.nav.regoppslag.util.PDLResponseUtil.BYSTED;
import static no.nav.regoppslag.util.PDLResponseUtil.FRITTFORMAT_ADRESSELINJE1;
import static no.nav.regoppslag.util.PDLResponseUtil.FRITTFORMAT_ADRESSELINJE2;
import static no.nav.regoppslag.util.PDLResponseUtil.FRITTFORMAT_POSTNUMMER;
import static no.nav.regoppslag.util.PDLResponseUtil.LANDKODE_NORGE;
import static no.nav.regoppslag.util.PDLResponseUtil.LANDKODE_UTENLANDSK;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTBOKSNUMMERNAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTKODE;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTSTED;
import static no.nav.regoppslag.util.PDLResponseUtil.createPdlHentPerson;
import static no.nav.regoppslag.util.PDLResponseUtil.createPdlHentPersonUtenlandskAdresse;
import static no.nav.regoppslag.util.PDLResponseUtil.createPersonnavn;
import static no.nav.regoppslag.util.TestDataUtil.ADRESSELINJE1;
import static no.nav.regoppslag.util.TestDataUtil.ADRESSELINJE2;
import static no.nav.regoppslag.util.TestDataUtil.ADRESSELINJE3;
import static no.nav.regoppslag.util.TestDataUtil.LANDKODE;
import static no.nav.regoppslag.util.TestDataUtil.POSTNUMMER;
import static no.nav.regoppslag.util.TestDataUtil.SVENSK_LANDKODE;
import static no.nav.regoppslag.util.TestDataUtil.UTENLANDSK_ADRESSELINJE1;
import static no.nav.regoppslag.util.TestDataUtil.UTENLANDSK_ADRESSELINJE2;
import static no.nav.regoppslag.util.TestDataUtil.UTENLANDSK_ADRESSELINJE3;
import static no.nav.regoppslag.util.TestDataUtil.createMottaker;
import static no.nav.regoppslag.util.TestDataUtil.createNorskPostadresse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@ExtendWith(MockitoExtension.class)
public class AdresseMapperTest {

	@InjectMocks
	private LandkodeService landkodeService;

	@InjectMocks
	private MapPDLResponse mapPDLResponse;
	@Mock
	private MicrometerMetrics metrics;
	@InjectMocks
	private AdresseMapper adresseMapper;
	@InjectMocks
	private PostnummerService postnummerService;

	@BeforeEach
	public void setUp() throws IOException {
		landkodeService = new LandkodeService();
		postnummerService.init();
		mapPDLResponse = new MapPDLResponse(postnummerService, landkodeService);
		adresseMapper = new AdresseMapper(landkodeService, metrics);
	}

	@Test
	public void shouldMapWithNorskPostAdresse() {
		HentMottakerOgAdresseResponse.Adresse adresse = adresseMapper.map(createMottaker());
		adresse.setLandkode(LANDKODE);
		assertThat(adresse.getAdresselinje1(), is(ADRESSELINJE1));
		assertThat(adresse.getAdresselinje2(), is(ADRESSELINJE2));
		assertThat(adresse.getAdresselinje3(), is(ADRESSELINJE3));
		assertThat(adresse.getLandkode(), is(LANDKODE));
		assertThat(adresse.getPostnummer(), is(POSTNUMMER));
		assertThat(adresse.getPoststed(), is(TestDataUtil.POSTSTED));
	}

	@Test
	public void shouldMapWithUtenlandskPostAdresse() {
		HentMottakerOgAdresseResponse.Adresse adresse = adresseMapper.map(createMottaker(false));
		adresse.setLandkode(SVENSK_LANDKODE);

		assertThat(adresse.getAdresselinje1(), is(UTENLANDSK_ADRESSELINJE1));
		assertThat(adresse.getAdresselinje2(), is(UTENLANDSK_ADRESSELINJE2));
		assertThat(adresse.getAdresselinje3(), is(UTENLANDSK_ADRESSELINJE3));
		assertThat(adresse.getLandkode(), is(SVENSK_LANDKODE));
		assertThat(adresse.getPostnummer(), nullValue());
		assertThat(adresse.getPoststed(), nullValue());
	}

	@Test
	public void shouldMapPDLWithNorskPostAdresse() {
		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(createPdlHentPerson(createPersonnavn()), SERVICE_CODE_TREG002);

		HentMottakerOgAdresseResponse.Adresse adresse = adresseMapper.mapFraPdl(mottakerInfo);
		assertEquals(FRITTFORMAT_ADRESSELINJE1, adresse.getAdresselinje1());
		assertEquals(FRITTFORMAT_ADRESSELINJE2, adresse.getAdresselinje2());
		assertNull(adresse.getAdresselinje3());
		assertEquals(LANDKODE_NORGE, adresse.getLandkode());
		assertEquals(FRITTFORMAT_POSTNUMMER, adresse.getPostnummer());
		assertEquals(POSTSTED, adresse.getPoststed());
	}

	@SneakyThrows
	@Test
	public void shouldMapPDLWithUtenlandskPostAdresse() {
		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(createPdlHentPersonUtenlandskAdresse(), SERVICE_CODE_TREG002);
		HentMottakerOgAdresseResponse.Adresse adresse = adresseMapper.mapFraPdl(mottakerInfo);

		assertEquals(POSTBOKSNUMMERNAVN, adresse.getAdresselinje1());
		assertEquals(POSTKODE, adresse.getAdresselinje2());
		assertEquals(BYSTED, adresse.getAdresselinje3());
		assertNull(adresse.getPostnummer());
		assertNull(adresse.getPoststed());
		assertEquals(SVENSK_LANDKODE, adresse.getLandkode());

	}

	@Test
	public void shouldMapWhenLandkodeIsNull() {
		Mottaker mottaker = createMottaker();
		NorskPostadresse norskPostadresse = createNorskPostadresse();
		norskPostadresse.setLand(null);
		mottaker.setMottakeradresse(norskPostadresse);
		HentMottakerOgAdresseResponse.Adresse adresse = adresseMapper.map(mottaker);

		assertThat(adresse.getAdresselinje1(), is(ADRESSELINJE1));
		assertThat(adresse.getAdresselinje2(), is(ADRESSELINJE2));
		assertThat(adresse.getAdresselinje3(), is(ADRESSELINJE3));
		assertThat(adresse.getLandkode(), is("???"));
		assertThat(adresse.getPostnummer(), is(POSTNUMMER));
		assertThat(adresse.getPoststed(), is(TestDataUtil.POSTSTED));
	}


}