package no.nav.regoppslag.util;

import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.dok.brevdata.felles.v1.navfelles.Person;
import no.nav.dok.brevdata.felles.v1.navfelles.UtenlandskPostadresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Adresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Gateadresse;
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

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class TestDataUtil {


    public static final String ADRESSELINJE1 = "adresselinje1";
    public static final String ADRESSELINJE2 = "adresselinje2";
    public static final String ADRESSELINJE3 = "adresselinje3";
    public static final String ADRESSELINJE_POSTSTED = "poststed";

    public static final String LANDKODE = "NO";
    public static final String LANDNAVN = "NORGE";
    public static final String UTENLANDSK_ADRESSELINJE1 = "Dammgatan 14";
    public static final String UTENLANDSK_ADRESSELINJE2 = "SE 567 31 VAGGERYD";
    public static final String UTENLANDSK_ADRESSELINJE3 = "SWEDEN";
    public static final String SVENSK_LANDKODE = "SE";
    public static final String SVENSK_LAND = "SVERIGE";
    public static final String POSTNUMMER = "3000";
    public static final String POSTSTED = "HER";
    public static final String GATENAVN = "Gatenavn";
    public static final int HUSNR = 13;
    public static final String HUSBOKSTAV = "X";
    public static final String POSTNR = "5460";
    public static final String MAALFORM = "NO";
    public static final String SEMIADR1 = "Semistrukturert adresselinje 1";
    public static final String SEMIADR2 = "Semistrukturert adresselinje 2";
    public static final String SEMIADR3 = "Semistrukturert adresselinje 3";
    public static final String SEMIADR4 = "Semistrukturert adresselinje 4";

    public static Mottaker createMottaker() {
        return createMottaker(true);
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
        norskPostadresse.setLand(LANDKODE);
        norskPostadresse.setPostnummer(POSTNUMMER);
        norskPostadresse.setPoststed(POSTSTED);
        return norskPostadresse;
    }

    public static UtenlandskPostadresse createUtenlandsPostadresse() {
        UtenlandskPostadresse utenlandskPostadresse = new UtenlandskPostadresse();
        utenlandskPostadresse.setAdresselinje1(UTENLANDSK_ADRESSELINJE1);
        utenlandskPostadresse.setAdresselinje2(UTENLANDSK_ADRESSELINJE2);
        utenlandskPostadresse.setAdresselinje3(UTENLANDSK_ADRESSELINJE3);
        utenlandskPostadresse.setLand(SVENSK_LANDKODE);
        return utenlandskPostadresse;
    }

    public static Organisasjon createOrganisasjon(List<String> orgNavn, List<String> orgKortnavn) throws DatatypeConfigurationException {
        Organisasjon organisasjon = new Organisasjon();
        OrganisasjonsDetaljer organisasjonsDetaljer = new OrganisasjonsDetaljer();
        UstrukturertNavn organisasjonKortnavn = new UstrukturertNavn();
        organisasjonKortnavn.getNavnelinje().addAll(orgKortnavn);
        organisasjon.setNavn(organisasjonKortnavn);

        UstrukturertNavn orgDetNavn = new UstrukturertNavn();
        orgDetNavn.getNavnelinje().addAll(orgNavn);
        Organisasjonsnavn organisasjonsnavn = new Organisasjonsnavn();
        organisasjonsnavn.setNavn(orgDetNavn);
        organisasjonsnavn.setFomBruksperiode(dateToGregorian(LocalDate.now().minusDays(1)));
        organisasjonsnavn.setFomGyldighetsperiode(dateToGregorian(LocalDate.now().minusDays(1)));
        organisasjonsDetaljer.getNavn().add(organisasjonsnavn);
        Maalformer maalformer = new Maalformer();
        maalformer.setKodeRef(MAALFORM);
        maalformer.setValue(MAALFORM);
        organisasjonsDetaljer.setGjeldendeMaalform(maalformer);
        organisasjonsDetaljer.setOpphoersdato(dateToGregorian(Date.from(Instant.now().plusSeconds(50000))));
        organisasjon.setOrganisasjonDetaljer(organisasjonsDetaljer);

        return organisasjon;
    }

    public static void settStrukturertAdresse(Organisasjon org, String adressetype) throws DatatypeConfigurationException {

        Gateadresse gateadresse = new Gateadresse();
        gateadresse.setGatenavn(GATENAVN);
        gateadresse.setHusnummer(HUSNR);
        gateadresse.setHusbokstav(HUSBOKSTAV);

        gateadresse.setFomGyldighetsperiode(dateToGregorian(Date.from(Instant.now().minusSeconds(10000))));
        gateadresse.setTomGyldighetsperiode(dateToGregorian(Date.from(Instant.now().plusSeconds(10000))));

        gateadresse.setFomBruksperiode(dateToGregorian(Date.from(Instant.now().minusSeconds(10000))));
        gateadresse.setTomBruksperiode(dateToGregorian(Date.from(Instant.now().plusSeconds(10000))));

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

    public static void settUtlandskPostadresse(Organisasjon org) throws DatatypeConfigurationException {
        SemistrukturertAdresse semistrukturertAdresse = new SemistrukturertAdresse();
        setFomTomPerioder(semistrukturertAdresse);

        UtenlandskPostadresse utenlandskPostadresse = createUtenlandsPostadresse();

        semistrukturertAdresse.getAdresseledd().add(createSemistrukturertAdresselinje(ADRESSELINJE1, utenlandskPostadresse.getAdresselinje1()));
        semistrukturertAdresse.getAdresseledd().add(createSemistrukturertAdresselinje(ADRESSELINJE2, utenlandskPostadresse.getAdresselinje2()));
        semistrukturertAdresse.getAdresseledd().add(createSemistrukturertAdresselinje(ADRESSELINJE3, utenlandskPostadresse.getAdresselinje3()));
        semistrukturertAdresse.setLandkode(createLandkodeRef(utenlandskPostadresse.getLand()));

        OrganisasjonsDetaljer organisasjonsDetaljer = org.getOrganisasjonDetaljer();
        organisasjonsDetaljer.getPostadresse().add(semistrukturertAdresse);

        org.setOrganisasjonDetaljer(organisasjonsDetaljer);
    }

    public static void settUtlandskPostadresseMedPoststed(Organisasjon org) throws DatatypeConfigurationException {
        SemistrukturertAdresse semistrukturertAdresse = new SemistrukturertAdresse();
        setFomTomPerioder(semistrukturertAdresse);

        UtenlandskPostadresse utenlandskPostadresse = createUtenlandsPostadresse();

        semistrukturertAdresse.getAdresseledd().add(createSemistrukturertAdresselinje(ADRESSELINJE1, utenlandskPostadresse.getAdresselinje1()));
        semistrukturertAdresse.getAdresseledd().add(createSemistrukturertAdresselinje(ADRESSELINJE_POSTSTED, utenlandskPostadresse.getAdresselinje2()));
        semistrukturertAdresse.setLandkode(createLandkodeRef(utenlandskPostadresse.getLand()));

        OrganisasjonsDetaljer organisasjonsDetaljer = org.getOrganisasjonDetaljer();
        organisasjonsDetaljer.getPostadresse().add(semistrukturertAdresse);

        org.setOrganisasjonDetaljer(organisasjonsDetaljer);
    }

    private static void setFomTomPerioder(Adresse objekt) throws DatatypeConfigurationException {
        objekt.setFomGyldighetsperiode(dateToGregorian(Date.from(Instant.now().minusSeconds(10000))));
        objekt.setTomGyldighetsperiode(dateToGregorian(Date.from(Instant.now().plusSeconds(10000))));
        objekt.setFomBruksperiode(dateToGregorian(Date.from(Instant.now().minusSeconds(10000))));
        objekt.setTomBruksperiode(dateToGregorian(Date.from(Instant.now().plusSeconds(10000))));
    }

    private static NoekkelVerdiAdresse createSemistrukturertAdresselinje(String noekkelVerdi, String verdi) {
        NoekkelVerdiAdresse noekkelVerdiAdresse = new NoekkelVerdiAdresse();

        NoeklerAdresseleddSemistrukturerteAdresser noekkel = new NoeklerAdresseleddSemistrukturerteAdresser();
        noekkel.setKodeRef(noekkelVerdi);

        noekkelVerdiAdresse.setNoekkel(noekkel);
        noekkelVerdiAdresse.setVerdi(verdi);
        return noekkelVerdiAdresse;
    }

    private static Landkoder createLandkodeRef(String landkodeString) {
        Landkoder landkode = new Landkoder();
        landkode.setKodeRef(landkodeString);
        return landkode;
    }

    public static void settKunForretningsadresse(Organisasjon org) throws DatatypeConfigurationException {
        Gateadresse gateadresse = new Gateadresse();
        gateadresse.setGatenavn(GATENAVN);
        gateadresse.setHusnummer(HUSNR);
        gateadresse.setHusbokstav(HUSBOKSTAV);

        gateadresse.setFomGyldighetsperiode(dateToGregorian(Date.from(Instant.now().minusSeconds(10000))));
        gateadresse.setFomBruksperiode(dateToGregorian(Date.from(Instant.now().minusSeconds(10000))));

        Postnummer postnummer = new Postnummer();
        postnummer.setKodeRef(POSTNR);
        postnummer.setValue(POSTSTED);
        gateadresse.setPoststed(postnummer);

        Landkoder landkoder = new Landkoder();
        landkoder.setKodeRef(LANDKODE);
        landkoder.setValue(LANDKODE);
        gateadresse.setLandkode(landkoder);

        OrganisasjonsDetaljer orgdet = org.getOrganisasjonDetaljer();
        SemistrukturertAdresse semistrukturertAdresse = new SemistrukturertAdresse();
        semistrukturertAdresse.setFomGyldighetsperiode(dateToGregorian(Date.from(Instant.now().minusSeconds(10000))));
        semistrukturertAdresse.setFomBruksperiode(dateToGregorian(Date.from(Instant.now().minusSeconds(10000))));
        orgdet.getPostadresse().add(semistrukturertAdresse);
        orgdet.getForretningsadresse().add(gateadresse);
    }

    public static void settSemistrukturertAdresse(Organisasjon org, String adressetype, long validSeconds) throws DatatypeConfigurationException {
        SemistrukturertAdresse semistrukturertAdresse = new SemistrukturertAdresse();

        semistrukturertAdresse.setFomGyldighetsperiode(dateToGregorian(Date.from(Instant.now().minusSeconds(validSeconds))));
        semistrukturertAdresse.setTomGyldighetsperiode(dateToGregorian(Date.from(Instant.now().plusSeconds(validSeconds))));

        semistrukturertAdresse.setFomBruksperiode(dateToGregorian(Date.from(Instant.now().minusSeconds(validSeconds))));
        semistrukturertAdresse.setTomBruksperiode(dateToGregorian(Date.from(Instant.now().plusSeconds(validSeconds))));

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

    public static XMLGregorianCalendar dateToGregorian(Date date) throws DatatypeConfigurationException {
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(date);
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
    }

    public static XMLGregorianCalendar dateToGregorian(LocalDate localDate) throws DatatypeConfigurationException {
        GregorianCalendar gregorianCalendar = GregorianCalendar.from(localDate.atStartOfDay(ZoneId.systemDefault()));
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
    }
}
