package no.nav.regoppslag.consumer.organisasjonv4.support;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import no.nav.dok.metaforcemal.jaxb2.gen.AktoerType;
import no.nav.dok.metaforcemal.jaxb2.gen.Mottaker;
import no.nav.dok.metaforcemal.jaxb2.gen.NorskPostadresse;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.service.LandkodeService;
import no.nav.regoppslag.service.PostnummerService;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Gateadresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.GeografiskAdresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Landkoder;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Maalformer;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.NoekkelVerdiAdresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.NoeklerAdresseleddSemistrukturerteAdresser;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.OrganisasjonsDetaljer;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjonsnavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Postnummer;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.SemistrukturertAdresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.StedsadresseNorge;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.StrukturertAdresse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.List;

public class OrganisasjonV4MapperTest {

	private PostnummerService postnummerService = new PostnummerService();
	private LandkodeService landkodeService= new LandkodeService();
	private OrganisasjonV4Mapper mapper;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public	void initPostnummer() throws Exception {
		landkodeService.init();
		postnummerService.init();
		mapper = new OrganisasjonV4Mapper(postnummerService, landkodeService);
	}

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
	private static final String SEMIADR4 = "Semistrukturert adresselinje 4";
	private static final String POSTNR = "5460";
	private static final String POSTSTED = "HUSNES";
	private static final String LANDKODE = "NOR";
	private static final String LAND = "NORWAY";
	private static final String MAALFORM = "NO";


	@Test
	public void simpleMapping() throws Exception {
		Mottaker mottaker = createMottaker(FNR);
		Organisasjon org = createOrganisasjon(Arrays.asList(ORGNAVN, ORGNAVN_2), Arrays.asList(ORGKORTNAVN, ORGKORTNAVN_2));
		mapper.map(org, mottaker);
		assertThat(mottaker.getId(), is(FNR));
		assertThat(mottaker.getKortNavn(), is(ORGKORTNAVN + " " + ORGKORTNAVN_2));
		assertThat(mottaker.getNavn(), is(ORGNAVN + " " + ORGNAVN_2));
	}

