package no.nav.regoppslag.util;

import no.nav.regoppslag.consumer.pdl.Bostedsadresse;
import no.nav.regoppslag.consumer.pdl.HentPerson;
import no.nav.regoppslag.consumer.pdl.Kontaktadresse;
import no.nav.regoppslag.consumer.pdl.KontaktinformasjonForDoedsbo;
import no.nav.regoppslag.consumer.pdl.Matrikkeladresse;
import no.nav.regoppslag.consumer.pdl.Oppholdsadresse;
import no.nav.regoppslag.consumer.pdl.PDLConstant;
import no.nav.regoppslag.consumer.pdl.PDLHentPersonResponse;
import no.nav.regoppslag.consumer.pdl.UtenlandskAdresse;
import no.nav.regoppslag.consumer.pdl.Vegadresse;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static no.nav.regoppslag.consumer.pdl.PDLConstant.PERSONSTATUS_BOSATT;
import static no.nav.regoppslag.consumer.pdl.PDLConstant.PERSONSTATUS_DOED;
import static no.nav.regoppslag.consumer.pdl.PDLConstant.POSTADRESSE_UTLAND;


public class PDLResponseUtil {

	public static final String UTELAND_NAVN = "Herr H. C. Andersen";
	public static final String UTENLANDSK_POSTKODE = "Ottawa ON K1A 0B1";
	public static final String UTENLANDSK_BYSTED = "Ottawa";
	public static final String UTENLANDSK_POSTBOKSNUMMERNAVN = "2701, promenade Riverside";
	public static final String UTENLANDSK_LANDKODE = "CA";

	public static final String ADVOKAT_FORNAVN = "Herr";
	public static final String ADVOKAT_MELLOMNAVN = "H. C.";
	public static final String ADVOKAT_ETTERNAVN = "Andersen";

	public static final String PERSON_FORNAVN = "Person_Fornavn";
	public static final String PERSON_MELLOMNAVN = "Person_Mellomnavn";
	public static final String PERSON_ETTERNAVN = "Person_Etternavn";

	public static final String HUSNUMMER_1 = "45";
	public static final String ADRESSENAVN_1 = "Finnesveien 45";
	public static final String KOMMUNENUMMER_1 = "5432";

	public static final int FOEDSELSAAR = 1984;
	public static final LocalDate FOEDSELDATO = LocalDate.of(1984, Month.APRIL, 24);
	public static final LocalDate DOEDSDATO = LocalDate.now();
	public static final String FORKOORETNAVN = "GYNGEHEST ÅPENHJERTIG";
	public static final String FULLT_NAVN = "GYNGEHEST A. ÅPENHJERTIG";
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
	public static final String FRITTFORMAT_POSTNUMMER = "7320";
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
	public static final String PERSON_IDENT = "0102030405";
	public static final String IDENTIFIKASJONSNUMMER = "01038401226";
	public static final String IDENTTYPE_FNR = "FNR";
	public static final String IDENTTYPE_DNR = "DNR";
	public static final String STATUS = "I_BRUK";
	public static final String ORGANISASJONNUMMER = "1234567";
	public static final String ORGANISASJONNAVN = "Fred Advokat AS";
	public static final LocalDate ATTESTUTSTEDELSEDATO = LocalDate.now().plusMonths(6);
	public static final String COADRESSENAVN = "C/O Herr Andersen ";
	public static final String CO_PERSON_NAVN = "C/O Person_Fornavn Person_Mellomnavn Person_Etternavn";
	public static final String CO_ORGINASJON_NAVN = "C/O Fred Advokat AS ";

	public static PDLHentPersonResponse pdlHentPersonResponse() {
		return PDLHentPersonResponse.builder()
				.data(PDLHentPersonResponse.PDLHentPerson.builder()
						.hentPerson(createPdlHentPerson())
						.build())
				.build();
	}

