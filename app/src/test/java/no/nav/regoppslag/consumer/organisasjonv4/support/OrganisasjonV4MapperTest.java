package no.nav.regoppslag.consumer.organisasjonv4.support;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.dok.brevdata.felles.v1.navfelles.UtenlandskPostadresse;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.regoppslag.service.LandkodeService;
import no.nav.regoppslag.service.PostnummerService;
import no.nav.regoppslag.treg001.to.MottakerTo;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Gateadresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Landkoder;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Maalformer;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Postnummer;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.StedsadresseNorge;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.xml.datatype.DatatypeConfigurationException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static no.nav.regoppslag.metrics.MetricLabels.ORGANISASJONV4_MAPPER;
import static no.nav.regoppslag.metrics.MetricLabels.UKJENT_POSTNUMMER;
import static no.nav.regoppslag.util.TestDataUtil.GATENAVN;
import static no.nav.regoppslag.util.TestDataUtil.HUSBOKSTAV;
import static no.nav.regoppslag.util.TestDataUtil.HUSNR;
import static no.nav.regoppslag.util.TestDataUtil.POSTNR;
import static no.nav.regoppslag.util.TestDataUtil.SEMIADR1;
import static no.nav.regoppslag.util.TestDataUtil.SEMIADR2;
import static no.nav.regoppslag.util.TestDataUtil.SEMIADR3;
import static no.nav.regoppslag.util.TestDataUtil.createOrganisasjon;
import static no.nav.regoppslag.util.TestDataUtil.createUtenlandsPostadresse;
import static no.nav.regoppslag.util.TestDataUtil.dateToGregorian;
import static no.nav.regoppslag.util.TestDataUtil.settKunForretningsadresse;
import static no.nav.regoppslag.util.TestDataUtil.settSemistrukturertAdresse;
import static no.nav.regoppslag.util.TestDataUtil.settStrukturertAdresse;
import static no.nav.regoppslag.util.TestDataUtil.settUtlandskPostadresse;
import static no.nav.regoppslag.util.TestDataUtil.settUtlandskPostadresseMedPoststed;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;


@ExtendWith(SpringExtension.class)
public class OrganisasjonV4MapperTest {

	private static final long VALID_SECONDS = 10000;

	private PostnummerService postnummerService;
	private LandkodeService landkodeService;
	private MeterRegistry registry;
	private MicrometerMetrics metrics;
	@InjectMocks
	private OrganisasjonV4Mapper mapper;


	@BeforeEach
	public void initTests() throws Exception {
		landkodeService = new LandkodeService();
		registry = new SimpleMeterRegistry();
		metrics = new MicrometerMetrics();
		postnummerService = new PostnummerService();
		ReflectionTestUtils.setField(metrics, "registry", registry);
		mapper = new OrganisasjonV4Mapper(postnummerService, landkodeService, metrics);
	}

	private static final String ORGNAVN = "Orgnavn 1";
	private static final String ORGID = "Orgnavn 1";
	private static final String ORGNAVN_2 = "Orgnavn_2";
	private static final String ORGKORTNAVN = "OrgKortnavn 1";
	private static final String ORGKORTNAVN_2 = "OrgKortnavn_2";
	private static final String POSTSTED = "HUSNES";
	private static final String LAND = "Norge";
	private static final String SVERIGE_LAND = "Sverige";
	private static final String SERVICECODE = "SERVICECODE";

	@Test
	public void shouldMapSpraakKode() throws DatatypeConfigurationException {
		Organisasjon org = createOrganisasjon(Arrays.asList(ORGNAVN, ORGNAVN_2), Arrays.asList(ORGKORTNAVN, ORGKORTNAVN_2));
		org.getOrganisasjonDetaljer().setGjeldendeMaalform(createSpraak("NO"));
		settSemistrukturertAdresse(org, "POSTADRESSE", VALID_SECONDS);
		MottakerTo mottakerTo = mapper.map(ORGID, org, SERVICECODE);

		assertThat(mottakerTo.getSpraakKode(), is("NO"));

		org.getOrganisasjonDetaljer().setGjeldendeMaalform(createSpraak("AA"));
		mottakerTo = mapper.map(ORGID, org, SERVICECODE);
		assertThat(mottakerTo.getSpraakKode(), is("AA"));
	}

