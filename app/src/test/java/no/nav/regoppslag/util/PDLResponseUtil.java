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
import java.util.Collections;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static java.util.Collections.singletonList;
import static no.nav.regoppslag.consumer.pdl.PDLConstant.PERSONSTATUS_BOSATT;
import static no.nav.regoppslag.consumer.pdl.PDLConstant.PERSONSTATUS_DOED;
import static no.nav.regoppslag.consumer.pdl.PDLConstant.POSTADRESSE_UTLAND;


public class PDLResponseUtil {

	public static final String UTENLANDSK_NAVN = "Herr H. C. Andersen";
	public static final String UTENLANDSK_POSTKODE = "Ottawa ON K1A 0B1";
	public static final String UTENLANDSK_BYSTED = "Ottawa";
	public static final String UTENLANDSK_POSTBOKSNUMMERNAVN = "2701, promenade Riverside";
	public static final String UTENLANDSK_LANDKODE = "CA";

	public static final String ADVOKAT_FORNAVN = "Herr";
	public static final String ADVOKAT_MELLOMNAVN = "";
	public static final String ADVOKAT_ETTERNAVN = "Andersen";

	public static final String PERSON_FORNAVN = "Herr";
	public static final String PERSON_MELLOMNAVN = "Andersen";

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
	public static final String HUSNUMMER = "45";
	public static final String HUSBOKSTAV = "B";
	public static final String BRUKSENETSNUMMER = "H0101";
	public static final String ADRESSENAVN = "Finnesveien";
	public static final String KOMMUNENUMMER = "5432";
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
	public static final String LANDKODE_NORGE = "NOR";
	public static final String PERSON_IDENT = "0102030405";
	public static final String IDENTIFIKASJONSNUMMER = "01038401226";
	public static final String IDENTTYPE_FNR = "FNR";
	public static final String IDENTTYPE_DNR = "DNR";
	public static final String STATUS = "I_BRUK";
	public static final String ORGANISASJONNUMMER = "1234567";
	public static final String ORGANISASJONNAVN = "Fred Advokat AS";
	public static final LocalDate ATTESTUTSTEDELSEDATO = LocalDate.now().plusMonths(6);
	public static final String COADRESSENAVN = "C/O Herr Andersen";
	public static final String CO_ORGINASJON_NAVN = "C/O Fred Advokat AS";

	public static final String POSTSTED_OSLO = "OSLO";
	public static final String ADRESSELINJE_POSTBOKS= "Postboks 15";

	public static PDLHentPersonResponse pdlHentPersonResponse() {
		return PDLHentPersonResponse.builder()
				.data(PDLHentPersonResponse.PDLHentPerson.builder()
						.hentPerson(createPdlHentPerson())
						.build())
				.build();
	}

	public static HentPerson createPdlHentPerson() {
		return HentPerson.builder()
				.adressebeskyttelse(singletonList(HentPerson.Adressebeskyttelse.builder()
						.gradering(HentPerson.Gradering.FORTROLIG)
						.build()))
				.doedsfall(Collections.emptyList())
				.foedsel(singletonList(HentPerson.Foedsel.builder()
						.foedselsaar(FOEDSELSAAR)
						.foedselsdato(FOEDSELDATO)
						.build()))
				.navn(singletonList(HentPerson.PersonNavn.builder()
						.forkortetNavn(FORKOORETNAVN)
						.fornavn(FORNAVN)
						.mellomnavn(MELLOMNAVN)
						.etternavn(ETTERNAVN)
						.build()))
				.kontaktadresse(singletonList(Kontaktadresse.builder()
						.gyldigFraOgMed(LocalDateTime.now().minusDays(2))
						.gyldigTilOgMed(LocalDateTime.now().plusYears(10))
						.type(PDLConstant.POSTADRESSE_INNLAND)
						.postadresseIFrittFormat(createPostadresseIFrittFormat())
						.build()))
				.sikkerhetstiltak(Collections.emptyList())
				.folkeregisteridentifikator(singletonList(HentPerson.Folkeregisteridentifikator.builder()
						.identifikasjonsnummer(IDENTIFIKASJONSNUMMER)
						.type(IDENTTYPE_FNR)
						.status(STATUS)
						.build()))
				.folkeregisterpersonstatus(singletonList(HentPerson.Folkeregisterpersonstatus.builder()
						.status(PERSONSTATUS_BOSATT)
						.forenkletStatus("bosattEtterFolkeregisterloven")
						.build()))
				.build();
	}

