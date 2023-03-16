package no.nav.regoppslag.treg001.support;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.dok.brevdata.felles.v1.navfelles.UtenlandskPostadresse;
import no.nav.regoppslag.consumer.ereg.support.Organisasjon;
import no.nav.regoppslag.consumer.ereg.support.OrganisasjonEregMapper;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.regoppslag.service.LandkodeService;
import no.nav.regoppslag.service.PostnummerService;
import no.nav.regoppslag.consumer.ereg.MottakerTo;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.xml.datatype.DatatypeConfigurationException;
import java.time.LocalDate;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static no.nav.regoppslag.util.TestDataUtil.ADRESSELINJE1;
import static no.nav.regoppslag.util.TestDataUtil.ADRESSELINJE2;
import static no.nav.regoppslag.util.TestDataUtil.ADRESSELINJE3;
import static no.nav.regoppslag.util.TestDataUtil.GATENAVN;
import static no.nav.regoppslag.util.TestDataUtil.HUSBOKSTAV;
import static no.nav.regoppslag.util.TestDataUtil.HUSNR;
import static no.nav.regoppslag.util.TestDataUtil.POSTNR;
import static no.nav.regoppslag.util.TestDataUtil.SVENSK_LAND;
import static no.nav.regoppslag.util.TestDataUtil.UTENLANDSK_ADRESSELINJE1;
import static no.nav.regoppslag.util.TestDataUtil.UTENLANDSK_ADRESSELINJE2;
import static no.nav.regoppslag.util.TestDataUtil.UTENLANDSK_ADRESSELINJE3;
import static no.nav.regoppslag.util.TestDataUtil.createOrganisasjon;
import static no.nav.regoppslag.util.TestDataUtil.createUtenlandsPostadresse;
import static no.nav.regoppslag.util.TestDataUtil.settKunForretningsadresse;
import static no.nav.regoppslag.util.TestDataUtil.settPostAdresse;
import static no.nav.regoppslag.util.TestDataUtil.settUtlandskPostadresse;
import static no.nav.regoppslag.util.TestDataUtil.settUtlandskPostadresseMedAlleAdresselinjerOgPoststed;
import static no.nav.regoppslag.util.TestDataUtil.settUtlandskPostadresseMedPoststed;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(SpringExtension.class)
public class OrganisasjonEregMapperTest {
	private static final long VALID_SECONDS = 10000;

	private static final String ORGNAVN = "Orgnavn 1";
	private static final String ORGID = "Orgnavn 1";
	private static final String ORGNAVN_2 = "Orgnavn_2";
	private static final String ORGKORTNAVN = "OrgKortnavn 1";
	private static final String ORGKORTNAVN_2 = "OrgKortnavn_2";
	private static final String POSTSTED = "HUSNES";
	private static final String LAND = "Norge";
	private static final String SERVICECODE = "SERVICECODE";

	private OrganisasjonEregMapper mapper;

	@BeforeEach
	public void initTests() throws Exception {
		LandkodeService landkodeService = new LandkodeService();
		MeterRegistry registry = new SimpleMeterRegistry();
		MicrometerMetrics metrics = new MicrometerMetrics();
		PostnummerService postnummerService = new PostnummerService();
		ReflectionTestUtils.setField(metrics, "registry", registry);
		mapper = new OrganisasjonEregMapper(postnummerService, landkodeService, metrics);
	}

	@Test
	public void shouldMapSpraakKode() {
		Organisasjon org = createOrganisasjon(asList(ORGNAVN, ORGNAVN_2), asList(ORGKORTNAVN, ORGKORTNAVN_2));
		org.getOrganisasjonDetaljer().setMaalform("NO");
		settPostAdresse(org, "POSTADRESSE", 20000L);
		MottakerTo mottakerTo = mapper.map(ORGID, org, SERVICECODE);

		assertThat(mottakerTo.getSpraakKode(), is("NO"));

		org.getOrganisasjonDetaljer().setMaalform("AA");
		mottakerTo = mapper.map(ORGID, org, SERVICECODE);
		assertThat(mottakerTo.getSpraakKode(), is("AA"));
	}

	@Test
	public void shouldMapSakspartnavn() {
		Organisasjon org = createOrganisasjon(asList(ORGNAVN, ORGNAVN_2), asList(ORGKORTNAVN, ORGKORTNAVN_2));
		String navn = mapper.getSakspartNavn(org);
		assertThat(navn, is(ORGNAVN + " " + ORGNAVN_2));
	}

	@Test
	public void shouldThrowWhenMissingAdresse() {
		Organisasjon org = createOrganisasjon(asList(ORGNAVN, ORGNAVN_2), asList(ORGKORTNAVN, ORGKORTNAVN_2));
		RegOppslagFunctionalException e = assertThrows(RegOppslagFunctionalException.class,
				() -> mapper.map(ORGID, org, SERVICECODE), "Organisasjon har opphørt");
		assertEquals(NOT_FOUND, e.getHttpStatus());
	}

