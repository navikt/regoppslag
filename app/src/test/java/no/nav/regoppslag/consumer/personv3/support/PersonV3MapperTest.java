package no.nav.regoppslag.consumer.personv3.support;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.dok.brevdata.felles.v1.navfelles.Person;
import no.nav.dok.brevdata.felles.v1.simpletypes.AktoerType;
import no.nav.dok.brevdata.felles.v1.simpletypes.Spraakkode;
import no.nav.regoppslag.service.LandkodeService;
import no.nav.regoppslag.service.PostnummerService;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bostedsadresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
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
import no.nav.tjeneste.virksomhet.person.v3.informasjon.UstrukturertAdresse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PersonV3MapperTest {
	private static final String FNR = "99999999999";
	private static final String FORNAVN = "TOM";
	private static final String MELLOMNAVN = "MARVOLO";
	private static final String ETTERNAVN = "RIDDLE";
	private static final String GATENAVN = "Gatenavn";
	private static final int HUSNR = 13;
	private static final String HUSBOKSTAV = "X";
	private static final String EIENDOMSNAVN = "Min eiendom";
	private static final String POSTBOKS = "Postboks 123";
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
	private static final String LAND = "Norway";
	private static final String LAND_UTLAND = "Denmark";

	private PostnummerService postnummerService = new PostnummerService();
	private LandkodeService landkodeService = new LandkodeService();
	private PersonV3Mapper mapper;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void initPostnummer() throws Exception {
		postnummerService.init();
		mapper = new PersonV3Mapper(postnummerService, landkodeService);
	}

	@Test
	public void simpleMapping() throws Exception {
		Mottaker mottaker = createMottaker(FNR);
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		mapper.map(person, mottaker, "");
		assertThat(mottaker.getId(), is(FNR));
		assertThat(mottaker.getKortNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat(mottaker.getNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
	}

	@Test
	public void mapPersonBostedadresseMedGateadresse() throws Exception {
		Mottaker mottaker = createMottaker(FNR);
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settBostedadresseMedGateadresse(person);
		mapper.map(person, mottaker, "");
		assertThat(mottaker.getId(), is(FNR));
		assertThat(mottaker.getKortNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat(mottaker.getNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1()), is(GATENAVN + " " + HUSNR + HUSBOKSTAV));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPostnummer()), is(POSTNR));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPoststed()), is(POSTSTED));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getLand()), is(LAND));
	}

	@Test
	public void mapPersonBostedadresseMedMatrikkeladresse() throws Exception {
		Mottaker mottaker = createMottaker(FNR);
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settBostedadresseMedMatrikkeladresse(person);
		mapper.map(person, mottaker, "");
		assertThat(mottaker.getId(), is(FNR));
		assertThat(mottaker.getKortNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat(mottaker.getNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1()), is(EIENDOMSNAVN));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPostnummer()), is(POSTNR));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPoststed()), is(POSTSTED));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getLand()), is(LAND));
	}

	@Test
	public void mapPersonBostedadresseMedPostboksadresse() throws Exception {
		Mottaker mottaker = createMottaker(FNR);
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settBostedadresseMedPostboksadresse(person);
		mapper.map(person, mottaker, "");
		assertThat(mottaker.getId(), is(FNR));
		assertThat(mottaker.getKortNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat(mottaker.getNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1()), is(POSTBOKS));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPostnummer()), is(POSTNR));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPoststed()), is(POSTSTED));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getLand()), is(LAND));
	}

	@Test
	public void mapPersonPostadresse() throws Exception {
		Mottaker mottaker = createMottaker(FNR);
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settPostadresse(person);
		mapper.map(person, mottaker, "");
		assertThat(mottaker.getId(), is(FNR));
		assertThat(mottaker.getKortNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat(mottaker.getNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1()), is(NORSK_ADRESSELINJE1));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje2()), is(NORSK_ADRESSELINJE2));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje3()), is(NORSK_ADRESSELINJE3));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPostnummer()), is(POSTNR));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPoststed()), is(POSTSTED));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getLand()), is(LAND));
	}

	@Test
	public void mapPersonPostadresseMedMidlertidigAdresseUtland() throws Exception {
		Mottaker mottaker = createMottaker(FNR);
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settPostadresseMedMidlertidigAresseUtland(person);
		mapper.map(person, mottaker, "");
		assertThat(mottaker.getId(), is(FNR));
		assertThat(mottaker.getKortNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat(mottaker.getNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1()), is(UTLAND_ADRESSELINJE1));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje2()), is(UTLAND_ADRESSELINJE2));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje3()), is(UTLAND_ADRESSELINJE3));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPostnummer()), is("0000"));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPoststed()), is("UKJENT/UNKNOWN"));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getLand()), is(LAND_UTLAND));
	}

	@Test
	public void mapPersonPostadresseMedMidlertidigGateAdresse() throws Exception {
		Mottaker mottaker = createMottaker(FNR);
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settPostadresseMedMidlertidigAresseGste(person);
		mapper.map(person, mottaker, "");
		assertThat(mottaker.getId(), is(FNR));
		assertThat(mottaker.getKortNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat(mottaker.getNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1()), is(GATENAVN + " " + HUSNR + HUSBOKSTAV));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPostnummer()), is(POSTNR));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPoststed()), is(POSTSTED));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getLand()), is(LAND));
	}

	@Test
	public void mapPersonPostadresseMedMidlertidigMatrikkelAdresse() throws Exception {
		Mottaker mottaker = createMottaker(FNR);
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settPostadresseMedMidlertidigAresseMatrikkel(person);
		mapper.map(person, mottaker, "");
		assertThat(mottaker.getId(), is(FNR));
		assertThat(mottaker.getKortNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat(mottaker.getNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1()), is(EIENDOMSNAVN));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPostnummer()), is(POSTNR));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPoststed()), is(POSTSTED));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getLand()), is(LAND));
	}

	@Test
	public void mapPersonBostedadresseMedMidlertidigPostboksAdresse() throws Exception {
		Mottaker mottaker = createMottaker(FNR);
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settPostadresseMedMidlertidigAressePostboks(person);
		mapper.map(person, mottaker, "");
		assertThat(mottaker.getId(), is(FNR));
		assertThat(mottaker.getKortNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat(mottaker.getNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getAdresselinje1()), is(POSTBOKS));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPostnummer()), is(POSTNR));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPoststed()), is(POSTSTED));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getLand()), is(LAND));
	}


	@Test
	public void mapPersonPostadresseUtenPostnr() throws Exception {
		Mottaker mottaker = createMottaker(FNR);
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settPostadresse(person);
		person.getPostadresse().getUstrukturertAdresse().setAdresselinje4("");
		mapper.map(person, mottaker, "");
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPostnummer()), is("0000"));
		assertThat((((NorskPostadresse) mottaker.getMottakeradresse()).getPoststed()), is("UKJENT/UNKNOWN"));
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

	private void settPostadresseMedMidlertidigAresseUtland(Bruker person) {
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

	private void settPostadresseMedMidlertidigAresseGste(Bruker person) {
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

	private void settPostadresseMedMidlertidigAresseMatrikkel(Bruker person) {
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

	private void settPostadresseMedMidlertidigAressePostboks(Bruker person) {
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

}