	public static HentPerson createPdlHentPersonWithVegadresse() {
		return HentPerson.builder()
				.adressebeskyttelse(singletonList(HentPerson.Adressebeskyttelse.builder()
						.gradering(HentPerson.Gradering.FORTROLIG)
						.build()))
				.doedsfall(Collections.emptyList())
				.foedsel(singletonList(HentPerson.Foedsel.builder()
						.foedselsaar(FOEDSELSAAR)
						.foedselsdato(FOEDSELDATO)
						.build()))
				.navn(singletonList(HentPerson.PersonNavn.builder()
						.forkortetNavn(FORKOORETNAVN)
						.fornavn(FORNAVN)
						.mellomnavn(MELLOMNAVN)
						.etternavn(ETTERNAVN)
						.build()))
				.kontaktadresse(singletonList(Kontaktadresse.builder()
						.gyldigFraOgMed(LocalDateTime.now().minusDays(2))
						.gyldigTilOgMed(LocalDateTime.now().plusYears(10))
						.type(PDLConstant.POSTADRESSE_INNLAND)
						.vegadresse(createVegadresse())
						.build()))
				.sikkerhetstiltak(Collections.emptyList())
				.folkeregisteridentifikator(singletonList(HentPerson.Folkeregisteridentifikator.builder()
						.identifikasjonsnummer(IDENTIFIKASJONSNUMMER)
						.type(IDENTTYPE_FNR)
						.status(STATUS)
						.build()))
				.folkeregisterpersonstatus(singletonList(HentPerson.Folkeregisterpersonstatus.builder()
						.status(PERSONSTATUS_BOSATT)
						.forenkletStatus("bosattEtterFolkeregisterloven")
						.build()))
				.build();
	}


	public static HentPerson createPdlHentPersonWithOppholdsadresse() {
		return HentPerson.builder()
				.adressebeskyttelse(singletonList(HentPerson.Adressebeskyttelse.builder()
						.gradering(HentPerson.Gradering.FORTROLIG)
						.build()))
				.doedsfall(Collections.emptyList())
				.foedsel(singletonList(HentPerson.Foedsel.builder()
						.foedselsaar(FOEDSELSAAR)
						.foedselsdato(FOEDSELDATO)
						.build()))
				.navn(singletonList(HentPerson.PersonNavn.builder()
						.forkortetNavn(FORKOORETNAVN)
						.fornavn(FORNAVN)
						.mellomnavn(MELLOMNAVN)
						.etternavn(ETTERNAVN)
						.build()))
				.oppholdsadresse(singletonList(Oppholdsadresse.builder()
						.gyldigFraOgMed(LocalDateTime.now().minusDays(2))
						.gyldigTilOgMed(LocalDateTime.now().plusYears(10))
						.vegadresse(createVegadresse())
						.build()))
				.sikkerhetstiltak(Collections.emptyList())
				.folkeregisteridentifikator(singletonList(HentPerson.Folkeregisteridentifikator.builder()
						.identifikasjonsnummer(IDENTIFIKASJONSNUMMER)
						.type(IDENTTYPE_FNR)
						.status(STATUS)
						.build()))
				.folkeregisterpersonstatus(singletonList(HentPerson.Folkeregisterpersonstatus.builder()
						.status(PERSONSTATUS_BOSATT)
						.forenkletStatus("bosattEtterFolkeregisterloven")
						.build()))
				.build();
	}

	public static HentPerson createPdlHentPersonWithBostedsadresse() {
		return HentPerson.builder()
				.adressebeskyttelse(singletonList(HentPerson.Adressebeskyttelse.builder()
						.gradering(HentPerson.Gradering.FORTROLIG)
						.build()))
				.doedsfall(Collections.emptyList())
				.foedsel(singletonList(HentPerson.Foedsel.builder()
						.foedselsaar(FOEDSELSAAR)
						.foedselsdato(FOEDSELDATO)
						.build()))
				.navn(singletonList(HentPerson.PersonNavn.builder()
						.forkortetNavn(FORKOORETNAVN)
						.fornavn(FORNAVN)
						.mellomnavn(MELLOMNAVN)
						.etternavn(ETTERNAVN)
						.build()))
				.bostedsadresse(singletonList(Bostedsadresse.builder()
						.gyldigFraOgMed(LocalDateTime.now().minusDays(2))
						.gyldigTilOgMed(LocalDateTime.now().plusYears(10))
						.vegadresse(createVegadresse())
						.build()))
				.sikkerhetstiltak(Collections.emptyList())
				.folkeregisteridentifikator(singletonList(HentPerson.Folkeregisteridentifikator.builder()
						.identifikasjonsnummer(IDENTIFIKASJONSNUMMER)
						.type(IDENTTYPE_FNR)
						.status(STATUS)
						.build()))
				.folkeregisterpersonstatus(singletonList(HentPerson.Folkeregisterpersonstatus.builder()
						.status(PERSONSTATUS_BOSATT)
						.forenkletStatus("bosattEtterFolkeregisterloven")
						.build()))
				.build();
	}