	public static HentPerson createPdlHentPerson() {
		return HentPerson.builder()
				.adressebeskyttelse(Collections.singletonList(HentPerson.Adressebeskyttelse.builder()
						.gradering(HentPerson.Gradering.FORTROLIG)
						.build()))
				.doedsfall(Collections.emptyList())
				.foedsel(Collections.singletonList(HentPerson.Foedsel.builder()
						.foedselsaar(FOEDSELSAAR)
						.foedselsdato(FOEDSELDATO)
						.build()))
				.navn(Collections.singletonList(HentPerson.PersonNavn.builder()
						.forkortetNavn(FORKOORETNAVN)
						.fornavn(FORNAVN)
						.mellomnavn(MELLOMNAVN)
						.etternavn(ETTERNAVN)
						.build()))
				.kontaktadresse(Collections.singletonList(Kontaktadresse.builder()
						.gyldigFraOgMed(LocalDateTime.now().minusDays(2))
						.gyldigTilOgMed(LocalDateTime.now().plusYears(10))
						.type(PDLConstant.POSTADRESSE_INNLAND)
						.postadresseIFrittFormat(createPostadresseIFrittFormat())
						.build()))
				.sikkerhetstiltak(Collections.emptyList())
				.folkeregisteridentifikator(Collections.singletonList(HentPerson.Folkeregisteridentifikator.builder()
						.identifikasjonsnummer(IDENTIFIKASJONSNUMMER)
						.type(IDENTTYPE_FNR)
						.status(STATUS)
						.build()))
				.folkeregisterpersonstatus(Collections.singletonList(HentPerson.Folkeregisterpersonstatus.builder()
						.status(PERSONSTATUS_BOSATT)
						.forenkletStatus("bosattEtterFolkeregisterloven")
						.build()))
				.build();
	}

	public static HentPerson createPdlHentPersonWithVegadresse() {
		return HentPerson.builder()
				.adressebeskyttelse(Collections.singletonList(HentPerson.Adressebeskyttelse.builder()
						.gradering(HentPerson.Gradering.FORTROLIG)
						.build()))
				.doedsfall(Collections.emptyList())
				.foedsel(Collections.singletonList(HentPerson.Foedsel.builder()
						.foedselsaar(FOEDSELSAAR)
						.foedselsdato(FOEDSELDATO)
						.build()))
				.navn(Collections.singletonList(HentPerson.PersonNavn.builder()
						.forkortetNavn(FORKOORETNAVN)
						.fornavn(FORNAVN)
						.mellomnavn(MELLOMNAVN)
						.etternavn(ETTERNAVN)
						.build()))
				.kontaktadresse(Collections.singletonList(Kontaktadresse.builder()
						.gyldigFraOgMed(LocalDateTime.now().minusDays(2))
						.gyldigTilOgMed(LocalDateTime.now().plusYears(10))
						.type(PDLConstant.POSTADRESSE_INNLAND)
						.vegadresse(createVegadresse())
						.build()))
				.sikkerhetstiltak(Collections.emptyList())
				.folkeregisteridentifikator(Collections.singletonList(HentPerson.Folkeregisteridentifikator.builder()
						.identifikasjonsnummer(IDENTIFIKASJONSNUMMER)
						.type(IDENTTYPE_FNR)
						.status(STATUS)
						.build()))
				.folkeregisterpersonstatus(Collections.singletonList(HentPerson.Folkeregisterpersonstatus.builder()
						.status(PERSONSTATUS_BOSATT)
						.forenkletStatus("bosattEtterFolkeregisterloven")
						.build()))
				.build();
	}


	public static HentPerson createPdlHentPersonWithOppholdsadresse() {
		return HentPerson.builder()
				.adressebeskyttelse(Collections.singletonList(HentPerson.Adressebeskyttelse.builder()
						.gradering(HentPerson.Gradering.FORTROLIG)
						.build()))
				.doedsfall(Collections.emptyList())
				.foedsel(Collections.singletonList(HentPerson.Foedsel.builder()
						.foedselsaar(FOEDSELSAAR)
						.foedselsdato(FOEDSELDATO)
						.build()))
				.navn(Collections.singletonList(HentPerson.PersonNavn.builder()
						.forkortetNavn(FORKOORETNAVN)
						.fornavn(FORNAVN)
						.mellomnavn(MELLOMNAVN)
						.etternavn(ETTERNAVN)
						.build()))
				.oppholdsadresse(Collections.singletonList(Oppholdsadresse.builder()
						.gyldigFraOgMed(LocalDateTime.now().minusDays(2))
						.gyldigTilOgMed(LocalDateTime.now().plusYears(10))
						.vegadresse(createVegadresse())
						.build()))
				.sikkerhetstiltak(Collections.emptyList())
				.folkeregisteridentifikator(Collections.singletonList(HentPerson.Folkeregisteridentifikator.builder()
						.identifikasjonsnummer(IDENTIFIKASJONSNUMMER)
						.type(IDENTTYPE_FNR)
						.status(STATUS)
						.build()))
				.folkeregisterpersonstatus(Collections.singletonList(HentPerson.Folkeregisterpersonstatus.builder()
						.status(PERSONSTATUS_BOSATT)
						.forenkletStatus("bosattEtterFolkeregisterloven")
						.build()))
				.build();
	}

