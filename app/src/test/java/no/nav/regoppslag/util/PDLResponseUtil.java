package no.nav.regoppslag.util;

import no.nav.regoppslag.consumer.pdl.pdlresponse.Bostedsadresse;
import no.nav.regoppslag.consumer.pdl.pdlresponse.HentPerson;
import no.nav.regoppslag.consumer.pdl.pdlresponse.Kontaktadresse;
import no.nav.regoppslag.consumer.pdl.pdlresponse.KontaktinformasjonForDoedsbo;
import no.nav.regoppslag.consumer.pdl.pdlresponse.Matrikkeladresse;
import no.nav.regoppslag.consumer.pdl.pdlresponse.PDLConstant;
import no.nav.regoppslag.consumer.pdl.pdlresponse.PDLHentPersonResponse;
import no.nav.regoppslag.consumer.pdl.pdlresponse.UtenlandskAdresse;
import no.nav.regoppslag.consumer.pdl.pdlresponse.Vegadresse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;

import static no.nav.regoppslag.consumer.pdl.pdlresponse.PDLConstant.PERSONSTATUS_BOSATT;
import static no.nav.regoppslag.consumer.pdl.pdlresponse.PDLConstant.POSTADRESSE_INNLAND;
import static no.nav.regoppslag.consumer.pdl.pdlresponse.PDLConstant.POSTADRESSE_UTLAND;


public class PDLResponseUtil {

	public static final String UTELAND_NAVN = "Herr H. C. Andersen";
	public static final String UTENLANDSK_POSTKODE = "Ottawa ON K1A 0B1";
	public static final String UTENLANDSK_BYSTED = "Ottawa";
	public static final String  UTENLANDSK_POSTBOKSNUMMERNAVN = "2701, promenade Riverside";
	public static final String  UTENLANDSK_LANDKODE = "CA";

	public static final String ADVOKAT_FORNAVN = "Herr";
	public static final String ADVOKAT_MELLOMNAVN = "H. C.";
	public static final String ADVOKAT_ETTERNAVN = "Andersen";

	public static final String HUSNUMMER_1 = "45";
	public static final String ADRESSENAVN_1 = "Finnesveien 45";
	public static final String KOMMUNENUMMER_1 = "5432";

	public static final int FOEDSELSAAR = 1984;
	public static final LocalDate FOEDSELDATO = LocalDate.of(1984, Month.APRIL, 24);
	public static final String FORKOORETNAVN = "GYNGEHEST ÅPENHJERTIG";
	public static final String FULTTNAVN = "GYNGEHEST A. ÅPENHJERTIG";
	public static final String FORNAVN = "GYNGEHEST";
	public static final String MELLOMNAVN = "A.";
	public static final String ETTERNAVN = "ÅPENHJERTIG";
	public static final Long MATRIKKEL_ID = 123456789L;
	public static final String HUSNUMMER = "12";
	public static final String HUSBOKSTAV = "B";
	public static final String BRUKSENETSNUMMER = "H0101";
	public static final String ADRESSENAVN = "Kirkegata";
	public static final String KOMMUNENUMMER = "4321";
	public static final String BYDERLSNUMMER = "030110";
	public static final String TILLEGGSNAVN = "Storgården";
	public static final String POSTNUMMER = "7320";
	public static final String FRITTFORMAT_ADRESSELINJE1 = "C/O Kari Hansen";
	public static final String FRITTFORMAT_ADRESSELINJE2 = "Kirkegata 12";
	public static final String FRITTFORMAT_POSTNUMMER = "9550";
	public static final String POSTSTED = "FANNREM";
	public static final String POSTBOKSEIER = "Byggfirma A/S";
	public static final String POSTBOKS = "Postboks 7320";
	public static final String ADRESSENAVN_NUMMER = "";
	public static final String POSTBOKSNUMMERNAVN = "P.O.Box 7320 Place";
	public static final String POSTKODE = "SE-12345";
	public static final String BYSTED = "Haworth";
	public static final String REGION_DISTRIKTOMRAADE = "Yorkshire";
	public static final String LANDKODE_UTENLANDSK = "SWE";
	public static final String LANDKODE_NORGE = "NO";

	public static final String IDENTIFIKASJONSNUMMER = "01038401226";
	public static final String IDENTTYPE_FNR = "FNR";
	public static final String IDENTTYPE_DNR = "DNR";
	public static final String STATUS = "I_BRUK";



	public static PDLHentPersonResponse pdlHentPersonResponse() {
		return PDLHentPersonResponse.builder()
				.data(PDLHentPersonResponse.PDLHentPerson.builder()
						.hentPerson(createPdlHentPerson())
						.build())
				.build();
	}