	public static HentPerson.HentPersonBuilder createHentePersonBuilder() {
		return HentPerson.builder()
				.adressebeskyttelse(singletonList(HentPerson.Adressebeskyttelse.builder()
						.gradering(HentPerson.Gradering.FORTROLIG)
						.build()))
				.foedsel(singletonList(HentPerson.Foedsel.builder()
						.foedselsaar(FOEDSELSAAR)
						.foedselsdato(FOEDSELDATO)
						.build()))
				.navn(singletonList(HentPerson.PersonNavn.builder()
						.forkortetNavn(FORKOORETNAVN)
						.fornavn(FORNAVN)
						.mellomnavn(MELLOMNAVN)
						.etternavn(ETTERNAVN)
						.build()))
				.folkeregisteridentifikator(singletonList(HentPerson.Folkeregisteridentifikator.builder()
						.identifikasjonsnummer(IDENTIFIKASJONSNUMMER)
						.type(IDENTTYPE_FNR)
						.status(STATUS)
						.build()));
	}

	public static HentPerson.Folkeregisterpersonstatus createFolkeregisterpersonstatus(String status) {
		return HentPerson.Folkeregisterpersonstatus.builder()
				.status(status)
				.forenkletStatus("bosattEtterFolkeregisterloven")
				.build();
	}

	public static HentPerson createPdlHentPersonWithNoAdresse() {
		return HentPerson.builder()
				.adressebeskyttelse(singletonList(HentPerson.Adressebeskyttelse.builder()
						.gradering(HentPerson.Gradering.FORTROLIG)
						.build()))
				.doedsfall(Collections.emptyList())
				.foedsel(singletonList(HentPerson.Foedsel.builder()
						.foedselsaar(FOEDSELSAAR)
						.foedselsdato(FOEDSELDATO)
						.build()))
				.navn(singletonList(HentPerson.PersonNavn.builder()
						.forkortetNavn(FORKOORETNAVN)
						.fornavn(FORNAVN)
						.mellomnavn(MELLOMNAVN)
						.etternavn(ETTERNAVN)
						.build()))
				.sikkerhetstiltak(Collections.emptyList())
				.folkeregisteridentifikator(singletonList(HentPerson.Folkeregisteridentifikator.builder()
						.identifikasjonsnummer(IDENTIFIKASJONSNUMMER)
						.type(IDENTTYPE_FNR)
						.status(STATUS)
						.build()))
				.folkeregisterpersonstatus(singletonList(HentPerson.Folkeregisterpersonstatus.builder()
						.status(PERSONSTATUS_BOSATT)
						.forenkletStatus("bosattEtterFolkeregisterloven")
						.build()))
				.build();
	}

	public static HentPerson createPdlHentPersonWithPersonDoedOgAdvokatSomKontakt(List<KontaktinformasjonForDoedsbo> kontaktinformasjonForDoedsboList) {
		return HentPerson.builder()
				.adressebeskyttelse(singletonList(HentPerson.Adressebeskyttelse.builder()
						.gradering(HentPerson.Gradering.FORTROLIG)
						.build()))
				.doedsfall(singletonList(HentPerson.Doedsfall.builder().doedsdato(DOEDSDATO).build()))
				.foedsel(singletonList(HentPerson.Foedsel.builder()
						.foedselsaar(FOEDSELSAAR)
						.foedselsdato(FOEDSELDATO)
						.build()))
				.navn(singletonList(HentPerson.PersonNavn.builder()
						.forkortetNavn(FORKOORETNAVN)
						.fornavn(FORNAVN)
						.mellomnavn(MELLOMNAVN)
						.etternavn(ETTERNAVN)
						.build()))
				.kontaktinformasjonForDoedsbo(kontaktinformasjonForDoedsboList)
				.sikkerhetstiltak(Collections.emptyList())
				.folkeregisteridentifikator(singletonList(HentPerson.Folkeregisteridentifikator.builder()
						.identifikasjonsnummer(IDENTIFIKASJONSNUMMER)
						.type(IDENTTYPE_FNR)
						.status(STATUS)
						.build()))
				.folkeregisterpersonstatus(singletonList(HentPerson.Folkeregisterpersonstatus.builder()
						.status(PERSONSTATUS_DOED)
						.forenkletStatus("bosattEtterFolkeregisterloven")
						.build()))
				.build();
	}

