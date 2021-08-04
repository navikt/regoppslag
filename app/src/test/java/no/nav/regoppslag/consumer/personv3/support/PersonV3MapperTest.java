package no.nav.regoppslag.consumer.personv3.support;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.dok.brevdata.felles.v1.navfelles.Person;
import no.nav.dok.brevdata.felles.v1.navfelles.UtenlandskPostadresse;
import no.nav.dok.brevdata.felles.v1.simpletypes.AktoerType;
import no.nav.dok.brevdata.felles.v1.simpletypes.Spraakkode;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.UkjentAdresseException;
import no.nav.regoppslag.exceptions.UkjentAdressePersonErDoed;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.regoppslag.service.LandkodeService;
import no.nav.regoppslag.service.PostnummerService;
import no.nav.regoppslag.treg001.to.MottakerTo;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bostedsadresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Doedsdato;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Gateadresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Landkoder;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Matrikkeladresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.MidlertidigPostadresseNorge;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.MidlertidigPostadresseUtland;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personnavn;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Postadresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Postadressetyper;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PostboksadresseNorsk;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Postnummer;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Spraak;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.UstrukturertAdresse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.GregorianCalendar;

import static no.nav.regoppslag.metrics.MetricLabels.PERSONV3_MAPPER;
import static no.nav.regoppslag.metrics.MetricLabels.UKJENT_LAND;
import static no.nav.regoppslag.metrics.MetricLabels.UKJENT_POSTNUMMER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;


public class PersonV3MapperTest {
	private static final String FNR = "99999999999";
	private static final String FORNAVN = "TOM";
	private static final String MELLOMNAVN = "MARVOLO";
	private static final String ETTERNAVN = "RIDDLE";
	private static final String GATENAVN = "Gatenavn";
	private static final int HUSNR = 13;
	private static final String HUSBOKSTAV = "X";
	private static final String EIENDOMSNAVN = "Min eiendom";
	private static final String POSTBOKS = "123";
	private static final String NORSK_ADRESSELINJE1 = "Norsk adresse 1";
	private static final String NORSK_ADRESSELINJE2 = "Norsk adresse 2";
	private static final String NORSK_ADRESSELINJE3 = "Norsk adresse 3";
	private static final String UTLAND_ADRESSELINJE1 = "Foreign address 1";
	private static final String UTLAND_ADRESSELINJE2 = "Foreign address 2";
	private static final String UTLAND_ADRESSELINJE3 = "Foreign address 3";
	private static final String UTLAND_ADRESSELINJE4 = "Foreign address 4";
	private static final String POSTNR = "5460";
	private static final String POSTSTED = "HUSNES";
	private static final String LANDKODE = "NOR";
	private static final String LANDKODE_UTLAND = "DNK";
	private static final String LAND = "Norge";
	private static final String LAND_UTLAND = "Denmark";
	private static final String TILLEGGSADRESSETYPE_CO = "C/O";
	private static final String TILLEGGSADRESSETYPE_V = "V/";
	private static final String TILLEGGSADRESSE = "Tilleggsadresse";
	private static final String CO_TILLEGGSADRESSE = "C/O someAdress";
	private static final String V_TILLEGGSADRESSE = "V/ someAdress";
	private static final LocalDateTime DOEDSDATO = LocalDateTime.parse("2020-03-03T10:15:30.000000");

	private PostnummerService postnummerService = new PostnummerService();
	private LandkodeService landkodeService = new LandkodeService();
	private MeterRegistry registry = new SimpleMeterRegistry();
	private MicrometerMetrics metrics = new MicrometerMetrics();

	private PersonV3Mapper mapper;

	@BeforeEach
	public void initPostnummer() throws Exception {
		postnummerService.init();
		ReflectionTestUtils.setField(metrics, "registry", registry);
		mapper = new PersonV3Mapper(postnummerService, landkodeService, metrics);
	}

