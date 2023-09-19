package no.nav.regoppslag.rreg003;

import lombok.SneakyThrows;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.regoppslag.consumer.ereg.MottakerTo;
import no.nav.regoppslag.consumer.pdl.PdlGraphQLConsumer;
import no.nav.regoppslag.consumer.pdl.to.PdlMottakerInfo;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.regoppslag.pdl.DoedsboAdresseService;
import no.nav.regoppslag.pdl.MapPDLResponse;
import no.nav.regoppslag.pdl.NorskAdresseService;
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
import java.time.Clock;

import static no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode.ENHETFORRETNINGSADRESSE;
import static no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode.ENHETPOSTADRESSE;
import static no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode.KONTAKTADRESSE;
import static no.nav.regoppslag.metrics.MetricLabels.SERVICE_CODE_TREG002;
import static no.nav.regoppslag.util.PDLResponseUtil.FRITTFORMAT_ADRESSELINJE1;
import static no.nav.regoppslag.util.PDLResponseUtil.FRITTFORMAT_ADRESSELINJE2;
import static no.nav.regoppslag.util.PDLResponseUtil.FRITTFORMAT_POSTNUMMER;
import static no.nav.regoppslag.util.PDLResponseUtil.LANDKODE_NORGE;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTBOKSNUMMERNAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTKODE_AND_BYSTED;
import static no.nav.regoppslag.util.PDLResponseUtil.TEMA;
import static no.nav.regoppslag.util.PDLResponseUtil.createPdlHentPerson;
import static no.nav.regoppslag.util.PDLResponseUtil.createPdlHentPersonUtenlandskAdresse;
import static no.nav.regoppslag.util.PDLResponseUtil.createPersonnavn;
import static no.nav.regoppslag.util.TestDataUtil.ADRESSELINJE1;
import static no.nav.regoppslag.util.TestDataUtil.ADRESSELINJE2;
import static no.nav.regoppslag.util.TestDataUtil.ADRESSELINJE3;
import static no.nav.regoppslag.util.TestDataUtil.LANDKODE;
import static no.nav.regoppslag.util.TestDataUtil.LANDNAVN;
import static no.nav.regoppslag.util.TestDataUtil.POSTNUMMER;
import static no.nav.regoppslag.util.TestDataUtil.SVENSK_LANDKODE;
import static no.nav.regoppslag.util.TestDataUtil.SVERIGE;
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
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class AdresseMapperTest {

	private MapPDLResponse mapPDLResponse;
	@Mock
	private MicrometerMetrics metrics;
	private AdresseMapper adresseMapper;
	@InjectMocks
	private PostnummerService postnummerService;

	@BeforeEach
	public void setUp() throws IOException {
		PdlGraphQLConsumer pdlGraphQLConsumer = mock(PdlGraphQLConsumer.class);
		LandkodeServiceNorsk landkodeServiceNorsk = new LandkodeServiceNorsk();
		mapPDLResponse = new MapPDLResponse(new DoedsboAdresseService(postnummerService, pdlGraphQLConsumer), new NorskAdresseService(postnummerService), Clock.systemDefaultZone());
		adresseMapper = new AdresseMapper(metrics, landkodeServiceNorsk);
	}

	@Test
	public void shouldMapWithNorskPostAdresse() {
		var mottaker = createMottaker(ENHETPOSTADRESSE);

		Adresse adresse = adresseMapper.map(mottaker);

		assertEquals(ENHETPOSTADRESSE, adresse.getAdresseKilde());
		assertThat(adresse.getAdresselinje1(), is(ADRESSELINJE1));
		assertThat(adresse.getAdresselinje2(), is(ADRESSELINJE2));
		assertThat(adresse.getAdresselinje3(), is(ADRESSELINJE3));
		assertThat(adresse.getLandkode(), is(LANDKODE));
		assertThat(adresse.getLand(), is(LANDNAVN));
		assertThat(adresse.getPostnummer(), is(POSTNUMMER));
		assertThat(adresse.getPoststed(), is(TestDataUtil.POSTSTED));
	}

	@Test
	public void shouldMapWithUtenlandskPostAdresse() {
		var mottaker = MottakerTo.builder()
				.mottaker(createMottaker(false))
				.adresseKilde(ENHETPOSTADRESSE)
				.build();

		Adresse adresse = adresseMapper.map(mottaker);

		assertEquals(ENHETPOSTADRESSE, adresse.getAdresseKilde());
		assertThat(adresse.getAdresselinje1(), is(UTENLANDSK_ADRESSELINJE1));
		assertThat(adresse.getAdresselinje2(), is(UTENLANDSK_ADRESSELINJE2));
		assertThat(adresse.getAdresselinje3(), is(UTENLANDSK_ADRESSELINJE3));
		assertThat(adresse.getLandkode(), is(SVENSK_LANDKODE));
		assertThat(adresse.getLand(), is(SVERIGE));
		assertThat(adresse.getPostnummer(), nullValue());
		assertThat(adresse.getPoststed(), nullValue());
	}

	@Test
	public void shouldMapPDLWithNorskPostAdresse() {
		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(createPdlHentPerson(createPersonnavn()), SERVICE_CODE_TREG002, TEMA);

		Adresse adresse = adresseMapper.mapFraPdl(mottakerInfo);

		assertEquals(KONTAKTADRESSE, adresse.getAdresseKilde());
		assertEquals(FRITTFORMAT_ADRESSELINJE1, adresse.getAdresselinje1());
		assertEquals(FRITTFORMAT_ADRESSELINJE2, adresse.getAdresselinje2());
		assertNull(adresse.getAdresselinje3());
		assertEquals(LANDKODE_NORGE, adresse.getLandkode());
		assertEquals(FRITTFORMAT_POSTNUMMER, adresse.getPostnummer());
		assertEquals(PDLResponseUtil.POSTSTED, adresse.getPoststed());
	}

	@SneakyThrows
	@Test
	public void shouldMapPDLWithUtenlandskPostAdresse() {
		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(createPdlHentPersonUtenlandskAdresse(), SERVICE_CODE_TREG002, TEMA);

		Adresse adresse = adresseMapper.mapFraPdl(mottakerInfo);

		assertEquals(KONTAKTADRESSE, adresse.getAdresseKilde());
		assertEquals(POSTBOKSNUMMERNAVN, adresse.getAdresselinje1());
		assertEquals(POSTKODE_AND_BYSTED + ", Yorkshire", adresse.getAdresselinje2());
		assertNull(adresse.getPostnummer());
		assertNull(adresse.getPoststed());
		assertEquals(SVENSK_LANDKODE, adresse.getLandkode());
	}

	@Test
	public void shouldMapWhenLandkodeIsNull() {
		MottakerTo mottaker = createMottaker(ENHETFORRETNINGSADRESSE);
		NorskPostadresse norskPostadresse = createNorskPostadresse();
		norskPostadresse.setLand(null);
		mottaker.getMottaker().setMottakeradresse(norskPostadresse);

		Adresse adresse = adresseMapper.map(mottaker);

		assertEquals(ENHETFORRETNINGSADRESSE, adresse.getAdresseKilde());
		assertThat(adresse.getAdresselinje1(), is(ADRESSELINJE1));
		assertThat(adresse.getAdresselinje2(), is(ADRESSELINJE2));
		assertThat(adresse.getAdresselinje3(), is(ADRESSELINJE3));
		assertThat(adresse.getLandkode(), is("???"));
		assertThat(adresse.getPostnummer(), is(POSTNUMMER));
		assertThat(adresse.getPoststed(), is(TestDataUtil.POSTSTED));
	}

}