	public static HentPerson createPdlHentPersonUtelandsk() {
		return HentPerson.builder()
				.adressebeskyttelse(singletonList(HentPerson.Adressebeskyttelse.builder()
						.gradering(HentPerson.Gradering.FORTROLIG)
						.build()))
				.doedsfall(Collections.emptyList())
				.foedsel(singletonList(HentPerson.Foedsel.builder()
						.foedselsaar(FOEDSELSAAR)
						.foedselsdato(FOEDSELDATO)
						.build()))
				.navn(singletonList(HentPerson.PersonNavn.builder()
						.forkortetNavn(FORKOORETNAVN)
						.fornavn(FORNAVN)
						.mellomnavn(MELLOMNAVN)
						.etternavn(ETTERNAVN)
						.build()))
				.kontaktadresse(singletonList(Kontaktadresse.builder()
						.type(POSTADRESSE_UTLAND)
						.UtenlandskAdresse(createUtenlandskAdresseWithName()).build()))
				.folkeregisteridentifikator(singletonList(HentPerson.Folkeregisteridentifikator.builder()
						.identifikasjonsnummer(IDENTIFIKASJONSNUMMER)
						.type(IDENTTYPE_FNR)
						.status(STATUS)
						.build()))
				.folkeregisterpersonstatus(singletonList(HentPerson.Folkeregisterpersonstatus.builder()
						.status(PERSONSTATUS_BOSATT)
						.forenkletStatus("bosattEtterFolkeregisterloven")
						.build()))
				.build();
	}

	public static HentPerson.Doedsfall createDoedsfall(LocalDate date) {
		return  HentPerson.Doedsfall.builder().doedsdato(date).build();
	}

	public static KontaktinformasjonForDoedsbo createKontaktinformasjonForDoedsbo() {
		return KontaktinformasjonForDoedsbo.builder()
				.attestutstedelsesdato(ATTESTUTSTEDELSEDATO)
				.skifteform(KontaktinformasjonForDoedsbo.Skifteform.ANNET)
				.adresse(createAdvokatKontaktAdresse())
				.advokatSomKontakt(KontaktinformasjonForDoedsbo.AdvokatSomKontakt.builder()
						.personnavn(createKontaktPersonnavn())
						.organisasjonsnummer(ORGANISASJONNUMMER)
						.organisasjonsnavn(ORGANISASJONNAVN)
						.build())
				.build();
	}