	public static HentPerson createPdlHentPersonWithBostedsadresse() {
		return HentPerson.builder()
				.adressebeskyttelse(Collections.singletonList(HentPerson.Adressebeskyttelse.builder()
						.gradering(HentPerson.Gradering.FORTROLIG)
						.build()))
				.doedsfall(Collections.emptyList())
				.foedsel(Collections.singletonList(HentPerson.Foedsel.builder()
						.foedselsaar(FOEDSELSAAR)
						.foedselsdato(FOEDSELDATO)
						.build()))
				.navn(Collections.singletonList(HentPerson.PersonNavn.builder()
						.forkortetNavn(FORKOORETNAVN)
						.fornavn(FORNAVN)
						.mellomnavn(MELLOMNAVN)
						.etternavn(ETTERNAVN)
						.build()))
				.bostedsadresse(Collections.singletonList(Bostedsadresse.builder()
						.gyldigFraOgMed(LocalDateTime.now().minusDays(2))
						.gyldigTilOgMed(LocalDateTime.now().plusYears(10))
						.vegadresse(createVegadresse())
						.build()))
				.sikkerhetstiltak(Collections.emptyList())
				.folkeregisteridentifikator(Collections.singletonList(HentPerson.Folkeregisteridentifikator.builder()
						.identifikasjonsnummer(IDENTIFIKASJONSNUMMER)
						.type(IDENTTYPE_FNR)
						.status(STATUS)
						.build()))
				.folkeregisterpersonstatus(Collections.singletonList(HentPerson.Folkeregisterpersonstatus.builder()
						.status(PERSONSTATUS_BOSATT)
						.forenkletStatus("bosattEtterFolkeregisterloven")
						.build()))
				.build();
	}

	public static HentPerson createPdlHentPersonWithNoAdresse() {
		return HentPerson.builder()
				.adressebeskyttelse(Collections.singletonList(HentPerson.Adressebeskyttelse.builder()
						.gradering(HentPerson.Gradering.FORTROLIG)
						.build()))
				.doedsfall(Collections.emptyList())
				.foedsel(Collections.singletonList(HentPerson.Foedsel.builder()
						.foedselsaar(FOEDSELSAAR)
						.foedselsdato(FOEDSELDATO)
						.build()))
				.navn(Collections.singletonList(HentPerson.PersonNavn.builder()
						.forkortetNavn(FORKOORETNAVN)
						.fornavn(FORNAVN)
						.mellomnavn(MELLOMNAVN)
						.etternavn(ETTERNAVN)
						.build()))
				.sikkerhetstiltak(Collections.emptyList())
				.folkeregisteridentifikator(Collections.singletonList(HentPerson.Folkeregisteridentifikator.builder()
						.identifikasjonsnummer(IDENTIFIKASJONSNUMMER)
						.type(IDENTTYPE_FNR)
						.status(STATUS)
						.build()))
				.folkeregisterpersonstatus(Collections.singletonList(HentPerson.Folkeregisterpersonstatus.builder()
						.status(PERSONSTATUS_BOSATT)
						.forenkletStatus("bosattEtterFolkeregisterloven")
						.build()))
				.build();
	}

	public static HentPerson createPdlHentPersonWithPersonDoedOgAdvokatSomKontakt() {
		return HentPerson.builder()
				.adressebeskyttelse(Collections.singletonList(HentPerson.Adressebeskyttelse.builder()
						.gradering(HentPerson.Gradering.FORTROLIG)
						.build()))
				.doedsfall(Collections.singletonList(HentPerson.Doedsfall.builder().doedsdato(DOEDSDATO).build()))
				.foedsel(Collections.singletonList(HentPerson.Foedsel.builder()
						.foedselsaar(FOEDSELSAAR)
						.foedselsdato(FOEDSELDATO)
						.build()))
				.navn(Collections.singletonList(HentPerson.PersonNavn.builder()
						.forkortetNavn(FORKOORETNAVN)
						.fornavn(FORNAVN)
						.mellomnavn(MELLOMNAVN)
						.etternavn(ETTERNAVN)
						.build()))
				.kontaktinformasjonForDoedsbo(Collections.singletonList(createKontaktinformasjonForDoedsbo()))
				.sikkerhetstiltak(Collections.emptyList())
				.folkeregisteridentifikator(Collections.singletonList(HentPerson.Folkeregisteridentifikator.builder()
						.identifikasjonsnummer(IDENTIFIKASJONSNUMMER)
						.type(IDENTTYPE_FNR)
						.status(STATUS)
						.build()))
				.folkeregisterpersonstatus(Collections.singletonList(HentPerson.Folkeregisterpersonstatus.builder()
						.status(PERSONSTATUS_DOED)
						.forenkletStatus("bosattEtterFolkeregisterloven")
						.build()))
				.build();
	}

