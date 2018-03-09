package no.nav.regoppslag.consumer.personv3.support;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import no.nav.dok.metaforcemal.jaxb2.gen.AktoerType;
import no.nav.dok.metaforcemal.jaxb2.gen.Mottaker;
import no.nav.dok.metaforcemal.jaxb2.gen.NorskPostadresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bostedsadresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Gateadresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Matrikkeladresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.MidlertidigPostadresseNorge;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.MidlertidigPostadresseUtland;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personnavn;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Postadresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Postadressetyper;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PostboksadresseNorsk;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.UstrukturertAdresse;
import org.junit.Test;

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

	private PersonV3Mapper mapper = new PersonV3Mapper();

	@Test
	public void simpleMapping() {
		Mottaker mottaker = createMottaker(FNR);
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		mapper.map(person, mottaker);
		assertThat(mottaker.getId(), is(FNR));
		assertThat(mottaker.getKortNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat(mottaker.getNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
	}

	@Test
	public void mapPersonBostedadresseMedGateadresse() {
		Mottaker mottaker = createMottaker(FNR);
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settBostedadresseMedGateadresse(person);
		mapper.map(person, mottaker);
		assertThat(mottaker.getId(), is(FNR));
		assertThat(mottaker.getKortNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat(mottaker.getNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getAdresselinje1()), is(GATENAVN + " " + HUSNR + HUSBOKSTAV));
	}

	@Test
	public void mapPersonBostedadresseMedMatrikkeladresse() {
		Mottaker mottaker = createMottaker(FNR);
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settBostedadresseMedMatrikkeladresse(person);
		mapper.map(person, mottaker);
		assertThat(mottaker.getId(), is(FNR));
		assertThat(mottaker.getKortNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat(mottaker.getNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getAdresselinje1()), is(EIENDOMSNAVN));
	}

	@Test
	public void mapPersonBostedadresseMedPostboksadresse() {
		Mottaker mottaker = createMottaker(FNR);
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settBostedadresseMedPostboksadresse(person);
		mapper.map(person, mottaker);
		assertThat(mottaker.getId(), is(FNR));
		assertThat(mottaker.getKortNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat(mottaker.getNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getAdresselinje1()), is(POSTBOKS));
	}

	@Test
	public void mapPersonPostadresse() {
		Mottaker mottaker = createMottaker(FNR);
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settPostadresse(person);
		mapper.map(person, mottaker);
		assertThat(mottaker.getId(), is(FNR));
		assertThat(mottaker.getKortNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat(mottaker.getNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getAdresselinje1()), is(NORSK_ADRESSELINJE1));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getAdresselinje2()), is(NORSK_ADRESSELINJE2));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getAdresselinje3()), is(NORSK_ADRESSELINJE3));
	}

	@Test
	public void mapPersonPostadresseMedMidlertidigAdresseUtland() {
		Mottaker mottaker = createMottaker(FNR);
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settPostadresseMedMidlertidigAresseUtland(person);
		mapper.map(person, mottaker);
		assertThat(mottaker.getId(), is(FNR));
		assertThat(mottaker.getKortNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat(mottaker.getNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getAdresselinje1()), is(UTLAND_ADRESSELINJE1));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getAdresselinje2()), is(UTLAND_ADRESSELINJE2));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getAdresselinje3()), is(UTLAND_ADRESSELINJE3));
	}

	@Test
	public void mapPersonPostadresseMedMidlertidigGateAdresse() {
		Mottaker mottaker = createMottaker(FNR);
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settPostadresseMedMidlertidigAresseGste(person);
		mapper.map(person, mottaker);
		assertThat(mottaker.getId(), is(FNR));
		assertThat(mottaker.getKortNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat(mottaker.getNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getAdresselinje1()), is(GATENAVN + " " + HUSNR + HUSBOKSTAV));
	}

	@Test
	public void mapPersonPostadresseMedMidlertidigMatrikkelAdresse() {
		Mottaker mottaker = createMottaker(FNR);
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settPostadresseMedMidlertidigAresseMatrikkel(person);
		mapper.map(person, mottaker);
		assertThat(mottaker.getId(), is(FNR));
		assertThat(mottaker.getKortNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat(mottaker.getNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getAdresselinje1()), is(EIENDOMSNAVN));
	}

	@Test
	public void mapPersonBostedadresseMedMidlertidigPostboksAdresse() {
		Mottaker mottaker = createMottaker(FNR);
		Bruker person = createPerson(FORNAVN, MELLOMNAVN, ETTERNAVN);
		settPostadresseMedMidlertidigAressePostboks(person);
		mapper.map(person, mottaker);
		assertThat(mottaker.getId(), is(FNR));
		assertThat(mottaker.getKortNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat(mottaker.getNavn(), is(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getAdresselinje1()), is(POSTBOKS));
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

		Bostedsadresse bostedsadresse = new Bostedsadresse();
		bostedsadresse.setStrukturertAdresse(gateadresse);

		person.setBostedsadresse(bostedsadresse);
	}

	private void settBostedadresseMedMatrikkeladresse(Bruker person) {
		Postadressetyper postadressetyper = new Postadressetyper();
		postadressetyper.setKodeverksRef("BOSTEDSADRESSE");
		postadressetyper.setValue("BOSTEDSADRESSE");
		person.setGjeldendePostadressetype(postadressetyper);

		Matrikkeladresse matrikkeladresse= new Matrikkeladresse();
		matrikkeladresse.setEiendomsnavn(EIENDOMSNAVN);

		Bostedsadresse bostedsadresse = new Bostedsadresse();
		bostedsadresse.setStrukturertAdresse(matrikkeladresse);

		person.setBostedsadresse(bostedsadresse);
	}

	private void settBostedadresseMedPostboksadresse(Bruker person) {
		Postadressetyper postadressetyper = new Postadressetyper();
		postadressetyper.setKodeverksRef("BOSTEDSADRESSE");
		postadressetyper.setValue("BOSTEDSADRESSE");
		person.setGjeldendePostadressetype(postadressetyper);

		PostboksadresseNorsk postboksadresse= new PostboksadresseNorsk();
		postboksadresse.setPostboksnummer(POSTBOKS);

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

		Postadresse postadresse = new Postadresse();
		postadresse.setUstrukturertAdresse(ustrukturertAdresse);

		person.setPostadresse(postadresse);
	}

	private void settPostadresseMedMidlertidigAresseUtland(Bruker person) {
		Postadressetyper postadressetyper = new Postadressetyper();
		postadressetyper.setKodeverksRef("MIDLERTIDIG_POSTADRESSE_UTLAND");
		postadressetyper.setValue("MIDLERTIDIG_POSTADRESSE_UTLAND");
		person.setGjeldendePostadressetype(postadressetyper);

		UstrukturertAdresse ustrukturertAdresse= new UstrukturertAdresse();
		ustrukturertAdresse.setAdresselinje1(UTLAND_ADRESSELINJE1);
		ustrukturertAdresse.setAdresselinje2(UTLAND_ADRESSELINJE2);
		ustrukturertAdresse.setAdresselinje3(UTLAND_ADRESSELINJE3);

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

		MidlertidigPostadresseNorge midlertidigPostadresseNorge = new MidlertidigPostadresseNorge();
		midlertidigPostadresseNorge.setStrukturertAdresse(gateadresse);

		person.setMidlertidigPostadresse(midlertidigPostadresseNorge);
	}

	private void settPostadresseMedMidlertidigAresseMatrikkel(Bruker person) {
		Postadressetyper postadressetyper = new Postadressetyper();
		postadressetyper.setKodeverksRef("MIDLERTIDIG_POSTADRESSE_NORGE");
		postadressetyper.setValue("MIDLERTIDIG_POSTADRESSE_NORGE");
		person.setGjeldendePostadressetype(postadressetyper);

		Matrikkeladresse matrikkeladresse= new Matrikkeladresse();
		matrikkeladresse.setEiendomsnavn(EIENDOMSNAVN);

		MidlertidigPostadresseNorge midlertidigPostadresseNorge = new MidlertidigPostadresseNorge();
		midlertidigPostadresseNorge.setStrukturertAdresse(matrikkeladresse);

		person.setMidlertidigPostadresse(midlertidigPostadresseNorge);
	}

	private void settPostadresseMedMidlertidigAressePostboks(Bruker person) {
		Postadressetyper postadressetyper = new Postadressetyper();
		postadressetyper.setKodeverksRef("MIDLERTIDIG_POSTADRESSE_NORGE");
		postadressetyper.setValue("MIDLERTIDIG_POSTADRESSE_NORGE");
		person.setGjeldendePostadressetype(postadressetyper);

		PostboksadresseNorsk postboksadresse= new PostboksadresseNorsk();
		postboksadresse.setPostboksnummer(POSTBOKS);

		MidlertidigPostadresseNorge midlertidigPostadresseNorge = new MidlertidigPostadresseNorge();
		midlertidigPostadresseNorge.setStrukturertAdresse(postboksadresse);

		person.setMidlertidigPostadresse(midlertidigPostadresseNorge);
	}

	private Mottaker createMottaker(String fnr) {
		Mottaker mottaker = new Mottaker();
		mottaker.setId(fnr);
		mottaker.setTypeKode(AktoerType.PERSON);
		return mottaker;
	}

}