	public static HentPerson createPdlHentPerson() {
		return HentPerson.builder()
				.adressebeskyttelse(Arrays.asList(HentPerson.Adressebeskyttelse.builder()
						.gradering(HentPerson.Gradering.FORTROLIG)
						.build()))
				.doedsfall(Collections.emptyList())
				.foedsel(Arrays.asList(HentPerson.Foedsel.builder()
						.foedselsaar(FOEDSELSAAR)
						.foedselsdato(FOEDSELDATO)
						.build()))
				.navn(Arrays.asList(HentPerson.PersonNavn.builder()
						.forkortetNavn(FORKOORETNAVN)
						.fornavn(FORNAVN)
						.mellomnavn(MELLOMNAVN)
						.etternavn(ETTERNAVN)
						.build()))
				.kontaktadresse(Arrays.asList(Kontaktadresse.builder()
						.gyldigFraOgMed(LocalDateTime.now().minusDays(2))
						.gyldigTilOgMed(LocalDateTime.now().plusYears(10))
						.type(PDLConstant.POSTADRESSE_INNLAND)
						.postadresseIFrittFormat(createPostadresseIFrittFormat())
						.build()))
				.sikkerhetstiltak(Collections.emptyList())
				.folkeregisteridentifikator(Arrays.asList(HentPerson.Folkeregisteridentifikator.builder()
						.identifikasjonsnummer(IDENTIFIKASJONSNUMMER)
						.type(IDENTTYPE_FNR)
						.status(STATUS)
						.build()))
				.folkeregisterpersonstatus(Arrays.asList(HentPerson.Folkeregisterpersonstatus.builder()
						.status(PERSONSTATUS_BOSATT)
						.forenkletStatus("bosattEtterFolkeregisterloven")
						.build()))
				.build();
	}

	public static HentPerson createPdlHentPersonWithVegadresse() {
		return HentPerson.builder()
				.adressebeskyttelse(Arrays.asList(HentPerson.Adressebeskyttelse.builder()
						.gradering(HentPerson.Gradering.FORTROLIG)
						.build()))
				.doedsfall(Collections.emptyList())
				.foedsel(Arrays.asList(HentPerson.Foedsel.builder()
						.foedselsaar(FOEDSELSAAR)
						.foedselsdato(FOEDSELDATO)
						.build()))
				.navn(Arrays.asList(HentPerson.PersonNavn.builder()
						.forkortetNavn(FORKOORETNAVN)
						.fornavn(FORNAVN)
						.mellomnavn(MELLOMNAVN)
						.etternavn(ETTERNAVN)
						.build()))
				.kontaktadresse(Arrays.asList(Kontaktadresse.builder()
						.gyldigFraOgMed(LocalDateTime.now().minusDays(2))
						.gyldigTilOgMed(LocalDateTime.now().plusYears(10))
						.type(PDLConstant.POSTADRESSE_INNLAND)
						.vegadresse(createVegadresse())
						.build()))
				.sikkerhetstiltak(Collections.emptyList())
				.folkeregisteridentifikator(Arrays.asList(HentPerson.Folkeregisteridentifikator.builder()
						.identifikasjonsnummer(IDENTIFIKASJONSNUMMER)
						.type(IDENTTYPE_FNR)
						.status(STATUS)
						.build()))
				.folkeregisterpersonstatus(Arrays.asList(HentPerson.Folkeregisterpersonstatus.builder()
						.status(PERSONSTATUS_BOSATT)
						.forenkletStatus("bosattEtterFolkeregisterloven")
						.build()))
				.build();
	}


	public static HentPerson createPdlHentPersonUtelandsk() {
		return HentPerson.builder()
				.adressebeskyttelse(Arrays.asList(HentPerson.Adressebeskyttelse.builder()
						.gradering(HentPerson.Gradering.FORTROLIG)
						.build()))
				.doedsfall(Collections.emptyList())
				.foedsel(Arrays.asList(HentPerson.Foedsel.builder()
						.foedselsaar(FOEDSELSAAR)
						.foedselsdato(FOEDSELDATO)
						.build()))
				.navn(Arrays.asList(HentPerson.PersonNavn.builder()
						.forkortetNavn(FORKOORETNAVN)
						.fornavn(FORNAVN)
						.mellomnavn(MELLOMNAVN)
						.etternavn(ETTERNAVN)
						.build()))
				.kontaktadresse(Arrays.asList(Kontaktadresse.builder()
						.type(POSTADRESSE_UTLAND)
						.UtenlandskAdresse(createUtenlandskAdresseWithName()).build()))
				.folkeregisteridentifikator(Arrays.asList(HentPerson.Folkeregisteridentifikator.builder()
						.identifikasjonsnummer(IDENTIFIKASJONSNUMMER)
						.type(IDENTTYPE_FNR)
						.status(STATUS)
						.build()))
				.folkeregisterpersonstatus(Arrays.asList(HentPerson.Folkeregisterpersonstatus.builder()
						.status(PERSONSTATUS_BOSATT)
						.forenkletStatus("bosattEtterFolkeregisterloven")
						.build()))
				.build();
	}