	@Test
	public void shouldMapSakspartnavn() throws Exception {
		Organisasjon org = createOrganisasjon(Arrays.asList(ORGNAVN, ORGNAVN_2), Arrays.asList(ORGKORTNAVN, ORGKORTNAVN_2));
		String navn = mapper.getSakspartNavn(org);
		assertThat(navn, is(ORGNAVN + " " + ORGNAVN_2));
	}

	@Test
	public void shouldThrowWhenMissingAdresse() throws Exception {
		Organisasjon org = createOrganisasjon(Arrays.asList(ORGNAVN, ORGNAVN_2), Arrays.asList(ORGKORTNAVN, ORGKORTNAVN_2));
		RegOppslagFunctionalException e = Assertions.assertThrows(RegOppslagFunctionalException.class,
				() -> mapper.map(ORGID, org, SERVICECODE), "Organisasjon har opphørt");
		assertEquals(NOT_FOUND, e.getHttpStatus());
	}

	@Test
	public void shouldThrowWhenExpiredAdresse() throws Exception {
		Organisasjon org = createOrganisasjon(Arrays.asList(ORGNAVN, ORGNAVN_2), Arrays.asList(ORGKORTNAVN, ORGKORTNAVN_2));
		settSemistrukturertAdresse(org, "POSTADRESSE", -20000);
		RegOppslagFunctionalException e = Assertions.assertThrows(RegOppslagFunctionalException.class,
				() -> mapper.map(ORGID, org, SERVICECODE), "Organisasjon har opphørt");
		assertEquals(NOT_FOUND, e.getHttpStatus());
	}

	@Test
	public void shouldThrowWhenMissingPoststed() throws Exception {
		Organisasjon org = createOrganisasjon(Arrays.asList(ORGNAVN, ORGNAVN_2), Arrays.asList(ORGKORTNAVN, ORGKORTNAVN_2));
		settStrukturertAdresse(org, "POSTADRESSE");
		((Gateadresse) org.getOrganisasjonDetaljer().getPostadresse().get(0)).setPoststed(null);
		RegOppslagFunctionalException e = Assertions.assertThrows(RegOppslagFunctionalException.class,
				() -> mapper.map(ORGID, org, SERVICECODE), "Ingen gyldige adresser funnet");
		assertEquals(NOT_FOUND, e.getHttpStatus());
	}

	@Test
	public void shouldThrowWhenOpphoertOrg() throws Exception {

		Organisasjon org = createOrganisasjon(Arrays.asList(ORGNAVN, ORGNAVN_2), Arrays.asList(ORGKORTNAVN, ORGKORTNAVN_2));
		org.getOrganisasjonDetaljer().setOpphoersdato(dateToGregorian(Date.from(Instant.now().minusSeconds(10000))));

		RegOppslagFunctionalException e = Assertions.assertThrows(RegOppslagFunctionalException.class,
				() -> mapper.map(ORGID, org, SERVICECODE), "Organisasjon har opphørt");
		assertEquals(NOT_FOUND, e.getHttpStatus());

	}