	@Test
	public void mapOrganisasjonSemistrukturertPostadresse() throws Exception {
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
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getAdresselinje4()), is(SEMIADR4));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getPostnummer()), is(POSTNR));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getPoststed()), is(POSTSTED));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getLand()), is(LAND));
	}

	@Test
	public void mapOrganisasjonSemistrukturertForretningsadresse() throws Exception {
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
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getAdresselinje4()), is(SEMIADR4));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getPostnummer()), is(POSTNR));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getPoststed()), is(POSTSTED));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getLand()), is(LAND));
	}

	@Test
	public void mapOrganisasjonStrukturertPostadresse() throws Exception {
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
	public void mapOrganisasjonStrukturertForretningsadresse() throws Exception {
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
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getAdresselinje4()), nullValue());
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getPostnummer()), is(POSTNR));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getPoststed()), is(POSTSTED));
		assertThat((((NorskPostadresse) mottaker.getAdresse()).getLand()), is(LAND));
	}

	@Test
	public void mapPersonPostadresseUtenPostnr() throws Exception {
		thrown.expect(RegOppslagFunctionalException.class);
		thrown.expectMessage("Mottaker orgoppslag - mangler postnummer for organisasjon:");
		Mottaker mottaker = createMottaker(FNR);
		Organisasjon org = createOrganisasjon(Arrays.asList(ORGNAVN, ORGNAVN_2), Arrays.asList(ORGKORTNAVN, ORGKORTNAVN_2));
		settStrukturertAdresse(org, "POSTADRESSE");
		((StedsadresseNorge) org.getOrganisasjonDetaljer().getPostadresse().get(0)).setPoststed(new Postnummer());
		mapper.map(org, mottaker);
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
		Maalformer maalformer = new Maalformer();
		maalformer.setKodeRef(MAALFORM);
		maalformer.setValue(MAALFORM);
		organisasjonsDetaljer.setGjeldendeMaalform(maalformer);
		organisasjon.setOrganisasjonDetaljer(organisasjonsDetaljer);

		return organisasjon;
	}

	private void settSemistrukturertAdresse(Organisasjon org, String adressetype) {
		SemistrukturertAdresse semistrukturertAdresse = new SemistrukturertAdresse();

		//Adresselinje1
		NoekkelVerdiAdresse noekkelVerdiAdresse = new NoekkelVerdiAdresse();
		NoeklerAdresseleddSemistrukturerteAdresser noekkel = new NoeklerAdresseleddSemistrukturerteAdresser();
		noekkel.setKodeRef("adresselinje1");
		noekkelVerdiAdresse.setNoekkel(noekkel);
		noekkelVerdiAdresse.setVerdi(SEMIADR1);
		semistrukturertAdresse.getAdresseledd().add(noekkelVerdiAdresse);

		//Adresselinje2
		noekkelVerdiAdresse = new NoekkelVerdiAdresse();
		noekkel = new NoeklerAdresseleddSemistrukturerteAdresser();
		noekkel.setKodeRef("adresselinje2");
		noekkelVerdiAdresse.setNoekkel(noekkel);
		noekkelVerdiAdresse.setVerdi(SEMIADR2);
		semistrukturertAdresse.getAdresseledd().add(noekkelVerdiAdresse);

		//Adresselinje3
		noekkelVerdiAdresse = new NoekkelVerdiAdresse();
		noekkel = new NoeklerAdresseleddSemistrukturerteAdresser();
		noekkel.setKodeRef("Adresse 3 split 1");
		noekkelVerdiAdresse.setNoekkel(noekkel);
		noekkelVerdiAdresse.setVerdi(SEMIADR3);
		semistrukturertAdresse.getAdresseledd().add(noekkelVerdiAdresse);

		//Adresselinje4
		noekkelVerdiAdresse = new NoekkelVerdiAdresse();
		noekkel = new NoeklerAdresseleddSemistrukturerteAdresser();
		noekkel.setKodeRef("Adresse 3 split 2");
		noekkelVerdiAdresse.setNoekkel(noekkel);
		noekkelVerdiAdresse.setVerdi(SEMIADR4);
		semistrukturertAdresse.getAdresseledd().add(noekkelVerdiAdresse);

		//Postnr
		noekkelVerdiAdresse = new NoekkelVerdiAdresse();
		noekkel = new NoeklerAdresseleddSemistrukturerteAdresser();
		noekkel.setKodeRef("postnr");
		noekkelVerdiAdresse.setNoekkel(noekkel);
		noekkelVerdiAdresse.setVerdi(POSTNR);
		semistrukturertAdresse.getAdresseledd().add(noekkelVerdiAdresse);

		//Poststed
		noekkelVerdiAdresse = new NoekkelVerdiAdresse();
		noekkel = new NoeklerAdresseleddSemistrukturerteAdresser();
		noekkel.setKodeRef("poststed");
		noekkelVerdiAdresse.setNoekkel(noekkel);
		noekkelVerdiAdresse.setVerdi(POSTSTED);
		semistrukturertAdresse.getAdresseledd().add(noekkelVerdiAdresse);


		Landkoder landkoder = new Landkoder();
		landkoder.setKodeRef(LANDKODE);
		landkoder.setValue(LANDKODE);
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
		postnummer.setKodeRef(POSTNR);
		postnummer.setValue(POSTSTED);
		StedsadresseNorge stedsadresseNorge = gateadresse;
		stedsadresseNorge.setPoststed(postnummer);

		Landkoder landkoder = new Landkoder();
		landkoder.setKodeRef(LANDKODE);
		landkoder.setValue(LANDKODE);
		stedsadresseNorge.setLandkode(landkoder);

		OrganisasjonsDetaljer orgdet = org.getOrganisasjonDetaljer();
		if ("POSTADRESSE".equals(adressetype)) {
			orgdet.getPostadresse().add(stedsadresseNorge);
		} else {
			orgdet.getForretningsadresse().add(stedsadresseNorge);
		}
		org.setOrganisasjonDetaljer(orgdet);
	}

	private Mottaker createMottaker(String orgnr) {
		Mottaker mottaker = new Mottaker();
		mottaker.setId(orgnr);
		mottaker.setTypeKode(AktoerType.ORGANISASJON);
		return mottaker;
	}
}
