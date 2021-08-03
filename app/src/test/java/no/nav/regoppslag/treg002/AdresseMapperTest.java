package no.nav.regoppslag.treg002;

import lombok.SneakyThrows;
import no.nav.regoppslag.api.HentMottakerOgAdresseResponse;
import no.nav.regoppslag.consumer.pdl.PdlGraphQLConsumer;
import no.nav.regoppslag.consumer.pdl.PdlMottakerInfo;
import no.nav.regoppslag.consumer.pdl.map.MapPDLResponse;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.regoppslag.service.LandkodeService;
import no.nav.regoppslag.service.PostnummerService;
import no.nav.regoppslag.util.PDLResponseUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static no.nav.regoppslag.metrics.MetricLabels.SERVICE_CODE_TREG002;
import static no.nav.regoppslag.util.PDLResponseUtil.ADRESSENAVN_1;
import static no.nav.regoppslag.util.PDLResponseUtil.BYSTED;
import static no.nav.regoppslag.util.PDLResponseUtil.FRITTFORMAT_ADRESSELINJE1;
import static no.nav.regoppslag.util.PDLResponseUtil.FRITTFORMAT_ADRESSELINJE2;
import static no.nav.regoppslag.util.PDLResponseUtil.FRITTFORMAT_POSTNUMMER;
import static no.nav.regoppslag.util.PDLResponseUtil.LANDKODE_NORGE;
import static no.nav.regoppslag.util.PDLResponseUtil.LANDKODE_UTENLANDSK;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTBOKSNUMMERNAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTKODE;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTNUMMER;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTSTED;
import static no.nav.regoppslag.util.PDLResponseUtil.createPdlHentPersonUtelandsk;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@ExtendWith(MockitoExtension.class)
public class AdresseMapperTest {

	@Mock
	private LandkodeService landkodeService;

	@InjectMocks
	private MapPDLResponse mapPDLResponse;

	@InjectMocks
	private PostnummerService postnummerService;

	@Mock
	private PdlGraphQLConsumer pdlGraphQLConsumer;

	@Mock
	private MicrometerMetrics metrics;

	@InjectMocks
	private AdresseMapper adresseMapper;

	@BeforeEach
	public void setUp() {
		mapPDLResponse = new MapPDLResponse(postnummerService);
	}

	@SneakyThrows
	@Test
	public void shouldMapWithNorskPostAdresse() {
		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(PDLResponseUtil.createPdlHentPerson(), SERVICE_CODE_TREG002);

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
	public void shouldMapWithUtenlandskPostAdresse() {
		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(createPdlHentPersonUtelandsk(), SERVICE_CODE_TREG002);
		HentMottakerOgAdresseResponse.Adresse adresse = adresseMapper.mapFraPdl(mottakerInfo);

		assertEquals(POSTBOKSNUMMERNAVN, adresse.getAdresselinje1());
		assertEquals(POSTKODE, adresse.getAdresselinje2());
		assertEquals(BYSTED, adresse.getAdresselinje3());
		assertNull(adresse.getPostnummer());
		assertNull(adresse.getPoststed());
		assertEquals(LANDKODE_UTENLANDSK, adresse.getLandkode());

	}

	@SneakyThrows
	@Test
	public void shouldMapWhenLandkodeIsNull() {
		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(PDLResponseUtil.createPdlHentPersonWithVegadresse(), SERVICE_CODE_TREG002);
		HentMottakerOgAdresseResponse.Adresse adresse = adresseMapper.mapFraPdl(mottakerInfo);

		assertEquals(ADRESSENAVN_1, adresse.getAdresselinje1());
		assertNull(adresse.getAdresselinje2());
		assertNull(adresse.getAdresselinje3());
		assertEquals(POSTNUMMER, adresse.getPostnummer());
		assertEquals(POSTSTED, adresse.getPoststed());
		assertEquals(LANDKODE_NORGE, adresse.getLandkode());
	}


}