	@Test
	public void shouldMapSakspartNavn() {
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		String navn = mapper.getSakspartNavn(person);
		assertThat(navn, is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
	}

	@Test
	public void shouldMapSakspartNavnWithoutMellomNavn() {
		Bruker person = createPerson(FORNAVN, null, ETTERNAVN);
		String navn = mapper.getSakspartNavn(person);
		assertThat(navn, is(FORNAVN + " " + ETTERNAVN));
	}

	@Test
	public void simpleMapping() {
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settPostadresse(person);
		person.getPostadresse().getUstrukturertAdresse().setAdresselinje2(null);
		person.getPostadresse().getUstrukturertAdresse().setAdresselinje3(null);
		person.getPostadresse().getUstrukturertAdresse().setAdresselinje4(null);
		person.getPostadresse().getUstrukturertAdresse().setLandkode(null);
		MottakerTo mottakerTo = mapper.map(person, "");
		assertThat(mottakerTo.getMottaker().getKortNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat(mottakerTo.getMottaker().getNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
	}

	@Test
	public void shouldMapSpraakKode() throws RegOppslagFunctionalException {
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		person.setMaalform(createSpraak("NO"));
		settBostedadresseMedGateadresse(person);
		MottakerTo mottakerTo = mapper.map(person, "");

		assertThat(mottakerTo.getSpraakKode(), is("NO"));

		person.setMaalform(createSpraak("AA"));
		mottakerTo = mapper.map(person, "");
		assertThat(mottakerTo.getSpraakKode(), is("AA"));
	}

	@Test
	public void mapPersonBostedadresseMedGateadresse() {
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		person.setMaalform(createSpraak("NO"));
		settBostedadresseMedGateadresse(person);
		MottakerTo mottakerTo = mapper.map(person, "");

		assertThat(mottakerTo.getSpraakKode(), is("NO"));
		assertThat(mottakerTo.getMottaker().getKortNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat(mottakerTo.getMottaker().getNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));

		Mottaker mottaker = mottakerTo.getMottaker();
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1()), is(GATENAVN + " " + HUSNR + HUSBOKSTAV));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPostnummer()), is(POSTNR));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPoststed()), is(POSTSTED));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getLand()), is(LAND));
	}

	@Test
	public void mapPersonBostedadresseMedMatrikkeladresse() {
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settBostedadresseMedMatrikkeladresse(person);
		MottakerTo mottakerTo = mapper.map(person, "");
		assertThat(mottakerTo.getMottaker().getKortNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat(mottakerTo.getMottaker().getNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));

		Mottaker mottaker = mottakerTo.getMottaker();
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1()), is(EIENDOMSNAVN));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPostnummer()), is(POSTNR));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPoststed()), is(POSTSTED));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getLand()), is(LAND));
	}

	@Test
	public void mapPersonBostedadresseMedPostboksadresse() {
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settBostedadresseMedPostboksadresse(person);
		MottakerTo mottakerTo = mapper.map(person, "");
		assertThat(mottakerTo.getMottaker().getKortNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat(mottakerTo.getMottaker().getNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));

		Mottaker mottaker = mottakerTo.getMottaker();
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1()), is("Postboks " + POSTBOKS));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPostnummer()), is(POSTNR));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPoststed()), is(POSTSTED));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getLand()), is(LAND));
	}

	@Test
	public void mapPersonPostadresse() {
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settPostadresse(person);
		MottakerTo mottakerTo = mapper.map(person, "");
		assertThat(mottakerTo.getMottaker().getKortNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat(mottakerTo.getMottaker().getNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));

		Mottaker mottaker = mottakerTo.getMottaker();
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1()), is(NORSK_ADRESSELINJE1));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje2()), is(NORSK_ADRESSELINJE2));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje3()), is(NORSK_ADRESSELINJE3));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPostnummer()), is(POSTNR));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPoststed()), is(POSTSTED));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getLand()), is(LAND));
	}

	@Test
	public void mapPersonPostadresseWhereAdresseLinje4HasPostnummer() {
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settPostadresse(person);
		person.getPostadresse().getUstrukturertAdresse().setAdresselinje4("0001 AAAA");
		MottakerTo mottakerTo = mapper.map(person, "");

		Mottaker mottaker = mottakerTo.getMottaker();
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPostnummer()), is("0001"));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPoststed()), is("OSLO"));

		person.getPostadresse().getUstrukturertAdresse().setAdresselinje4("AAAA 0001");
		mottakerTo = mapper.map(person, "");
		mottaker = mottakerTo.getMottaker();
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPostnummer()), is("0001"));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPoststed()), is("OSLO"));
	}

	@Test
	public void shouldMapToUtenlandskAdresseWhenLandIsNotNorway() {
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settPostadresse(person);
		person.getPostadresse().getUstrukturertAdresse().setAdresselinje3("0001 AAAA");
		person.getPostadresse().getUstrukturertAdresse().getLandkode().setValue("SWE");
		MottakerTo mottaker = mapper.map(person, "");
		assertTrue(mottaker.getMottaker().getMottakeradresse() instanceof UtenlandskPostadresse);
	}

	@Test
	public void mapPersonPostadresseMedMidlertidigAdresseUtland() {
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settPostadresseMedMidlertidigUtlandsadresse(person);
		MottakerTo mottakerTo = mapper.map(person, "");
		assertThat(mottakerTo.getMottaker().getKortNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat(mottakerTo.getMottaker().getNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));

		Mottaker mottaker = mottakerTo.getMottaker();
		assertThat((((UtenlandskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1()), is(UTLAND_ADRESSELINJE1));
		assertThat((((UtenlandskPostadresse) mottaker.getMottakeradresse()).getAdresselinje2()), is(UTLAND_ADRESSELINJE2));
		assertThat((((UtenlandskPostadresse) mottaker.getMottakeradresse()).getAdresselinje3()), is(UTLAND_ADRESSELINJE3));
		assertThat((((UtenlandskPostadresse) mottaker.getMottakeradresse()).getLand()), is(LAND_UTLAND));
	}

	@Test
	public void mapPersonPostadresseMedMidlertidigAdresseUtlandPostnummerInAdresseLinje4() {
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settPostadresseMedMidlertidigUtlandsadresse(person);
		MidlertidigPostadresseUtland midlertidigPostadresseUtland = (MidlertidigPostadresseUtland) person.getMidlertidigPostadresse();
		midlertidigPostadresseUtland.getUstrukturertAdresse().setAdresselinje4("0001 ADDDD");
		MottakerTo mottakerTo = mapper.map(person, "");
		assertThat(mottakerTo.getMottaker().getKortNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat(mottakerTo.getMottaker().getNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));

		Mottaker mottaker = mottakerTo.getMottaker();
		assertThat((((UtenlandskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1()), is(UTLAND_ADRESSELINJE1));
		assertThat((((UtenlandskPostadresse) mottaker.getMottakeradresse()).getAdresselinje2()), is(UTLAND_ADRESSELINJE2));
		assertThat((((UtenlandskPostadresse) mottaker.getMottakeradresse()).getAdresselinje3()), is(UTLAND_ADRESSELINJE3));
		assertThat((((UtenlandskPostadresse) mottaker.getMottakeradresse()).getLand()), is(LAND_UTLAND));
	}

	@Test
	public void mapPersonPostadresseMedMidlertidigGateAdresse() {
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settPostadresseMedMidlertidigGateadresse(person);
		MottakerTo mottakerTo = mapper.map(person, "");
		assertThat(mottakerTo.getMottaker().getKortNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat(mottakerTo.getMottaker().getNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));

		Mottaker mottaker = mottakerTo.getMottaker();
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1()), is(GATENAVN + " " + HUSNR + HUSBOKSTAV));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPostnummer()), is(POSTNR));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPoststed()), is(POSTSTED));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getLand()), is(LAND));
	}

	@Test
	public void mapPersonPostadresseMedMidlertidigGateAdresseMedCo() {
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settPostadresseMedMidlertidigGateadresseMedTilleggasadressetype(person, TILLEGGSADRESSETYPE_CO);
		MottakerTo mottakerTo = mapper.map(person, "");

		Mottaker mottaker = mottakerTo.getMottaker();
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1()), is(TILLEGGSADRESSETYPE_CO + " " + TILLEGGSADRESSE));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje2()), is(GATENAVN + " " + HUSNR + HUSBOKSTAV));
	}

	@Test
	public void mapPersonPostadresseMedMidlertidigGateAdresseMedCoAdresse() {
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settPostadresseMedMidlertidigGateadresseMedTilleggsadresse(person, CO_TILLEGGSADRESSE);
		MottakerTo mottakerTo = mapper.map(person, "");

		Mottaker mottaker = mottakerTo.getMottaker();
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1()), is(CO_TILLEGGSADRESSE));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje2()), is(GATENAVN + " " + HUSNR + HUSBOKSTAV));

	}

	@Test
	public void mapPersonPostadresseMedMidlertidigGateAdresseMedV() {
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settPostadresseMedMidlertidigGateadresseMedTilleggasadressetype(person, TILLEGGSADRESSETYPE_V);
		MottakerTo mottakerTo = mapper.map(person, "");

		Mottaker mottaker = mottakerTo.getMottaker();
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1()), is(TILLEGGSADRESSETYPE_V + " " + TILLEGGSADRESSE));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje2()), is(GATENAVN + " " + HUSNR + HUSBOKSTAV));
	}

	@Test
	public void mapPersonPostadresseMedMidlertidigGateAdresseMedVAdresse() {
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settPostadresseMedMidlertidigGateadresseMedTilleggsadresse(person, V_TILLEGGSADRESSE);
		MottakerTo mottakerTo = mapper.map(person, "");

		Mottaker mottaker = mottakerTo.getMottaker();
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1()), is(V_TILLEGGSADRESSE));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje2()), is(GATENAVN + " " + HUSNR + HUSBOKSTAV));

	}

	@Test
	public void mapPersonPostadresseMedMidlertidigMatrikkelAdresse() {
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settPostadresseMedMidlertidigMatrikkeladresse(person);
		MottakerTo mottakerTo = mapper.map(person, "");
		assertThat(mottakerTo.getMottaker().getKortNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat(mottakerTo.getMottaker().getNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));

		Mottaker mottaker = mottakerTo.getMottaker();
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1()), is(EIENDOMSNAVN));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPostnummer()), is(POSTNR));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPoststed()), is(POSTSTED));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getLand()), is(LAND));
	}

	@Test
	public void mapPersonPostadresseMedMidlertidigMatrikkelAdresseMedCo() {
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settPostadresseMedMidlertidigMatrikkeladresseMedCo(person);
		MottakerTo mottakerTo = mapper.map(person, "");

		Mottaker mottaker = mottakerTo.getMottaker();
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1()), is(TILLEGGSADRESSETYPE_CO + " " + TILLEGGSADRESSE));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje2()), is(EIENDOMSNAVN));
	}

	@Test
	public void mapPersonPostadresseMedMidlertidigMatrikkelAdresseMedCoAdresse() {
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settPostadresseMedMidlertidigMatrikkelAdresseMedCoAdresse(person);
		MottakerTo mottakerTo = mapper.map(person, "");

		Mottaker mottaker = mottakerTo.getMottaker();
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1()), is(CO_TILLEGGSADRESSE));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje2()), is(EIENDOMSNAVN));
	}


	@Test
	public void mapPersonBostedadresseMedMidlertidigPostboksAdresse() {
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settPostadresseMedMidlertidigPostboksadresse(person);
		MottakerTo mottakerTo = mapper.map(person, "");
		assertThat(mottakerTo.getMottaker().getKortNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat(mottakerTo.getMottaker().getNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));

		Mottaker mottaker = mottakerTo.getMottaker();
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1()), is("Postboks " + POSTBOKS));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPostnummer()), is(POSTNR));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPoststed()), is(POSTSTED));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getLand()), is(LAND));
	}

	@Test
	public void mapPersonBostedAdresseMedMidlertidigPostboksAdresseMedCo() {
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settMidlertidigPostBoksAdresseMedCo(person);
		MottakerTo mottakerTo = mapper.map(person, "");

		Mottaker mottaker = mottakerTo.getMottaker();
		assertThat(((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1(), is(TILLEGGSADRESSETYPE_CO + " " + TILLEGGSADRESSE));
		assertThat(((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje2(), is("Postboks" + " " + POSTBOKS));
	}

	@Test
	public void mapPersonBostedAdresseMedMidlertidigPostboksAdresseMedCoAdresse() {
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settMidlertidigPostboksAdresseMedCoAdresse(person);
		MottakerTo mottakerTo = mapper.map(person, "");

		Mottaker mottaker = mottakerTo.getMottaker();
		assertThat(((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1(), is(CO_TILLEGGSADRESSE));
		assertThat(((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje2(), is("Postboks" + " " + POSTBOKS));
	}

	@Test
	public void mapDodPersonWithAdresse() throws DatatypeConfigurationException {
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settPostadresse(person);

		Doedsdato doedsdato = new Doedsdato();
		doedsdato.setDoedsdato(createDoedsdato());
		person.setDoedsdato(doedsdato);

		MottakerTo mottakerTo = mapper.map(person, "");
		assertThat(mottakerTo.getMottaker().getKortNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat(mottakerTo.getMottaker().getNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));

		Mottaker mottaker = mottakerTo.getMottaker();
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1()), is(NORSK_ADRESSELINJE1));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje2()), is(NORSK_ADRESSELINJE2));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje3()), is(NORSK_ADRESSELINJE3));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPostnummer()), is(POSTNR));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPoststed()), is(POSTSTED));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getLand()), is(LAND));

	}

	@Test
	public void shouldThrowIfDodPersonWithoutAdress() throws DatatypeConfigurationException {
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		Doedsdato doedsdato = new Doedsdato();
		doedsdato.setDoedsdato(createDoedsdato());
		person.setDoedsdato(doedsdato);
		UkjentAdressePersonErDoed e = Assertions.assertThrows(UkjentAdressePersonErDoed.class,
				() -> mapper.map(person, ""));
	}

	@Test
	public void shouldThrowIfUtenPostnrAndLand() {
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settPostadresse(person);
		person.getPostadresse().getUstrukturertAdresse().setAdresselinje4(null);
		person.getPostadresse().getUstrukturertAdresse().setAdresselinje1(null);
		person.getPostadresse().getUstrukturertAdresse().setLandkode(null);
		UkjentAdresseException e = Assertions.assertThrows(UkjentAdresseException.class,
				() -> mapper.map(person, ""));
	}

	@Test
	public void shouldThrowIfUkjentAdresse() {
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settPostadresse(person);
		Postadressetyper postadressetyper = new Postadressetyper();
		postadressetyper.setValue("UKJENT_ADRESSE");
		person.setGjeldendePostadressetype(postadressetyper);
		UkjentAdresseException e = Assertions.assertThrows(UkjentAdresseException.class,
				() -> mapper.map(person, ""));
	}

	@Test
	public void shouldNotThrowIfNotMissingPostnrButMissingAdresseLinje1() {
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settPostadresse(person);

		person.getPostadresse().getUstrukturertAdresse().setAdresselinje1(null);
		person.getPostadresse().getUstrukturertAdresse().setAdresselinje4("0001 AAAA");
		person.getPostadresse().getUstrukturertAdresse().setLandkode(createLandkode("NOR"));
		mapper.map(person, "");
	}

	@Test
	public void shouldNotThrowIfMissingNotMissingAdresseLinje1ButMissingOtherAdresseAttributes() {

		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settPostadresse(person);
		person.getPostadresse().getUstrukturertAdresse().setAdresselinje2(null);
		person.getPostadresse().getUstrukturertAdresse().setAdresselinje3(null);
		person.getPostadresse().getUstrukturertAdresse().setAdresselinje4(null);
		person.getPostadresse().getUstrukturertAdresse().setLandkode(null);
		MottakerTo mottaker = mapper.map(person, "");
		NorskPostadresse norskPostadresse = (NorskPostadresse) mottaker.getMottaker().getMottakeradresse();
		assertThat(norskPostadresse.getAdresselinje1(), is(NORSK_ADRESSELINJE1));
	}

	@Test
	public void testFunctionalMetrics() {
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settPostadresse(person);
		person.getPostadresse().getUstrukturertAdresse().setAdresselinje2(null);
		person.getPostadresse().getUstrukturertAdresse().setAdresselinje3(null);
		person.getPostadresse().getUstrukturertAdresse().setAdresselinje4(null);

		person.getPostadresse().getUstrukturertAdresse().setLandkode(null);
		mapper.map(person, "T");

		assertThat(metrics.countEvents("T", PERSONV3_MAPPER, UKJENT_POSTNUMMER), is(1.0));
		assertThat(metrics.countEvents("T", PERSONV3_MAPPER, UKJENT_LAND), is(1.0));

		person.getPostadresse().getUstrukturertAdresse().setLandkode(createLandkode("SE"));
		mapper.map(person, "T");
		assertThat(metrics.countEvents("T", PERSONV3_MAPPER, UKJENT_POSTNUMMER), is(1.0));
		assertThat(metrics.countEvents("T", PERSONV3_MAPPER, UKJENT_LAND), is(1.0));
	}


	@Test
	public void shouldNotThrowIfMissingLandkode() {

		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settPostadresse(person);
		person.getPostadresse().getUstrukturertAdresse().setLandkode(null);
		mapper.map(person, "");
	}

	private Bruker createPerson(String fornavn, String mellomnavn, String etternavn) {
		Personnavn personnavn = new Personnavn();
		personnavn.setFornavn(fornavn);
		if (mellomnavn != null) {
			personnavn.setMellomnavn(mellomnavn);
			personnavn.setSammensattNavn(fornavn + " " + mellomnavn + " " + etternavn);
		} else {
			personnavn.setSammensattNavn(fornavn + " " + etternavn);
		}
		personnavn.setEtternavn(etternavn);
		Bruker person = new Bruker();
		person.setPersonnavn(personnavn);
		return person;
	}

	private void settBostedadresseMedGateadresse(Bruker person) {
		Postadressetyper postadressetyper = new Postadressetyper();
		postadressetyper.setKodeverksRef("BOSTEDSADRESSE");
		postadressetyper.setValue("BOSTEDSADRESSE");
		person.setGjeldendePostadressetype(postadressetyper);

		Gateadresse gateadresse = new Gateadresse();
		gateadresse.setGatenavn(GATENAVN);
		gateadresse.setHusnummer(HUSNR);
		gateadresse.setHusbokstav(HUSBOKSTAV);

		Postnummer postnummer = new Postnummer();
		postnummer.setKodeverksRef(POSTNR);
		postnummer.setKodeRef(POSTNR);
		postnummer.setValue(POSTNR);
		gateadresse.setPoststed(postnummer);

		Landkoder landkoder = new Landkoder();
		landkoder.setKodeverksRef(LANDKODE);
		landkoder.setKodeRef(LANDKODE);
		landkoder.setValue(LANDKODE);
		gateadresse.setLandkode(landkoder);

		Bostedsadresse bostedsadresse = new Bostedsadresse();
		bostedsadresse.setStrukturertAdresse(gateadresse);

		person.setBostedsadresse(bostedsadresse);
	}

	private XMLGregorianCalendar createDoedsdato() throws DatatypeConfigurationException {
		LocalDate date = LocalDate.now().minusMonths(1);
		GregorianCalendar gcal = GregorianCalendar.from(date.atStartOfDay(ZoneId.systemDefault()));
		return DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
	}

	private Landkoder createLandkode(String landkode) {
		Landkoder landkoder = new Landkoder();
		landkoder.setValue(landkode);
		return landkoder;
	}

	private void settBostedadresseMedMatrikkeladresse(Bruker person) {
		Postadressetyper postadressetyper = new Postadressetyper();
		postadressetyper.setKodeverksRef("BOSTEDSADRESSE");
		postadressetyper.setValue("BOSTEDSADRESSE");
		person.setGjeldendePostadressetype(postadressetyper);

		Matrikkeladresse matrikkeladresse = new Matrikkeladresse();
		matrikkeladresse.setEiendomsnavn(EIENDOMSNAVN);

		Postnummer postnummer = new Postnummer();
		postnummer.setKodeverksRef(POSTNR);
		postnummer.setKodeRef(POSTNR);
		postnummer.setValue(POSTNR);
		matrikkeladresse.setPoststed(postnummer);

		Landkoder landkoder = new Landkoder();
		landkoder.setKodeverksRef(LANDKODE);
		landkoder.setKodeRef(LANDKODE);
		landkoder.setValue(LANDKODE);
		matrikkeladresse.setLandkode(landkoder);

		Bostedsadresse bostedsadresse = new Bostedsadresse();
		bostedsadresse.setStrukturertAdresse(matrikkeladresse);

		person.setBostedsadresse(bostedsadresse);
	}

	private void settBostedadresseMedPostboksadresse(Bruker person) {
		Postadressetyper postadressetyper = new Postadressetyper();
		postadressetyper.setKodeverksRef("BOSTEDSADRESSE");
		postadressetyper.setValue("BOSTEDSADRESSE");
		person.setGjeldendePostadressetype(postadressetyper);

		PostboksadresseNorsk postboksadresse = new PostboksadresseNorsk();
		postboksadresse.setPostboksnummer(POSTBOKS);

		Postnummer postnummer = new Postnummer();
		postnummer.setKodeverksRef(POSTNR);
		postnummer.setKodeRef(POSTNR);
		postnummer.setValue(POSTNR);
		postboksadresse.setPoststed(postnummer);

		Landkoder landkoder = new Landkoder();
		landkoder.setKodeverksRef(LANDKODE);
		landkoder.setKodeRef(LANDKODE);
		landkoder.setValue(LANDKODE);
		postboksadresse.setLandkode(landkoder);

		Bostedsadresse bostedsadresse = new Bostedsadresse();
		bostedsadresse.setStrukturertAdresse(postboksadresse);

		person.setBostedsadresse(bostedsadresse);
	}

	private void settPostadresse(Bruker person) {
		Postadressetyper postadressetyper = new Postadressetyper();
		postadressetyper.setKodeverksRef("POSTADRESSE");
		postadressetyper.setValue("POSTADRESSE");
		person.setGjeldendePostadressetype(postadressetyper);

		UstrukturertAdresse ustrukturertAdresse = new UstrukturertAdresse();
		ustrukturertAdresse.setAdresselinje1(NORSK_ADRESSELINJE1);
		ustrukturertAdresse.setAdresselinje2(NORSK_ADRESSELINJE2);
		ustrukturertAdresse.setAdresselinje3(NORSK_ADRESSELINJE3);
		ustrukturertAdresse.setAdresselinje4(POSTNR);

		Landkoder landkoder = new Landkoder();
		landkoder.setKodeverksRef(LANDKODE);
		landkoder.setKodeRef(LANDKODE);
		landkoder.setValue(LANDKODE);
		ustrukturertAdresse.setLandkode(landkoder);

		Postadresse postadresse = new Postadresse();
		postadresse.setUstrukturertAdresse(ustrukturertAdresse);

		person.setPostadresse(postadresse);
	}

	private void settPostadresseMedMidlertidigUtlandsadresse(Bruker person) {
		Postadressetyper postadressetyper = new Postadressetyper();
		postadressetyper.setKodeverksRef("MIDLERTIDIG_POSTADRESSE_UTLAND");
		postadressetyper.setValue("MIDLERTIDIG_POSTADRESSE_UTLAND");
		person.setGjeldendePostadressetype(postadressetyper);

		UstrukturertAdresse ustrukturertAdresse = new UstrukturertAdresse();
		ustrukturertAdresse.setAdresselinje1(UTLAND_ADRESSELINJE1);
		ustrukturertAdresse.setAdresselinje2(UTLAND_ADRESSELINJE2);
		ustrukturertAdresse.setAdresselinje3(UTLAND_ADRESSELINJE3);
		ustrukturertAdresse.setAdresselinje4(UTLAND_ADRESSELINJE4);

		Landkoder landkoder = new Landkoder();
		landkoder.setKodeRef(LANDKODE_UTLAND);
		landkoder.setValue(LANDKODE_UTLAND);
		ustrukturertAdresse.setLandkode(landkoder);

		MidlertidigPostadresseUtland midlertidigPostadresseUtland = new MidlertidigPostadresseUtland();
		midlertidigPostadresseUtland.setUstrukturertAdresse(ustrukturertAdresse);

		person.setMidlertidigPostadresse(midlertidigPostadresseUtland);
	}

	private void settPostadresseMedMidlertidigGateadresse(Bruker person) {
		Postadressetyper postadressetyper = new Postadressetyper();
		postadressetyper.setKodeverksRef("MIDLERTIDIG_POSTADRESSE_NORGE");
		postadressetyper.setValue("MIDLERTIDIG_POSTADRESSE_NORGE");
		person.setGjeldendePostadressetype(postadressetyper);

		Gateadresse gateadresse = new Gateadresse();
		gateadresse.setGatenavn(GATENAVN);
		gateadresse.setHusnummer(HUSNR);
		gateadresse.setHusbokstav(HUSBOKSTAV);

		Postnummer postnummer = new Postnummer();
		postnummer.setKodeRef(POSTNR);
		postnummer.setValue(POSTNR);
		gateadresse.setPoststed(postnummer);

		Landkoder landkoder = new Landkoder();
		landkoder.setKodeRef(LANDKODE);
		landkoder.setValue(LANDKODE);
		gateadresse.setLandkode(landkoder);

		MidlertidigPostadresseNorge midlertidigPostadresseNorge = new MidlertidigPostadresseNorge();
		midlertidigPostadresseNorge.setStrukturertAdresse(gateadresse);

		person.setMidlertidigPostadresse(midlertidigPostadresseNorge);
	}

	private void settPostadresseMedMidlertidigGateadresseMedTilleggasadressetype(Bruker person, String tilleggsadressetype) {
		Postadressetyper postadressetyper = new Postadressetyper();
		postadressetyper.setKodeverksRef("MIDLERTIDIG_POSTADRESSE_NORGE");
		postadressetyper.setValue("MIDLERTIDIG_POSTADRESSE_NORGE");
		person.setGjeldendePostadressetype(postadressetyper);

		Gateadresse gateadresse = new Gateadresse();
		gateadresse.setGatenavn(GATENAVN);
		gateadresse.setHusnummer(HUSNR);
		gateadresse.setHusbokstav(HUSBOKSTAV);

		Postnummer postnummer = new Postnummer();
		postnummer.setKodeRef(POSTNR);
		postnummer.setValue(POSTNR);
		gateadresse.setPoststed(postnummer);

		Landkoder landkoder = new Landkoder();
		landkoder.setKodeRef(LANDKODE);
		landkoder.setValue(LANDKODE);
		gateadresse.setLandkode(landkoder);
		gateadresse.setTilleggsadresse(TILLEGGSADRESSE);
		gateadresse.setTilleggsadresseType(tilleggsadressetype);

		MidlertidigPostadresseNorge midlertidigPostadresseNorge = new MidlertidigPostadresseNorge();
		midlertidigPostadresseNorge.setStrukturertAdresse(gateadresse);

		person.setMidlertidigPostadresse(midlertidigPostadresseNorge);
	}

	private void settPostadresseMedMidlertidigGateadresseMedTilleggsadresse(Bruker person, String tilleggsadresse) {
		Postadressetyper postadressetyper = new Postadressetyper();
		postadressetyper.setKodeverksRef("MIDLERTIDIG_POSTADRESSE_NORGE");
		postadressetyper.setValue("MIDLERTIDIG_POSTADRESSE_NORGE");
		person.setGjeldendePostadressetype(postadressetyper);

		Gateadresse gateadresse = new Gateadresse();
		gateadresse.setGatenavn(GATENAVN);
		gateadresse.setHusnummer(HUSNR);
		gateadresse.setHusbokstav(HUSBOKSTAV);

		Postnummer postnummer = new Postnummer();
		postnummer.setKodeRef(POSTNR);
		postnummer.setValue(POSTNR);
		gateadresse.setPoststed(postnummer);

		Landkoder landkoder = new Landkoder();
		landkoder.setKodeRef(LANDKODE);
		landkoder.setValue(LANDKODE);
		gateadresse.setLandkode(landkoder);
		gateadresse.setTilleggsadresse(tilleggsadresse);

		MidlertidigPostadresseNorge midlertidigPostadresseNorge = new MidlertidigPostadresseNorge();
		midlertidigPostadresseNorge.setStrukturertAdresse(gateadresse);

		person.setMidlertidigPostadresse(midlertidigPostadresseNorge);
	}

	private void settMidlertidigPostboksAdresseMedCoAdresse(Bruker person) {
		Postadressetyper postadressetyper = new Postadressetyper();
		postadressetyper.setKodeverksRef("MIDLERTIDIG_POSTADRESSE_NORGE");
		postadressetyper.setValue("MIDLERTIDIG_POSTADRESSE_NORGE");
		person.setGjeldendePostadressetype(postadressetyper);

		PostboksadresseNorsk postboksadresse = new PostboksadresseNorsk();
		postboksadresse.setPostboksnummer(POSTBOKS);

		Postnummer postnummer = new Postnummer();
		postnummer.setKodeRef(POSTNR);
		postnummer.setValue(POSTNR);
		postboksadresse.setPoststed(postnummer);

		Landkoder landkoder = new Landkoder();
		landkoder.setKodeRef(LANDKODE);
		landkoder.setValue(LANDKODE);
		postboksadresse.setLandkode(landkoder);
		postboksadresse.setTilleggsadresse(CO_TILLEGGSADRESSE);
		MidlertidigPostadresseNorge midlertidigPostadresseNorge = new MidlertidigPostadresseNorge();
		midlertidigPostadresseNorge.setStrukturertAdresse(postboksadresse);

		person.setMidlertidigPostadresse(midlertidigPostadresseNorge);
	}

	private void settMidlertidigPostBoksAdresseMedCo(Bruker person) {
		Postadressetyper postadressetyper = new Postadressetyper();
		postadressetyper.setKodeverksRef("MIDLERTIDIG_POSTADRESSE_NORGE");
		postadressetyper.setValue("MIDLERTIDIG_POSTADRESSE_NORGE");
		person.setGjeldendePostadressetype(postadressetyper);

		PostboksadresseNorsk postboksadresse = new PostboksadresseNorsk();
		postboksadresse.setPostboksnummer(POSTBOKS);

		Postnummer postnummer = new Postnummer();
		postnummer.setKodeRef(POSTNR);
		postnummer.setValue(POSTNR);
		postboksadresse.setPoststed(postnummer);

		Landkoder landkoder = new Landkoder();
		landkoder.setKodeRef(LANDKODE);
		landkoder.setValue(LANDKODE);
		postboksadresse.setLandkode(landkoder);
		postboksadresse.setTilleggsadresse(TILLEGGSADRESSE);
		postboksadresse.setTilleggsadresseType(TILLEGGSADRESSETYPE_CO);
		MidlertidigPostadresseNorge midlertidigPostadresseNorge = new MidlertidigPostadresseNorge();
		midlertidigPostadresseNorge.setStrukturertAdresse(postboksadresse);

		person.setMidlertidigPostadresse(midlertidigPostadresseNorge);
	}

	private void settPostadresseMedMidlertidigMatrikkeladresse(Bruker person) {
		Postadressetyper postadressetyper = new Postadressetyper();
		postadressetyper.setKodeverksRef("MIDLERTIDIG_POSTADRESSE_NORGE");
		postadressetyper.setValue("MIDLERTIDIG_POSTADRESSE_NORGE");
		person.setGjeldendePostadressetype(postadressetyper);

		Matrikkeladresse matrikkeladresse = new Matrikkeladresse();
		matrikkeladresse.setEiendomsnavn(EIENDOMSNAVN);

		Postnummer postnummer = new Postnummer();
		postnummer.setKodeRef(POSTNR);
		postnummer.setValue(POSTNR);
		matrikkeladresse.setPoststed(postnummer);

		Landkoder landkoder = new Landkoder();
		landkoder.setKodeRef(LANDKODE);
		landkoder.setValue(LANDKODE);
		matrikkeladresse.setLandkode(landkoder);

		MidlertidigPostadresseNorge midlertidigPostadresseNorge = new MidlertidigPostadresseNorge();
		midlertidigPostadresseNorge.setStrukturertAdresse(matrikkeladresse);

		person.setMidlertidigPostadresse(midlertidigPostadresseNorge);
	}

	private void settPostadresseMedMidlertidigMatrikkelAdresseMedCoAdresse(Bruker person) {
		Postadressetyper postadressetyper = new Postadressetyper();
		postadressetyper.setKodeverksRef("MIDLERTIDIG_POSTADRESSE_NORGE");
		postadressetyper.setValue("MIDLERTIDIG_POSTADRESSE_NORGE");
		person.setGjeldendePostadressetype(postadressetyper);

		Matrikkeladresse matrikkeladresse = new Matrikkeladresse();
		matrikkeladresse.setEiendomsnavn(EIENDOMSNAVN);

		Postnummer postnummer = new Postnummer();
		postnummer.setKodeRef(POSTNR);
		postnummer.setValue(POSTNR);
		matrikkeladresse.setPoststed(postnummer);

		Landkoder landkoder = new Landkoder();
		landkoder.setKodeRef(LANDKODE);
		landkoder.setValue(LANDKODE);
		matrikkeladresse.setLandkode(landkoder);
		matrikkeladresse.setTilleggsadresse(CO_TILLEGGSADRESSE);

		MidlertidigPostadresseNorge midlertidigPostadresseNorge = new MidlertidigPostadresseNorge();
		midlertidigPostadresseNorge.setStrukturertAdresse(matrikkeladresse);

		person.setMidlertidigPostadresse(midlertidigPostadresseNorge);
	}

	private void settPostadresseMedMidlertidigMatrikkeladresseMedCo(Bruker person) {
		Postadressetyper postadressetyper = new Postadressetyper();
		postadressetyper.setKodeverksRef("MIDLERTIDIG_POSTADRESSE_NORGE");
		postadressetyper.setValue("MIDLERTIDIG_POSTADRESSE_NORGE");
		person.setGjeldendePostadressetype(postadressetyper);

		Matrikkeladresse matrikkeladresse = new Matrikkeladresse();
		matrikkeladresse.setEiendomsnavn(EIENDOMSNAVN);

		Postnummer postnummer = new Postnummer();
		postnummer.setKodeRef(POSTNR);
		postnummer.setValue(POSTNR);
		matrikkeladresse.setPoststed(postnummer);

		Landkoder landkoder = new Landkoder();
		landkoder.setKodeRef(LANDKODE);
		landkoder.setValue(LANDKODE);
		matrikkeladresse.setLandkode(landkoder);
		matrikkeladresse.setTilleggsadresse(TILLEGGSADRESSE);
		matrikkeladresse.setTilleggsadresseType(TILLEGGSADRESSETYPE_CO);

		MidlertidigPostadresseNorge midlertidigPostadresseNorge = new MidlertidigPostadresseNorge();
		midlertidigPostadresseNorge.setStrukturertAdresse(matrikkeladresse);

		person.setMidlertidigPostadresse(midlertidigPostadresseNorge);
	}

	private void settPostadresseMedMidlertidigPostboksadresse(Bruker person) {
		Postadressetyper postadressetyper = new Postadressetyper();
		postadressetyper.setKodeverksRef("MIDLERTIDIG_POSTADRESSE_NORGE");
		postadressetyper.setValue("MIDLERTIDIG_POSTADRESSE_NORGE");
		person.setGjeldendePostadressetype(postadressetyper);

		PostboksadresseNorsk postboksadresse = new PostboksadresseNorsk();
		postboksadresse.setPostboksnummer(POSTBOKS);

		Postnummer postnummer = new Postnummer();
		postnummer.setKodeRef(POSTNR);
		postnummer.setValue(POSTNR);
		postboksadresse.setPoststed(postnummer);

		Landkoder landkoder = new Landkoder();
		landkoder.setKodeRef(LANDKODE);
		landkoder.setValue(LANDKODE);
		postboksadresse.setLandkode(landkoder);

		MidlertidigPostadresseNorge midlertidigPostadresseNorge = new MidlertidigPostadresseNorge();
		midlertidigPostadresseNorge.setStrukturertAdresse(postboksadresse);

		person.setMidlertidigPostadresse(midlertidigPostadresseNorge);
	}

	private Mottaker createMottaker(String fnr) {
		Mottaker mottaker = new Person();
		mottaker.setSpraakkode(Spraakkode.NB);
		mottaker.setId(fnr);
		mottaker.setTypeKode(AktoerType.PERSON);
		return mottaker;
	}

	private Spraak createSpraak(String spraakKode) {
		Spraak spraak = new Spraak();
		spraak.setValue(spraakKode);
		return spraak;
	}
}
