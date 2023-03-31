package no.nav.regoppslag.util;

import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.dok.brevdata.felles.v1.navfelles.Person;
import no.nav.dok.brevdata.felles.v1.navfelles.UtenlandskPostadresse;
import no.nav.regoppslag.consumer.ereg.MottakerTo;
import no.nav.regoppslag.consumer.ereg.support.Bruksperiode;
import no.nav.regoppslag.consumer.ereg.support.Gyldighetsperiode;
import no.nav.regoppslag.consumer.ereg.support.Navn;
import no.nav.regoppslag.consumer.ereg.support.Organisasjon;
import no.nav.regoppslag.consumer.ereg.support.OrganisasjonDetaljer;
import no.nav.regoppslag.consumer.ereg.support.Postadresse;
import no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.util.Collections.singletonList;

public class TestDataUtil {

	public static final String LANDKODE = "NO";
	public static final String LANDNAVN = "Norge";
	public static final String UTENLANDSK_ADRESSELINJE1 = "Dammgatan 14";
	public static final String UTENLANDSK_ADRESSELINJE2 = "SE 567 31 VAGGERYD";
	public static final String UTENLANDSK_ADRESSELINJE3 = "SWEDEN";
	public static final String SVENSK_LANDKODE = "SE";
	public static final String SVENSK_LAND = "Sweden";
	public static final String SVERIGE = "SVERIGE";
	public static final String POSTNUMMER = "3000";
	public static final String POSTSTED = "HUSNES";
	public static final String GATENAVN = "Gatenavn";
	public static final int HUSNR = 13;
	public static final String HUSBOKSTAV = "X";
	public static final String ADRESSELINJE1 = GATENAVN + " " + HUSNR + HUSBOKSTAV;
	public static final String ADRESSELINJE2 = null;
	public static final String ADRESSELINJE3 = null;
	public static final String POSTNR = "5460";
	public static final String MAALFORM = "NO";

	public static MottakerTo createMottaker(AdresseKildeCode adresseKilde) {

		return MottakerTo.builder()
				.adresseKilde(adresseKilde)
				.mottaker(createMottaker(true)).build();
	}

	public static Mottaker createMottaker(boolean withNorskPostedAdresse) {
		Person person = new Person();
		if (!withNorskPostedAdresse) {
			person.setMottakeradresse(createUtenlandsPostadresse());

		} else {
			person.setMottakeradresse(createNorskPostadresse());
		}
		return person;
	}

	public static NorskPostadresse createNorskPostadresse() {
		NorskPostadresse norskPostadresse = new NorskPostadresse();
		norskPostadresse.setAdresselinje1(ADRESSELINJE1);
		norskPostadresse.setAdresselinje2(ADRESSELINJE2);
		norskPostadresse.setAdresselinje3(ADRESSELINJE3);
		norskPostadresse.setLand(LANDNAVN);
		norskPostadresse.setPostnummer(POSTNUMMER);
		norskPostadresse.setPoststed(POSTSTED);
		return norskPostadresse;
	}

	public static UtenlandskPostadresse createUtenlandsPostadresse() {
		UtenlandskPostadresse utenlandskPostadresse = new UtenlandskPostadresse();
		utenlandskPostadresse.setAdresselinje1(UTENLANDSK_ADRESSELINJE1);
		utenlandskPostadresse.setAdresselinje2(UTENLANDSK_ADRESSELINJE2);
		utenlandskPostadresse.setAdresselinje3(UTENLANDSK_ADRESSELINJE3);
		utenlandskPostadresse.setLand(SVENSK_LAND);
		return utenlandskPostadresse;
	}

	public static Organisasjon createOrganisasjon(String navn) {
		Organisasjon organisasjon = new Organisasjon();
		OrganisasjonDetaljer organisasjonsDetaljer = new OrganisasjonDetaljer();
		Navn organisasjonKortnavn = new Navn();
		organisasjonKortnavn.setNavnelinje1(navn);
		organisasjonKortnavn.setSammensattnavn(navn);
		organisasjon.setNavn(organisasjonKortnavn);

		Navn organisasjonsnavn = new Navn();
		organisasjonsnavn.setNavnelinje1(navn);
		organisasjonsnavn.setSammensattnavn(navn);
		Bruksperiode bruksperiode = new Bruksperiode();
		bruksperiode.setFom(LocalDateTime.now().minusDays(1));
		organisasjonsnavn.setBruksperiode(bruksperiode);
		Gyldighetsperiode gyldighetsperiode = new Gyldighetsperiode();
		gyldighetsperiode.setFom(LocalDate.now().minusDays(1));
		organisasjonsnavn.setGyldighetsperiode(gyldighetsperiode);
		organisasjonsDetaljer.setNavn(singletonList(organisasjonsnavn));

		organisasjonsDetaljer.setMaalform(MAALFORM);
		organisasjonsDetaljer.setOpphoersdato(LocalDate.now().plusDays(10));
		organisasjon.setOrganisasjonDetaljer(organisasjonsDetaljer);

		return organisasjon;
	}