	public static HentPerson createPdlHentPersonUtelandsk() {
		return HentPerson.builder()
				.adressebeskyttelse(Collections.singletonList(HentPerson.Adressebeskyttelse.builder()
						.gradering(HentPerson.Gradering.FORTROLIG)
						.build()))
				.doedsfall(Collections.emptyList())
				.foedsel(Collections.singletonList(HentPerson.Foedsel.builder()
						.foedselsaar(FOEDSELSAAR)
						.foedselsdato(FOEDSELDATO)
						.build()))
				.navn(Collections.singletonList(HentPerson.PersonNavn.builder()
						.forkortetNavn(FORKOORETNAVN)
						.fornavn(FORNAVN)
						.mellomnavn(MELLOMNAVN)
						.etternavn(ETTERNAVN)
						.build()))
				.kontaktadresse(Collections.singletonList(Kontaktadresse.builder()
						.type(POSTADRESSE_UTLAND)
						.UtenlandskAdresse(createUtenlandskAdresseWithName()).build()))
				.folkeregisteridentifikator(Collections.singletonList(HentPerson.Folkeregisteridentifikator.builder()
						.identifikasjonsnummer(IDENTIFIKASJONSNUMMER)
						.type(IDENTTYPE_FNR)
						.status(STATUS)
						.build()))
				.folkeregisterpersonstatus(Collections.singletonList(HentPerson.Folkeregisterpersonstatus.builder()
						.status(PERSONSTATUS_BOSATT)
						.forenkletStatus("bosattEtterFolkeregisterloven")
						.build()))
				.build();
	}

	public static KontaktinformasjonForDoedsbo createKontaktinformasjonForDoedsbo() {
		return KontaktinformasjonForDoedsbo.builder()
				.attestutstedelsesdato(ATTESTUTSTEDELSEDATO)
				.skifteform(KontaktinformasjonForDoedsbo.Skifteform.ANNET)
				.adresse(createAdvokatKontaktAdresse())
				.advokatSomKontakt(KontaktinformasjonForDoedsbo.AdvokatSomKontakt.builder()
						.personnavn(createNavnForAdvokatSomKontakt())
						.organisasjonsnummer(ORGANISASJONNUMMER)
						.organisasjonsnavn(ORGANISASJONNAVN)
						.build())
				.build();
	}

	public static KontaktinformasjonForDoedsbo createKontaktinformasjonForDoedsboWithPerson() {
		return KontaktinformasjonForDoedsbo.builder()
				.attestutstedelsesdato(ATTESTUTSTEDELSEDATO)
				.skifteform(KontaktinformasjonForDoedsbo.Skifteform.ANNET)
				.adresse(createPersonKontaktAdresse())
				.personSomKontakt(KontaktinformasjonForDoedsbo.PersonSomKontakt.builder()
						.personnavn(createNavnForPersonSomKontakt())
						.identifikasjonsnummer(IDENTIFIKASJONSNUMMER)
						.build())
				.build();
	}

	public static KontaktinformasjonForDoedsbo createKontaktinformasjonForDoedsboWithOrginasjon() {
		return KontaktinformasjonForDoedsbo.builder()
				.attestutstedelsesdato(ATTESTUTSTEDELSEDATO)
				.skifteform(KontaktinformasjonForDoedsbo.Skifteform.ANNET)
				.adresse(createPersonKontaktAdresse())
				.organisasjonSomKontakt(KontaktinformasjonForDoedsbo.OrganisasjonSomKontakt.builder()
						.kontaktperson(createNavnForOrginasjonSomKontakt())
						.organisasjonsnummer(ORGANISASJONNUMMER)
						.organisasjonsnavn(ORGANISASJONNAVN)
						.build())
				.build();
	}

	public static KontaktinformasjonForDoedsbo createKontaktinformasjonForDoedsboWithNoContact() {
		return KontaktinformasjonForDoedsbo.builder()
				.attestutstedelsesdato(ATTESTUTSTEDELSEDATO)
				.skifteform(KontaktinformasjonForDoedsbo.Skifteform.ANNET)
				.adresse(createPersonKontaktAdresse())
				.build();
	}


	private static KontaktinformasjonForDoedsbo.Personnavn createNavnForOrginasjonSomKontakt() {
		KontaktinformasjonForDoedsbo.Personnavn personnavn = new KontaktinformasjonForDoedsbo.Personnavn();
		personnavn.setFornavn(ORGANISASJONNAVN);
		return personnavn;
	}