	private static KontaktinformasjonForDoedsbo.Personnavn createNavnAdvokatSomKontakt() {
		KontaktinformasjonForDoedsbo.Personnavn personnavn = new KontaktinformasjonForDoedsbo.Personnavn();
		personnavn.setFornavn(ADVOKAT_FORNAVN);
		personnavn.setMellomnavn(ADVOKAT_MELLOMNAVN);
		personnavn.setMellomnavn(ADVOKAT_ETTERNAVN);
		return personnavn;
	}

	private static Bostedsadresse createBostedsadresse() {
		return Bostedsadresse.builder()
				.angittFlyttedato(LocalDate.of(1984, Month.MARCH, 1))
				.gyldigFraOgMed(LocalDateTime.now().minusYears(3))
				.gyldigTilOgMed(LocalDateTime.now().plusYears(3))
				.coAdressenavn(null)
				.vegadresse(createVegadresse())
				.build();
	}

	private static Vegadresse createVegadresse() {
		return Vegadresse.builder()
				.matrikkelId(275433480L)
				.husnummer("45")
				.husbokstav(null)
				.bruksenhetsnummer(null)
				.adressenavn("Finnesveien")
				.kommunenummer("5432")
				.bydelsnummer(null)
				.tilleggsnavn(null)
				.postnummer("7320")
				.build();
	}

	private static Vegadresse createVegadresseWithAllValues() {
		return Vegadresse.builder()
				.matrikkelId(MATRIKKEL_ID)
				.husnummer(HUSNUMMER)
				.husbokstav(HUSBOKSTAV)
				.bruksenhetsnummer(BRUKSENETSNUMMER)
				.adressenavn(ADRESSENAVN)
				.kommunenummer(KOMMUNENUMMER)
				.bydelsnummer(BYDERLSNUMMER)
				.tilleggsnavn(TILLEGGSNAVN)
				.postnummer(POSTNUMMER)
				.build();
	}

	private static Matrikkeladresse createMatrikkeladresse() {
		return Matrikkeladresse.builder()
				.matrikkelId(MATRIKKEL_ID)
				.bruksenhetsnummer(BRUKSENETSNUMMER)
				.kommunenummer(KOMMUNENUMMER)
				.tilleggsnavn(TILLEGGSNAVN)
				.postnummer(POSTNUMMER)
				.build();
	}

	private static UtenlandskAdresse createUtenlandskAdresse() {
		return UtenlandskAdresse.builder()
				.adressenavnNummer(null)
				.postboksNummerNavn(UTENLANDSK_POSTBOKSNUMMERNAVN)
				.postkode(UTENLANDSK_POSTKODE)
				.bySted(UTENLANDSK_BYSTED)
				.regionDistriktOmraade(null)
				.landkode(UTENLANDSK_LANDKODE)
				.build();
	}

	private static Kontaktadresse.PostadresseIFrittFormat createPostadresseIFrittFormat() {
		return Kontaktadresse.PostadresseIFrittFormat.builder()
				.adresselinje1(FRITTFORMAT_ADRESSELINJE1)
				.adresselinje2(FRITTFORMAT_ADRESSELINJE2)
				.postnummer(FRITTFORMAT_POSTNUMMER)
				.build();
	}

	private static Kontaktadresse.Postboksadresse createPostboksadresse() {
		return Kontaktadresse.Postboksadresse.builder()
				.postbokseier(POSTBOKSEIER)
				.postboks(POSTBOKS)
				.postnummer(POSTNUMMER)
				.build();
	}

	private static UtenlandskAdresse createUtenlandskAdresseWithName() {
		return UtenlandskAdresse.builder()
				.adressenavnNummer(ADRESSENAVN_NUMMER)
				.postboksNummerNavn(POSTBOKSNUMMERNAVN)
				.postkode(POSTKODE)
				.bySted(BYSTED)
				.regionDistriktOmraade(REGION_DISTRIKTOMRAADE)
				.landkode(LANDKODE_UTENLANDSK)
				.build();
	}
}