	@Test
	public void shouldThrowWhenExpiredAdresse() {
		Organisasjon org = createOrganisasjon(asList(ORGNAVN, ORGNAVN_2), asList(ORGKORTNAVN, ORGKORTNAVN_2));
		settPostAdresse(org, "POSTADRESSE", -20000L);
		RegOppslagFunctionalException e = assertThrows(RegOppslagFunctionalException.class,
				() -> mapper.map(ORGID, org, SERVICECODE), "Organisasjon har opphørt");
		assertEquals(NOT_FOUND, e.getHttpStatus());
	}

	@Test
	public void shouldThrowWhenMissingPoststed() {
		Organisasjon org = createOrganisasjon(asList(ORGNAVN, ORGNAVN_2), asList(ORGKORTNAVN, ORGKORTNAVN_2));
		settPostAdresse(org, "POSTADRESSE", 10000L);
		org.getOrganisasjonDetaljer().getPostadresser().get(0).setPoststed(null);
		org.getOrganisasjonDetaljer().getPostadresser().get(0).setPostnummer(null);
		RegOppslagFunctionalException e = assertThrows(RegOppslagFunctionalException.class,
				() -> mapper.map(ORGID, org, SERVICECODE), "Ingen gyldige adresser funnet");
		assertEquals(NOT_FOUND, e.getHttpStatus());
	}

	@Test
	public void shouldThrowWhenOpphoertOrg() {

		Organisasjon org = createOrganisasjon(asList(ORGNAVN, ORGNAVN_2), asList(ORGKORTNAVN, ORGKORTNAVN_2));
		org.getOrganisasjonDetaljer().setOpphoersdato(LocalDate.now().minusDays(1));

		RegOppslagFunctionalException e = assertThrows(RegOppslagFunctionalException.class,
				() -> mapper.map(ORGID, org, SERVICECODE), "Organisasjon har opphørt");
		assertEquals(NOT_FOUND, e.getHttpStatus());
	}