	private static KontaktinformasjonForDoedsbo.Personnavn createNavnForPersonSomKontakt() {
		KontaktinformasjonForDoedsbo.Personnavn personnavn = new KontaktinformasjonForDoedsbo.Personnavn();
		personnavn.setFornavn(PERSON_FORNAVN);
		personnavn.setMellomnavn(PERSON_MELLOMNAVN);
		personnavn.setEtternavn(PERSON_ETTERNAVN);
		return personnavn;
	}

	private static KontaktinformasjonForDoedsbo.KontaktAdresse createPersonKontaktAdresse() {
		KontaktinformasjonForDoedsbo.KontaktAdresse kontaktAdresse = new KontaktinformasjonForDoedsbo.KontaktAdresse();
		kontaktAdresse.setAdresselinje1(ADRESSENAVN_1);
		kontaktAdresse.setPostnummer(POSTNUMMER);
		kontaktAdresse.setPoststedsnavn(POSTSTED);
		return kontaktAdresse;
	}

	private static KontaktinformasjonForDoedsbo.Personnavn createNavnForAdvokatSomKontakt() {
		KontaktinformasjonForDoedsbo.Personnavn personnavn = new KontaktinformasjonForDoedsbo.Personnavn();
		personnavn.setFornavn(ADVOKAT_FORNAVN);
		personnavn.setMellomnavn(ADVOKAT_MELLOMNAVN);
		personnavn.setMellomnavn(ADVOKAT_ETTERNAVN);
		return personnavn;
	}

	private static KontaktinformasjonForDoedsbo.KontaktAdresse createAdvokatKontaktAdresse() {
		KontaktinformasjonForDoedsbo.KontaktAdresse kontaktAdresse = new KontaktinformasjonForDoedsbo.KontaktAdresse();
		kontaktAdresse.setAdresselinje1(ADRESSENAVN_1);
		kontaktAdresse.setPostnummer(POSTNUMMER);
		kontaktAdresse.setPoststedsnavn(POSTSTED);
		return kontaktAdresse;
	}

	public Kontaktadresse kontaktadresse() {
		return Kontaktadresse.builder()
				.type(UTENLANDSK_BYSTED)
				.build();
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

	public static Vegadresse createVegadresse() {
		return Vegadresse.builder()
				.matrikkelId(275433480L)
				.husnummer("45")
				.husbokstav(null)
				.bruksenhetsnummer(null)
				.adressenavn("Finnesveien")
				.kommunenummer("5432")
				.bydelsnummer(null)
				.tilleggsnavn(null)
				.postnummer(POSTNUMMER)
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

	public static UtenlandskAdresse createUtenlandskAdresse() {
		return UtenlandskAdresse.builder()
				.adressenavnNummer(null)
				.postboksNummerNavn(UTENLANDSK_POSTBOKSNUMMERNAVN)
				.postkode(UTENLANDSK_POSTKODE)
				.bySted(UTENLANDSK_BYSTED)
				.regionDistriktOmraade(null)
				.landkode(UTENLANDSK_LANDKODE)
				.build();
	}

	public static Kontaktadresse.UtenlandskAdresseIFrittFormat createUtenlandskAdresseIFrittFormat() {
		return Kontaktadresse.UtenlandskAdresseIFrittFormat.builder()
				.postkode(UTENLANDSK_POSTKODE)
				.landkode(UTENLANDSK_LANDKODE)
				.adresselinje1("a1")
				.adresselinje2("a2")
				.adresselinje3("a3")
				.byEllerStedsnavn(POSTSTED)
				.build();
	}

	public static Kontaktadresse.PostadresseIFrittFormat createPostadresseIFrittFormat() {
		return Kontaktadresse.PostadresseIFrittFormat.builder()
				.adresselinje1(FRITTFORMAT_ADRESSELINJE1)
				.adresselinje2(FRITTFORMAT_ADRESSELINJE2)
				.postnummer(FRITTFORMAT_POSTNUMMER)
				.build();
	}

	public static Kontaktadresse.Postboksadresse createPostboksadresse() {
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

	public static void postPdlGraphql(int status, String filePath) {
		stubFor(post("/graphql")
				.willReturn(aResponse().withStatus(status)
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile(filePath)));
	}

	public static void getStsToken(int status, String filePath) {
		stubFor(get("/stsRest/token?grant_type=client_credentials&scope=openid").willReturn(aResponse()
				.withStatus(status)
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile(filePath)));
	}
}
