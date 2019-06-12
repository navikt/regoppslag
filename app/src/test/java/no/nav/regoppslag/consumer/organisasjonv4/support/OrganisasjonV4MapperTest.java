package no.nav.regoppslag.consumer.organisasjonv4.support;

import static no.nav.regoppslag.metrics.PrometheusLabels.ORGANISASJONV4_MAPPER;
import static no.nav.regoppslag.metrics.PrometheusLabels.UKJENT_POSTNUMMER;
import static no.nav.regoppslag.metrics.PrometheusMetrics.getConsumerId;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestCounter;
import static no.nav.regoppslag.util.TestDataUtil.GATENAVN;
import static no.nav.regoppslag.util.TestDataUtil.HUSBOKSTAV;
import static no.nav.regoppslag.util.TestDataUtil.HUSNR;
import static no.nav.regoppslag.util.TestDataUtil.POSTNR;
import static no.nav.regoppslag.util.TestDataUtil.SEMIADR1;
import static no.nav.regoppslag.util.TestDataUtil.SEMIADR2;
import static no.nav.regoppslag.util.TestDataUtil.SEMIADR3;
import static no.nav.regoppslag.util.TestDataUtil.createOrganisasjon;
import static no.nav.regoppslag.util.TestDataUtil.dateToGregorian;
import static no.nav.regoppslag.util.TestDataUtil.settKunForretningsadresse;
import static no.nav.regoppslag.util.TestDataUtil.settSemistrukturertAdresse;
import static no.nav.regoppslag.util.TestDataUtil.settStrukturertAdresse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.service.LandkodeService;
import no.nav.regoppslag.service.PostnummerService;
import no.nav.regoppslag.treg001.to.MottakerTo;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Gateadresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Landkoder;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Postnummer;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.StedsadresseNorge;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

public class OrganisasjonV4MapperTest {

	private static final long VALID_SECONDS = 10000;

	private PostnummerService postnummerService = new PostnummerService();
	private LandkodeService landkodeService = new LandkodeService();
	private OrganisasjonV4Mapper mapper;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void initPostnummer() throws Exception {
		postnummerService.init();
		mapper = new OrganisasjonV4Mapper(postnummerService, landkodeService);
	}

	private static final String ORGNAVN = "Orgnavn 1";
	private static final String ORGID = "Orgnavn 1";
	private static final String ORGNAVN_2 = "Orgnavn_2";
	private static final String ORGKORTNAVN = "OrgKortnavn 1";
	private static final String ORGKORTNAVN_2 = "OrgKortnavn_2";
	private static final String POSTSTED = "HUSNES";
	private static final String LAND = "Norge";
	private static final String SERVICECODE = "SERVICECODE";

	@Test
	public void shouldMapSakspartnavn() throws Exception {
		Organisasjon org = createOrganisasjon(Arrays.asList(ORGNAVN, ORGNAVN_2), Arrays.asList(ORGKORTNAVN, ORGKORTNAVN_2));
		String navn = mapper.getSakspartNavn(org);
		assertThat(navn, is(ORGNAVN + " " + ORGNAVN_2));
	}

	@Test
	public void shouldThrowWhenMissingAdresse() throws Exception {
		thrown.expect(RegOppslagFunctionalException.class);
		thrown.expectMessage("Ingen gyldige adresser funnet");

		Organisasjon org = createOrganisasjon(Arrays.asList(ORGNAVN, ORGNAVN_2), Arrays.asList(ORGKORTNAVN, ORGKORTNAVN_2));
		MottakerTo mottaker = mapper.map(ORGID, org, SERVICECODE);
		assertThat(mottaker.getMottaker().getKortNavn(), is(ORGKORTNAVN + " " + ORGKORTNAVN_2));
		assertThat(mottaker.getMottaker().getNavn(), is(ORGNAVN + " " + ORGNAVN_2));
	}

	@Test
	public void shouldThrowWhenExpiredAdresse() throws Exception {
		thrown.expect(RegOppslagFunctionalException.class);
		thrown.expectMessage("Ingen gyldige adresser funnet");

		Organisasjon org = createOrganisasjon(Arrays.asList(ORGNAVN, ORGNAVN_2), Arrays.asList(ORGKORTNAVN, ORGKORTNAVN_2));
		settSemistrukturertAdresse(org, "POSTADRESSE", -20000);
		MottakerTo mottaker = mapper.map(ORGID, org, SERVICECODE);
		assertThat(mottaker.getMottaker().getKortNavn(), is(ORGKORTNAVN + " " + ORGKORTNAVN_2));
		assertThat(mottaker.getMottaker().getNavn(), is(ORGNAVN + " " + ORGNAVN_2));
	}

	@Test
	public void shouldThrowWhenMissingPoststed() throws Exception {
		thrown.expect(RegOppslagFunctionalException.class);
		thrown.expectMessage("Ingen gyldige adresser funnet");

		Organisasjon org = createOrganisasjon(Arrays.asList(ORGNAVN, ORGNAVN_2), Arrays.asList(ORGKORTNAVN, ORGKORTNAVN_2));
		settStrukturertAdresse(org, "POSTADRESSE");
		((Gateadresse) org.getOrganisasjonDetaljer().getPostadresse().get(0)).setPoststed(null);
		mapper.map(ORGID, org, SERVICECODE);
	}

	@Test
	public void shouldThrowWhenOpphoertOrg() throws Exception {
		thrown.expect(RegOppslagFunctionalException.class);
		thrown.expectMessage("Organisasjon har opphørt");

		Organisasjon org = createOrganisasjon(Arrays.asList(ORGNAVN, ORGNAVN_2), Arrays.asList(ORGKORTNAVN, ORGKORTNAVN_2));
		org.getOrganisasjonDetaljer().setOpphoersdato(dateToGregorian(Date.from(Instant.now().minusSeconds(10000))));
		MottakerTo mottaker = mapper.map(ORGID, org, SERVICECODE);
		assertThat(mottaker.getMottaker().getKortNavn(), is(ORGKORTNAVN + " " + ORGKORTNAVN_2));
		assertThat(mottaker.getMottaker().getNavn(), is(ORGNAVN + " " + ORGNAVN_2));
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
		assertThat(requestCounter.labels("T", ORGANISASJONV4_MAPPER, UKJENT_POSTNUMMER, getConsumerId(), UKJENT_POSTNUMMER).get(), is(1.0));

		(org.getOrganisasjonDetaljer().getPostadresse().get(0)).setLandkode(createLandkode("SE"));
		mapper.map(ORGID, org, "T");
		assertThat(requestCounter.labels("T", ORGANISASJONV4_MAPPER, UKJENT_POSTNUMMER, getConsumerId(), UKJENT_POSTNUMMER).get(), is(1.0));
	}

	private Landkoder createLandkode(String landkode) {
		Landkoder landkoder = new Landkoder();
		landkoder.setValue(landkode);
		landkoder.setKodeRef(landkode);
		landkoder.setKodeverksRef(landkode);
		return landkoder;
	}

}