	public static KontaktinformasjonForDoedsbo.KontaktinformasjonForDoedsboBuilder createKontaktinformasjonForDoeds() {
		return KontaktinformasjonForDoedsbo.builder()
				.attestutstedelsesdato(ATTESTUTSTEDELSEDATO)
				.skifteform(KontaktinformasjonForDoedsbo.Skifteform.ANNET)
				.adresse(createAdvokatKontaktAdresse())
				.personSomKontakt(KontaktinformasjonForDoedsbo.PersonSomKontakt.builder()
						.personnavn(createKontaktPersonnavn())
						.identifikasjonsnummer(ORGANISASJONNUMMER)
						.build());
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

	public static KontaktinformasjonForDoedsbo createKontaktinformasjonForDoedsboWithOrginasjon(KontaktinformasjonForDoedsbo.OrganisasjonSomKontakt organisasjonSomKontakt,
																								KontaktinformasjonForDoedsbo.KontaktAdresse adresse) {
		return KontaktinformasjonForDoedsbo.builder()
				.attestutstedelsesdato(ATTESTUTSTEDELSEDATO)
				.skifteform(KontaktinformasjonForDoedsbo.Skifteform.ANNET)
				.adresse(adresse)
				.organisasjonSomKontakt(organisasjonSomKontakt)
				.build();
	}

	public static KontaktinformasjonForDoedsbo createKontaktinformasjonForDoedsboWithNoContact(KontaktinformasjonForDoedsbo.KontaktAdresse adresse) {
		return KontaktinformasjonForDoedsbo.builder()
				.attestutstedelsesdato(ATTESTUTSTEDELSEDATO)
				.skifteform(KontaktinformasjonForDoedsbo.Skifteform.ANNET)
				.adresse(adresse)
				.build();
	}

	public static KontaktinformasjonForDoedsbo.OrganisasjonSomKontakt organisasjonSomKontakt(KontaktinformasjonForDoedsbo.Personnavn personnavn) {
		return KontaktinformasjonForDoedsbo.OrganisasjonSomKontakt.builder()
				.kontaktperson(personnavn)
				.organisasjonsnummer(ORGANISASJONNUMMER)
				.organisasjonsnavn(ORGANISASJONNAVN)
				.build();
	}

	public static KontaktinformasjonForDoedsbo.Personnavn createNavnForOrginasjonSomKontakt() {
		KontaktinformasjonForDoedsbo.Personnavn personnavn = new KontaktinformasjonForDoedsbo.Personnavn();
		personnavn.setFornavn(ORGANISASJONNAVN);
		return personnavn;
	}


	public static KontaktinformasjonForDoedsbo.Personnavn createNavnForPersonSomKontakt() {
		KontaktinformasjonForDoedsbo.Personnavn personnavn = new KontaktinformasjonForDoedsbo.Personnavn();
		personnavn.setFornavn(PERSON_FORNAVN);
		personnavn.setMellomnavn(null);
		personnavn.setEtternavn(PERSON_MELLOMNAVN);
		return personnavn;
	}

	public static KontaktinformasjonForDoedsbo.KontaktAdresse createPersonKontaktAdresse() {
		KontaktinformasjonForDoedsbo.KontaktAdresse kontaktAdresse = new KontaktinformasjonForDoedsbo.KontaktAdresse();
		kontaktAdresse.setAdresselinje1(ADRESSENAVN_1);
		kontaktAdresse.setPostnummer(POSTNUMMER);
		kontaktAdresse.setPoststedsnavn(POSTSTED);
		return kontaktAdresse;
	}

	private static KontaktinformasjonForDoedsbo.Personnavn createKontaktPersonnavn() {
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
				.matrikkelId(MATRIKKEL_ID)
				.husnummer(HUSNUMMER)
				.husbokstav(null)
				.bruksenhetsnummer(null)
				.adressenavn(ADRESSENAVN)
				.kommunenummer(KOMMUNENUMMER)
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

	public static UtenlandskAdresse createUtenlandskAdresse(String landkode) {
		return UtenlandskAdresse.builder()
				.adressenavnNummer(null)
				.postboksNummerNavn(UTENLANDSK_POSTBOKSNUMMERNAVN)
				.postkode(UTENLANDSK_POSTKODE)
				.bySted(UTENLANDSK_BYSTED)
				.regionDistriktOmraade(null)
				.landkode(landkode)
				.build();
	}

	public static Kontaktadresse.UtenlandskAdresseIFrittFormat createUtenlandskAdresseIFrittFormat() {
		return Kontaktadresse.UtenlandskAdresseIFrittFormat.builder()
				.landkode(UTENLANDSK_LANDKODE)
				.adresselinje1(UTENLANDSK_POSTBOKSNUMMERNAVN)
				.adresselinje2(UTENLANDSK_POSTKODE)
				.adresselinje3(UTENLANDSK_BYSTED)
				.byEllerStedsnavn(null)
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

	public static void getPdlDkif(int status, String filePath) {
		stubFor(get("/api/v1/personer/kontaktinformasjon?inkluderSikkerDigitalPost=false")
				.willReturn(aResponse().withStatus(status)
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile(filePath)));
	}

	public static void postPdlGraphqlWithErrorResponse(int status) {
		stubFor(post("/graphql")
				.willReturn(aResponse().withStatus(status)
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())));
	}

	public static void getStsToken(int status, String filePath) {
		stubFor(get("/stsRest/token?grant_type=client_credentials&scope=openid").willReturn(aResponse()
				.withStatus(status)
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile(filePath)));
	}
}
