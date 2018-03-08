package no.nav.regoppslag.consumer.organisasjonv4.support;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import no.nav.dok.metaforcemal.jaxb2.gen.AktoerType;
import no.nav.dok.metaforcemal.jaxb2.gen.Mottaker;
import no.nav.dok.metaforcemal.jaxb2.gen.NorskPostadresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Gateadresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Landkoder;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.NoekkelVerdiAdresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.NoeklerAdresseleddSemistrukturerteAdresser;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.OrganisasjonsDetaljer;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjonsnavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Postnummer;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.SemistrukturertAdresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.StedsadresseNorge;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class OrganisasjonV4MapperTest {

	private OrganisasjonV4Mapper mapper = new OrganisasjonV4Mapper();

	private static final String FNR = "12345678901";
	private static final String ORGNAVN = "Orgnavn 1";
	private static final String ORGNAVN_2 = "Orgnavn_2";
	private static final String ORGKORTNAVN = "OrgKortnavn 1";
	private static final String ORGKORTNAVN_2 = "OrgKortnavn_2";
	private static final String GATENAVN = "Gatenavn";
	private static final int HUSNR = 13;
	private static final String HUSBOKSTAV = "X";
	private static final String SEMIADR1 = "Semistrukturert adresselinje 1";
	private static final String SEMIADR2 = "Semistrukturert adresselinje 2";
	private static final String SEMIADR3 = "Semistrukturert adresselinje 3";
	private static final String POSTNR = "5460";
	private static final String POSTSTED = "Husnes";
	private static final String LAND = "Noreg";

	@Test
	public void simpleMapping() {
		Mottaker mottaker = createMottaker(FNR);
		Organisasjon org = createOrganisasjon(Arrays.asList(ORGNAVN, ORGNAVN_2), Arrays.asList(ORGKORTNAVN, ORGKORTNAVN_2));
		mapper.map(org, mottaker);
		assertThat(mottaker.getId(), is(FNR));
		assertThat(mottaker.getKortNavn(), is(ORGKORTNAVN + " " + ORGKORTNAVN_2));
		assertThat(mottaker.getNavn(), is(ORGNAVN + " " + ORGNAVN_2));
	}

	@Test
	public void mapOrganisasjonSemistrukturertPostadresse() {
		Mottaker mottaker = createMottaker(FNR);
		Organisasjon org = createOrganisasjon(Arrays.asList(ORGNAVN, ORGNAVN_2), Arrays.asList(ORGKORTNAVN, ORGKORTNAVN_2));
		settSemistrukturertAdresse(org, "POSTADRESSE");
		mapper.map(org, mottaker);
		assertThat(mottaker.getId(), is(FNR));
		assertThat(mottaker.getKortNavn(), is(ORGKORTNAVN + " " + ORGKORTNAVN_2));
		assertThat(mottaker.getNavn(), is(ORGNAVN + " " + ORGNAVN_2));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getAdresselinje1()), is(SEMIADR1));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getAdresselinje2()), is(SEMIADR2));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getAdresselinje3()), is(SEMIADR3));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getPostnummer()), is(POSTNR));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getPoststed()), is(POSTSTED));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getLand()), is(LAND));
	}

	@Test
	public void mapOrganisasjonSemistrukturertForretningsadresse() {
		Mottaker mottaker = createMottaker(FNR);
		Organisasjon org = createOrganisasjon(Arrays.asList(ORGNAVN, ORGNAVN_2), Arrays.asList(ORGKORTNAVN, ORGKORTNAVN_2));
		settSemistrukturertAdresse(org, "FORRETNINGSADRESSE");
		mapper.map(org, mottaker);
		assertThat(mottaker.getId(), is(FNR));
		assertThat(mottaker.getKortNavn(), is(ORGKORTNAVN + " " + ORGKORTNAVN_2));
		assertThat(mottaker.getNavn(), is(ORGNAVN + " " + ORGNAVN_2));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getAdresselinje1()), is(SEMIADR1));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getAdresselinje2()), is(SEMIADR2));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getAdresselinje3()), is(SEMIADR3));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getPostnummer()), is(POSTNR));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getPoststed()), is(POSTSTED));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getLand()), is(LAND));
	}

	@Test
	public void mapOrganisasjonStrukturertPostadresse() {
		Mottaker mottaker = createMottaker(FNR);
		Organisasjon org = createOrganisasjon(Arrays.asList(ORGNAVN, ORGNAVN_2), Arrays.asList(ORGKORTNAVN, ORGKORTNAVN_2));
		settStrukturertAdresse(org, "POSTADRESSE");
		mapper.map(org, mottaker);
		assertThat(mottaker.getId(), is(FNR));
		assertThat(mottaker.getKortNavn(), is(ORGKORTNAVN + " " + ORGKORTNAVN_2));
		assertThat(mottaker.getNavn(), is(ORGNAVN + " " + ORGNAVN_2));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getAdresselinje1()), is(GATENAVN + " " + HUSNR + HUSBOKSTAV));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getAdresselinje2()), nullValue());
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getAdresselinje3()), nullValue());
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getPostnummer()), is(POSTNR));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getPoststed()), is(POSTSTED));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getLand()), is(LAND));
	}

	@Test
	public void mapOrganisasjonStrukturertForretningsadresse() {
		Mottaker mottaker = createMottaker(FNR);
		Organisasjon org = createOrganisasjon(Arrays.asList(ORGNAVN, ORGNAVN_2), Arrays.asList(ORGKORTNAVN, ORGKORTNAVN_2));
		settStrukturertAdresse(org, "FORRETNINGSADRESSE");
		mapper.map(org, mottaker);
		assertThat(mottaker.getId(), is(FNR));
		assertThat(mottaker.getKortNavn(), is(ORGKORTNAVN + " " + ORGKORTNAVN_2));
		assertThat(mottaker.getNavn(), is(ORGNAVN + " " + ORGNAVN_2));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getAdresselinje1()), is(GATENAVN + " " + HUSNR + HUSBOKSTAV));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getAdresselinje2()), nullValue());
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getAdresselinje3()), nullValue());
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getPostnummer()), is(POSTNR));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getPoststed()), is(POSTSTED));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getLand()), is(LAND));
	}

	private Organisasjon createOrganisasjon(List<String> orgNavn, List<String> orgKortnavn) {
		Organisasjon organisasjon = new Organisasjon();
		OrganisasjonsDetaljer organisasjonsDetaljer = new OrganisasjonsDetaljer();
		UstrukturertNavn organisasjonKortnavn = new UstrukturertNavn();
		organisasjonKortnavn.getNavnelinje().addAll(orgKortnavn);
		organisasjon.setNavn(organisasjonKortnavn);

		UstrukturertNavn orgDetNavn = new UstrukturertNavn();
		orgDetNavn.getNavnelinje().addAll(orgNavn);
		Organisasjonsnavn organisasjonsnavn = new Organisasjonsnavn();
		organisasjonsnavn.setNavn(orgDetNavn);
		organisasjonsDetaljer.getNavn().add(organisasjonsnavn);
		organisasjon.setOrganisasjonDetaljer(organisasjonsDetaljer);

		return organisasjon;
	}

	private void settSemistrukturertAdresse(Organisasjon org, String adressetype) {
		SemistrukturertAdresse semistrukturertAdresse = new SemistrukturertAdresse();

		//Adresselinje1
		NoekkelVerdiAdresse noekkelVerdiAdresse = new NoekkelVerdiAdresse();
		NoeklerAdresseleddSemistrukturerteAdresser noekkel = new NoeklerAdresseleddSemistrukturerteAdresser();
		noekkel.setKodeverksRef("adresselinje1");
		noekkelVerdiAdresse.setNoekkel(noekkel);
		noekkelVerdiAdresse.setVerdi(SEMIADR1);
		semistrukturertAdresse.getAdresseledd().add(noekkelVerdiAdresse);

		//Adresselinje2
		noekkelVerdiAdresse = new NoekkelVerdiAdresse();
		noekkel = new NoeklerAdresseleddSemistrukturerteAdresser();
		noekkel.setKodeverksRef("adresselinje2");
		noekkelVerdiAdresse.setNoekkel(noekkel);
		noekkelVerdiAdresse.setVerdi(SEMIADR2);
		semistrukturertAdresse.getAdresseledd().add(noekkelVerdiAdresse);

		//Adresselinje3
		noekkelVerdiAdresse = new NoekkelVerdiAdresse();
		noekkel = new NoeklerAdresseleddSemistrukturerteAdresser();
		noekkel.setKodeverksRef("adresselinje3split1");
		noekkelVerdiAdresse.setNoekkel(noekkel);
		noekkelVerdiAdresse.setVerdi(SEMIADR3);
		semistrukturertAdresse.getAdresseledd().add(noekkelVerdiAdresse);

		//TODO adresselinje4

		//Postnr
		noekkelVerdiAdresse = new NoekkelVerdiAdresse();
		noekkel = new NoeklerAdresseleddSemistrukturerteAdresser();
		noekkel.setKodeverksRef("postnr");
		noekkelVerdiAdresse.setNoekkel(noekkel);
		noekkelVerdiAdresse.setVerdi(POSTNR);
		semistrukturertAdresse.getAdresseledd().add(noekkelVerdiAdresse);

		//Poststed
		noekkelVerdiAdresse = new NoekkelVerdiAdresse();
		noekkel = new NoeklerAdresseleddSemistrukturerteAdresser();
		noekkel.setKodeverksRef("poststed");
		noekkelVerdiAdresse.setNoekkel(noekkel);
		noekkelVerdiAdresse.setVerdi(POSTSTED);
		semistrukturertAdresse.getAdresseledd().add(noekkelVerdiAdresse);

		Landkoder landkoder = new Landkoder();
		landkoder.setKodeRef(LAND);
		landkoder.setKodeverksRef(LAND);
		landkoder.setValue(LAND);
		semistrukturertAdresse.setLandkode(landkoder);

		OrganisasjonsDetaljer orgdet = org.getOrganisasjonDetaljer();

		if ("POSTADRESSE".equals(adressetype)) {
			orgdet.getPostadresse().add(semistrukturertAdresse);
		} else {
			orgdet.getForretningsadresse().add(semistrukturertAdresse);
		}
		org.setOrganisasjonDetaljer(orgdet);
	}

	private void settStrukturertAdresse(Organisasjon org, String adressetype) {

		Gateadresse gateadresse = new Gateadresse();
		gateadresse.setGatenavn(GATENAVN);
		gateadresse.setHusnummer(HUSNR);
		gateadresse.setHusbokstav(HUSBOKSTAV);


		Postnummer postnummer = new Postnummer();
		postnummer.setKodeverksRef(POSTNR);
		postnummer.setKodeRef(POSTSTED);
		postnummer.setValue(POSTSTED);
		StedsadresseNorge stedsadresseNorge = gateadresse;
		stedsadresseNorge.setPoststed(postnummer);

		Landkoder landkoder = new Landkoder();
		landkoder.setKodeRef(LAND);
		landkoder.setKodeverksRef(LAND);
		landkoder.setValue(LAND);
		stedsadresseNorge.setLandkode(landkoder);

		OrganisasjonsDetaljer orgdet = org.getOrganisasjonDetaljer();
		if ("POSTADRESSE".equals(adressetype)) {
			orgdet.getPostadresse().add(stedsadresseNorge);
		} else {
			orgdet.getForretningsadresse().add(stedsadresseNorge);
		}
		org.setOrganisasjonDetaljer(orgdet);
	}

	private Mottaker createMottaker(String fnr) {
		Mottaker mottaker = new Mottaker();
		mottaker.setId(fnr);
		mottaker.setTypeKode(AktoerType.ORGANISASJON);
		return mottaker;
	}
}
