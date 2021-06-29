package no.nav.regoppslag.treg002;

import no.nav.regoppslag.api.HentMottakerOgAdresseResponse;
import no.nav.regoppslag.consumer.pdl.PdlGraphQLConsumer;
import no.nav.regoppslag.consumer.pdl.PdlMottakerInfo;
import no.nav.regoppslag.consumer.pdl.pdlresponse.MapPDLResponse;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.regoppslag.service.LandkodeService;
import no.nav.regoppslag.util.PDLResponseUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static no.nav.regoppslag.metrics.MetricLabels.SERVICE_CODE_TREG002;
import static no.nav.regoppslag.util.PDLResponseUtil.ADRESSENAVN_1;
import static no.nav.regoppslag.util.PDLResponseUtil.FRITTFORMAT_ADRESSELINJE1;
import static no.nav.regoppslag.util.PDLResponseUtil.FRITTFORMAT_ADRESSELINJE2;
import static no.nav.regoppslag.util.PDLResponseUtil.FRITTFORMAT_POSTNUMMER;
import static no.nav.regoppslag.util.PDLResponseUtil.LANDKODE_NORGE;
import static no.nav.regoppslag.util.PDLResponseUtil.LANDKODE_UTENLANDSK;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTBOKSNUMMERNAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTKODE;
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

	@Mock
	private PdlGraphQLConsumer pdlGraphQLConsumer;

	@Mock
	private MicrometerMetrics metrics;

	@InjectMocks
	private AdresseMapper adresseMapper;

	@BeforeEach
	public void setUp() {

	}

	@Test
	public void shouldMapWithNorskPostAdresse() {
		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(PDLResponseUtil.createPdlHentPerson(), SERVICE_CODE_TREG002);

		HentMottakerOgAdresseResponse.Adresse adresse = adresseMapper.mapFraPdl(mottakerInfo);
		assertEquals(adresse.getAdresselinje1(), FRITTFORMAT_ADRESSELINJE1);
		assertEquals(adresse.getAdresselinje2(), FRITTFORMAT_ADRESSELINJE2);
		assertNull(adresse.getAdresselinje3());
		assertEquals(adresse.getLandkode(), "NO");
		assertEquals(adresse.getPostnummer(), FRITTFORMAT_POSTNUMMER);
		assertEquals(adresse.getPoststed(), PDLResponseUtil.POSTSTED);
	}

	@Test
	public void shouldMapWithUtenlandskPostAdresse() {
		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(createPdlHentPersonUtelandsk(), SERVICE_CODE_TREG002);
		HentMottakerOgAdresseResponse.Adresse adresse = adresseMapper.mapFraPdl(mottakerInfo);

		assertEquals(adresse.getAdresselinje1(), POSTBOKSNUMMERNAVN);
		assertEquals(adresse.getAdresselinje2(), POSTKODE);
		assertEquals(adresse.getAdresselinje3(), PDLResponseUtil.BYSTED);
		assertNull(adresse.getPostnummer());
		assertNull(adresse.getPoststed());
		assertEquals(adresse.getLandkode(), LANDKODE_UTENLANDSK);

	}

	@Test
	public void shouldMapWhenLandkodeIsNull() {
		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(PDLResponseUtil.createPdlHentPersonWithVegadresse(), SERVICE_CODE_TREG002);
		HentMottakerOgAdresseResponse.Adresse adresse = adresseMapper.mapFraPdl(mottakerInfo);

		assertEquals(adresse.getAdresselinje1(), ADRESSENAVN_1);
		assertNull(adresse.getAdresselinje2());
		assertNull(adresse.getAdresselinje3());
		assertEquals(adresse.getPostnummer(), PDLResponseUtil.POSTNUMMER);
		assertEquals(adresse.getPoststed(), PDLResponseUtil.POSTSTED);
		assertEquals(adresse.getLandkode(), LANDKODE_NORGE);
	}


}