	@Test
	public void mapOrganisasjonSemistrukturertPostadresse() throws Exception {
		Organisasjon org = createOrganisasjon(Arrays.asList(ORGNAVN, ORGNAVN_2), Arrays.asList(ORGKORTNAVN, ORGKORTNAVN_2));
		settSemistrukturertAdresse(org, "POSTADRESSE", VALID_SECONDS);
		MottakerTo mottakerTo = mapper.map(ORGID, org, SERVICECODE);
		assertThat(mottakerTo.getMottaker().getKortNavn(), is(ORGKORTNAVN + " " + ORGKORTNAVN_2));
		assertThat(mottakerTo.getMottaker().getNavn(), is(ORGNAVN + " " + ORGNAVN_2));

		Mottaker mottaker = mottakerTo.getMottaker();
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1()), is(SEMIADR1));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje2()), is(SEMIADR2));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje3()), is(SEMIADR3));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPostnummer()), is(POSTNR));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPoststed()), is(POSTSTED));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getLand()), is(LAND));
	}

	@Test
	public void mapOrganisasjonSemistrukturertForretningsadresse() throws Exception {
		Organisasjon org = createOrganisasjon(Arrays.asList(ORGNAVN, ORGNAVN_2), Arrays.asList(ORGKORTNAVN, ORGKORTNAVN_2));
		settSemistrukturertAdresse(org, "FORRETNINGSADRESSE", VALID_SECONDS);
		MottakerTo mottakerTo = mapper.map(ORGID, org, SERVICECODE);
		assertThat(mottakerTo.getMottaker().getKortNavn(), is(ORGKORTNAVN + " " + ORGKORTNAVN_2));
		assertThat(mottakerTo.getMottaker().getNavn(), is(ORGNAVN + " " + ORGNAVN_2));

		Mottaker mottaker = mottakerTo.getMottaker();
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1()), is(SEMIADR1));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje2()), is(SEMIADR2));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje3()), is(SEMIADR3));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPostnummer()), is(POSTNR));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPoststed()), is(POSTSTED));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getLand()), is(LAND));
	}

	@Test
	public void mapOrganisasjonStrukturertPostadresse() throws Exception {
		Organisasjon org = createOrganisasjon(Arrays.asList(ORGNAVN, ORGNAVN_2), Arrays.asList(ORGKORTNAVN, ORGKORTNAVN_2));
		settStrukturertAdresse(org, "POSTADRESSE");
		MottakerTo mottakerTo = mapper.map(ORGID, org, SERVICECODE);
		assertThat(mottakerTo.getMottaker().getKortNavn(), is(ORGKORTNAVN + " " + ORGKORTNAVN_2));
		assertThat(mottakerTo.getMottaker().getNavn(), is(ORGNAVN + " " + ORGNAVN_2));

		Mottaker mottaker = mottakerTo.getMottaker();
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1()), is(GATENAVN + " " + HUSNR + HUSBOKSTAV));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje2()), nullValue());
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje3()), nullValue());
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPostnummer()), is(POSTNR));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPoststed()), is(POSTSTED));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getLand()), is(LAND));
	}

	@Test
	public void mapOrganisasjonStrukturertForretningsadresse() throws Exception {
		Organisasjon org = createOrganisasjon(Arrays.asList(ORGNAVN, ORGNAVN_2), Arrays.asList(ORGKORTNAVN, ORGKORTNAVN_2));
		settStrukturertAdresse(org, "FORRETNINGSADRESSE");
		MottakerTo mottakerTo = mapper.map(ORGID, org, SERVICECODE);
		assertThat(mottakerTo.getMottaker().getKortNavn(), is(ORGKORTNAVN + " " + ORGKORTNAVN_2));
		assertThat(mottakerTo.getMottaker().getNavn(), is(ORGNAVN + " " + ORGNAVN_2));

		Mottaker mottaker = mottakerTo.getMottaker();
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1()), is(GATENAVN + " " + HUSNR + HUSBOKSTAV));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje2()), nullValue());
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje3()), nullValue());
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPostnummer()), is(POSTNR));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPoststed()), is(POSTSTED));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getLand()), is(LAND));
	}

	@Test
	public void shouldMapOrganisasjonEmptyPostAdresse() throws Exception {
		Organisasjon org = createOrganisasjon(Collections.singletonList(ORGNAVN), Collections.singletonList(ORGKORTNAVN));
		settKunForretningsadresse(org);
		MottakerTo mottakerTo = mapper.map(ORGID, org, SERVICECODE);
		assertThat(mottakerTo.getMottaker().getKortNavn(), is(ORGKORTNAVN));
		assertThat(mottakerTo.getMottaker().getNavn(), is(ORGNAVN));

		Mottaker mottaker = mottakerTo.getMottaker();
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1()), is(GATENAVN + " " + HUSNR + HUSBOKSTAV));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje2()), nullValue());
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje3()), nullValue());
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPostnummer()), is(POSTNR));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPoststed()), is(POSTSTED));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getLand()), is(LAND));
	}

	@Test
	public void shouldMapSemistrukturertAdresseWhenLandskodeNotNO() throws DatatypeConfigurationException, RegOppslagFunctionalException {
		UtenlandskPostadresse expectedAdresse = createUtenlandsPostadresse();
		expectedAdresse.setLand("Sweden");

		Organisasjon org = createOrganisasjon(Collections.singletonList(ORGNAVN), Collections.singletonList(ORGKORTNAVN));
		settUtlandskPostadresse(org);
		MottakerTo mottaker = mapper.map(ORGID, org, SERVICECODE);

		assertThat(mottaker.getMottaker().getMottakeradresse(), instanceOf(UtenlandskPostadresse.class));
		UtenlandskPostadresse actualAdresse = (UtenlandskPostadresse) mottaker.getMottaker().getMottakeradresse();
		assertThat(expectedAdresse.getAdresselinje1(), equalTo(actualAdresse.getAdresselinje1()));
		assertThat(expectedAdresse.getAdresselinje2(), equalTo(actualAdresse.getAdresselinje2()));
		assertThat(expectedAdresse.getAdresselinje3(), equalTo(actualAdresse.getAdresselinje3()));
		assertThat(expectedAdresse.getLand(), equalTo(actualAdresse.getLand()));
	}

	@Test
	public void shouldMapSemistrukturertAdresseWithAdresseledPoststedWhenLandskodeNotNO() throws DatatypeConfigurationException, RegOppslagFunctionalException {
		UtenlandskPostadresse expectedAdresse = createUtenlandsPostadresse();

		Organisasjon org = createOrganisasjon(Collections.singletonList(ORGNAVN), Collections.singletonList(ORGKORTNAVN));
		settUtlandskPostadresseMedPoststed(org);
		MottakerTo mottaker = mapper.map(ORGID, org, SERVICECODE);

		assertThat(mottaker.getMottaker().getMottakeradresse(), instanceOf(UtenlandskPostadresse.class));
		UtenlandskPostadresse actualAdresse = (UtenlandskPostadresse) mottaker.getMottaker().getMottakeradresse();
		assertThat(expectedAdresse.getAdresselinje1(), equalTo(actualAdresse.getAdresselinje1()));
		assertThat(expectedAdresse.getAdresselinje2(), equalTo(actualAdresse.getAdresselinje2()));
	}

	@Test
	public void mapPersonPostadresseUtenPostnr() throws Exception {
		Organisasjon org = createOrganisasjon(Arrays.asList(ORGNAVN, ORGNAVN_2), Arrays.asList(ORGKORTNAVN, ORGKORTNAVN_2));
		settStrukturertAdresse(org, "POSTADRESSE");
		((StedsadresseNorge) org.getOrganisasjonDetaljer().getPostadresse().get(0)).setPoststed(new Postnummer());
		MottakerTo mottakerTo = mapper.map(ORGID, org, SERVICECODE);

		Mottaker mottaker = mottakerTo.getMottaker();
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPostnummer()), nullValue());
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPoststed()), nullValue());
	}

	@Test
	public void testFunctionalMetrics() throws Exception {
		Organisasjon org = createOrganisasjon(Arrays.asList(ORGNAVN, ORGNAVN_2), Arrays.asList(ORGKORTNAVN, ORGKORTNAVN_2));
		settStrukturertAdresse(org, "POSTADRESSE");
		((StedsadresseNorge) org.getOrganisasjonDetaljer().getPostadresse().get(0)).setPoststed(new Postnummer());

		mapper.map(ORGID, org, "T");
		assertThat(metrics.countEvents("T", ORGANISASJONV4_MAPPER, UKJENT_POSTNUMMER), is(1.0));

		(org.getOrganisasjonDetaljer().getPostadresse().get(0)).setLandkode(createLandkode("SE"));
		mapper.map(ORGID, org, "T");
		assertThat(metrics.countEvents("T", ORGANISASJONV4_MAPPER, UKJENT_POSTNUMMER), is(1.0));
	}

	private Landkoder createLandkode(String landkode) {
		Landkoder landkoder = new Landkoder();
		landkoder.setValue(landkode);
		landkoder.setKodeRef(landkode);
		landkoder.setKodeverksRef(landkode);
		return landkoder;
	}

	private Maalformer createSpraak(String spraakKode) {
		Maalformer spraak = new Maalformer();
		spraak.setKodeRef(spraakKode);
		return spraak;
	}
}