	public static void settPostAdresse(Organisasjon org, String adressetype, Long validSeconds) {

		Postadresse postadresse = new Postadresse();
		postadresse.setAdresselinje1(GATENAVN + " " + HUSNR + HUSBOKSTAV);
		setFomTomPerioder(postadresse, validSeconds);

		postadresse.setPostnummer(POSTNR);
		postadresse.setPoststed(POSTSTED);

		postadresse.setLandkode(LANDKODE);

		OrganisasjonDetaljer orgdet = org.getOrganisasjonDetaljer();
		if ("POSTADRESSE".equals(adressetype)) {
			orgdet.setPostadresser(singletonList(postadresse));
		} else {
			orgdet.setForretningsadresser(singletonList(postadresse));
		}
		org.setOrganisasjonDetaljer(orgdet);
	}

	public static void settUtlandskPostadresse(Organisasjon org) {
		Postadresse postadresse = new Postadresse();
		setFomTomPerioder(postadresse, 10000L);

		postadresse.setAdresselinje1(UTENLANDSK_ADRESSELINJE1);
		postadresse.setAdresselinje2(UTENLANDSK_ADRESSELINJE2);
		postadresse.setAdresselinje3(UTENLANDSK_ADRESSELINJE3);
		postadresse.setLandkode(SVENSK_LANDKODE);


		OrganisasjonDetaljer organisasjonDetaljer = org.getOrganisasjonDetaljer();
		organisasjonDetaljer.setPostadresser(singletonList(postadresse));

		org.setOrganisasjonDetaljer(organisasjonDetaljer);
	}

	public static void settUtlandskPostadresseMedPoststed(Organisasjon org) {
		Postadresse postadresse = new Postadresse();
		setFomTomPerioder(postadresse, 10000L);

		postadresse.setAdresselinje1(UTENLANDSK_ADRESSELINJE1);
		postadresse.setAdresselinje2(UTENLANDSK_ADRESSELINJE2);
		postadresse.setPoststed(POSTSTED);
		postadresse.setLandkode(SVENSK_LANDKODE);


		OrganisasjonDetaljer organisasjonDetaljer = org.getOrganisasjonDetaljer();
		organisasjonDetaljer.setPostadresser(singletonList(postadresse));

		org.setOrganisasjonDetaljer(organisasjonDetaljer);
	}

	public static void settUtlandskPostadresseMedAlleAdresselinjerOgPoststed(Organisasjon org) {
		Postadresse postadresse = new Postadresse();
		setFomTomPerioder(postadresse, 10000L);

		postadresse.setAdresselinje1(UTENLANDSK_ADRESSELINJE1);
		postadresse.setAdresselinje2(UTENLANDSK_ADRESSELINJE2);
		postadresse.setAdresselinje3(UTENLANDSK_ADRESSELINJE3);
		postadresse.setPoststed(POSTSTED);
		postadresse.setLandkode(SVENSK_LANDKODE);

		OrganisasjonDetaljer organisasjonDetaljer = org.getOrganisasjonDetaljer();
		organisasjonDetaljer.setPostadresser(singletonList(postadresse));

		org.setOrganisasjonDetaljer(organisasjonDetaljer);
	}

	private static void setFomTomPerioder(Postadresse objekt, Long validSeconds) {

		Gyldighetsperiode gyldighetsperiode = new Gyldighetsperiode();
		gyldighetsperiode.setFom(LocalDate.now().minusDays(validSeconds / (60 * 60)));
		gyldighetsperiode.setTom(LocalDate.now().plusDays(validSeconds / (60 * 60)));
		Bruksperiode bruksperiode = new Bruksperiode();
		bruksperiode.setFom(LocalDateTime.now().minusSeconds(validSeconds));
		bruksperiode.setTom(LocalDateTime.now().plusSeconds(validSeconds));

		objekt.setBruksperiode(bruksperiode);
		objekt.setGyldighetsperiode(gyldighetsperiode);
	}

	public static void settKunForretningsadresse(Organisasjon org) {
		Postadresse postadresse = new Postadresse();
		postadresse.setAdresselinje1(GATENAVN + " " + HUSNR + HUSBOKSTAV);
		setFomTomPerioder(postadresse, 10000L);

		postadresse.setPostnummer(POSTNR);
		postadresse.setPoststed(POSTSTED);

		postadresse.setLandkode(LANDKODE);

		OrganisasjonDetaljer orgdet = org.getOrganisasjonDetaljer();

		orgdet.setForretningsadresser(singletonList(postadresse));
		org.setOrganisasjonDetaljer(orgdet);
	}

}