	@Test
	public void mapOrganisasjonSemistrukturertPostadresse() {
		Organisasjon org = createOrganisasjon(asList(ORGNAVN, ORGNAVN_2), asList(ORGKORTNAVN, ORGKORTNAVN_2));
		settPostAdresse(org, "POSTADRESSE", VALID_SECONDS);
		MottakerTo mottakerTo = mapper.map(ORGID, org, SERVICECODE);
		assertThat(mottakerTo.getMottaker().getKortNavn(), is(ORGKORTNAVN + " " + ORGKORTNAVN_2));
		assertThat(mottakerTo.getMottaker().getNavn(), is(ORGNAVN + " " + ORGNAVN_2));

		Mottaker mottaker = mottakerTo.getMottaker();
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1()), Matchers.is(ADRESSELINJE1));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje2()), Matchers.is(ADRESSELINJE2));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje3()), Matchers.is(ADRESSELINJE3));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPostnummer()), Matchers.is(POSTNR));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPoststed()), is(POSTSTED));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getLand()), is(LAND));
	}

	@Test
	public void shouldMapAdresseWhenLandskodeNotNOAndAlleAdresselinjerSatt() throws RegOppslagFunctionalException {
		Organisasjon org = createOrganisasjon(singletonList(ORGNAVN), singletonList(ORGKORTNAVN));
		settUtlandskPostadresseMedAlleAdresselinjerOgPoststed(org);

		MottakerTo mottaker = mapper.map(ORGID, org, SERVICECODE);
		assertThat(mottaker.getMottaker().getMottakeradresse(), instanceOf(UtenlandskPostadresse.class));

		UtenlandskPostadresse actualAdresse = (UtenlandskPostadresse) mottaker.getMottaker().getMottakeradresse();
		assertEquals(UTENLANDSK_ADRESSELINJE1, actualAdresse.getAdresselinje1());
		assertEquals(UTENLANDSK_ADRESSELINJE2 + ", " + UTENLANDSK_ADRESSELINJE3, actualAdresse.getAdresselinje2());
		assertEquals(POSTSTED, actualAdresse.getAdresselinje3());
		assertEquals(SVENSK_LAND, actualAdresse.getLand());
	}

	@Test
	public void mapOrganisasjonSemistrukturertForretningsadresse() {
		Organisasjon org = createOrganisasjon(asList(ORGNAVN, ORGNAVN_2), asList(ORGKORTNAVN, ORGKORTNAVN_2));
		settPostAdresse(org, "FORRETNINGSADRESSE", VALID_SECONDS);
		MottakerTo mottakerTo = mapper.map(ORGID, org, SERVICECODE);
		assertThat(mottakerTo.getMottaker().getKortNavn(), is(ORGKORTNAVN + " " + ORGKORTNAVN_2));
		assertThat(mottakerTo.getMottaker().getNavn(), is(ORGNAVN + " " + ORGNAVN_2));

		Mottaker mottaker = mottakerTo.getMottaker();
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1()), Matchers.is(ADRESSELINJE1));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje2()), Matchers.is(ADRESSELINJE2));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje3()), Matchers.is(ADRESSELINJE3));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPostnummer()), Matchers.is(POSTNR));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPoststed()), is(POSTSTED));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getLand()), is(LAND));
	}

	@Test
	public void mapOrganisasjonStrukturertPostadresse() {
		Organisasjon org = createOrganisasjon(asList(ORGNAVN, ORGNAVN_2), asList(ORGKORTNAVN, ORGKORTNAVN_2));
		settPostAdresse(org, "POSTADRESSE", VALID_SECONDS);
		MottakerTo mottakerTo = mapper.map(ORGID, org, SERVICECODE);
		assertThat(mottakerTo.getMottaker().getKortNavn(), is(ORGKORTNAVN + " " + ORGKORTNAVN_2));
		assertThat(mottakerTo.getMottaker().getNavn(), is(ORGNAVN + " " + ORGNAVN_2));

		Mottaker mottaker = mottakerTo.getMottaker();
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1()), is(GATENAVN + " " + HUSNR + HUSBOKSTAV));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje2()), nullValue());
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje3()), nullValue());
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPostnummer()), Matchers.is(POSTNR));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPoststed()), is(POSTSTED));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getLand()), is(LAND));
	}

	@Test
	public void mapOrganisasjonStrukturertForretningsadresse() {
		Organisasjon org = createOrganisasjon(asList(ORGNAVN, ORGNAVN_2), asList(ORGKORTNAVN, ORGKORTNAVN_2));
		settPostAdresse(org, "FORRETNINGSADRESSE", VALID_SECONDS);
		MottakerTo mottakerTo = mapper.map(ORGID, org, SERVICECODE);
		assertThat(mottakerTo.getMottaker().getKortNavn(), is(ORGKORTNAVN + " " + ORGKORTNAVN_2));
		assertThat(mottakerTo.getMottaker().getNavn(), is(ORGNAVN + " " + ORGNAVN_2));

		Mottaker mottaker = mottakerTo.getMottaker();
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1()), is(GATENAVN + " " + HUSNR + HUSBOKSTAV));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje2()), nullValue());
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje3()), nullValue());
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPostnummer()), Matchers.is(POSTNR));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPoststed()), is(POSTSTED));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getLand()), is(LAND));
	}

	@Test
	public void shouldMapOrganisasjonEmptyPostAdresse() {
		Organisasjon org = createOrganisasjon(singletonList(ORGNAVN), singletonList(ORGKORTNAVN));
		settKunForretningsadresse(org);
		MottakerTo mottakerTo = mapper.map(ORGID, org, SERVICECODE);
		assertThat(mottakerTo.getMottaker().getKortNavn(), is(ORGKORTNAVN));
		assertThat(mottakerTo.getMottaker().getNavn(), is(ORGNAVN));

		Mottaker mottaker = mottakerTo.getMottaker();
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1()), is(GATENAVN + " " + HUSNR + HUSBOKSTAV));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje2()), nullValue());
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje3()), nullValue());
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPostnummer()), Matchers.is(POSTNR));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPoststed()), is(POSTSTED));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getLand()), is(LAND));
	}

	@Test
	public void shouldMapSemistrukturertAdresseWhenLandskodeNotNO() throws RegOppslagFunctionalException {
		UtenlandskPostadresse expectedAdresse = createUtenlandsPostadresse();

		Organisasjon org = createOrganisasjon(singletonList(ORGNAVN), singletonList(ORGKORTNAVN));
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
	public void shouldMapSemistrukturertAdresseWithAdresseledPoststedWhenLandskodeNotNO() throws RegOppslagFunctionalException {
		UtenlandskPostadresse expectedAdresse = createUtenlandsPostadresse();

		Organisasjon org = createOrganisasjon(singletonList(ORGNAVN), singletonList(ORGKORTNAVN));
		settUtlandskPostadresseMedPoststed(org);
		MottakerTo mottaker = mapper.map(ORGID, org, SERVICECODE);

		assertThat(mottaker.getMottaker().getMottakeradresse(), instanceOf(UtenlandskPostadresse.class));
		UtenlandskPostadresse actualAdresse = (UtenlandskPostadresse) mottaker.getMottaker().getMottakeradresse();
		assertThat(expectedAdresse.getAdresselinje1(), equalTo(actualAdresse.getAdresselinje1()));
		assertThat(expectedAdresse.getAdresselinje2(), equalTo(actualAdresse.getAdresselinje2()));
	}

	@Test
	public void mapPersonPostadresseUtenPostnr() {
		Organisasjon org = createOrganisasjon(asList(ORGNAVN, ORGNAVN_2), asList(ORGKORTNAVN, ORGKORTNAVN_2));
		settPostAdresse(org, "POSTADRESSE", VALID_SECONDS);
		org.getOrganisasjonDetaljer().getPostadresser().get(0).setPostnummer(null);
		MottakerTo mottakerTo = mapper.map(ORGID, org, SERVICECODE);

		Mottaker mottaker = mottakerTo.getMottaker();
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPostnummer()), nullValue());
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPoststed()), nullValue());
	}
}
