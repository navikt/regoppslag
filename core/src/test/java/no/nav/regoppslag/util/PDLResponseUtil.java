package no.nav.regoppslag.util;

import no.nav.regoppslag.consumer.pdl.to.Bostedsadresse;
import no.nav.regoppslag.consumer.pdl.to.Endring;
import no.nav.regoppslag.consumer.pdl.to.HentPerson;
import no.nav.regoppslag.consumer.pdl.to.InformasjonKilde;
import no.nav.regoppslag.consumer.pdl.to.Kontaktadresse;
import no.nav.regoppslag.consumer.pdl.to.KontaktinformasjonForDoedsbo;
import no.nav.regoppslag.consumer.pdl.to.Matrikkeladresse;
import no.nav.regoppslag.consumer.pdl.to.Metadata;
import no.nav.regoppslag.consumer.pdl.to.Oppholdsadresse;
import no.nav.regoppslag.consumer.pdl.to.PDLConstant;
import no.nav.regoppslag.consumer.pdl.to.PDLHentPersonResponse;
import no.nav.regoppslag.consumer.pdl.to.UkjentBosted;
import no.nav.regoppslag.consumer.pdl.to.UtenlandskAdresse;
import no.nav.regoppslag.consumer.pdl.to.Vegadresse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static java.util.Collections.singletonList;
import static no.nav.regoppslag.consumer.pdl.to.Endring.EndringsType.OPPRETT;
import static no.nav.regoppslag.consumer.pdl.to.InformasjonKilde.FREG;
import static no.nav.regoppslag.consumer.pdl.to.InformasjonKilde.PDL;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.PERSONSTATUS_BOSATT;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.PERSONSTATUS_DOED;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.PERSONSTATUS_UTFLYTTET;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_UTLAND;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


public class PDLResponseUtil {

	public static final String UTENLANDSK_POSTKODE = "K1A 0B1";
	public static final String UTENLANDSK_BYSTED = "Ottawa";
	public static final String UTENLANDSK_POSTBOKSNUMMERNAVN = "2701, promenade Riverside";
	public static final String CANADA_ALPHA3_LANDKODE = "CAN";
	public static final String CANADA_ALPHA2_LANDKODE = "CA";
	public static final String CANADA = "Canada";

	public static final String UTENLANDSK_ADRESSELINJE1 = "1KOLEJOWA 6/5";
	public static final String UTENLANDSK_ADRESSELINJE2 = "18-500 KOLNO";
	public static final String UTENLANDSK_ADRESSELINJE3 = "CAPITAL WEST 3000";

	public static final String CONAVN_UTENLANDSK_ADRESSELINJE1 = "GREENDALE COMMUNITY COLLEGE";
	public static final String CONAVN_UTENLANDSK_ADRESSELINJE2 = "GREENDALE COUNTY";
	public static final String CONAVN_UTENLANDSK_ADRESSELINJE3 = "Milwaukee, Wisconsin";
	public static final String BYGNINGETASJELEILIGHET = "Test 175";

	public static final String ADVOKAT_FORNAVN = "Herr";
	public static final String ADVOKAT_MELLOMNAVN = "";
	public static final String ADVOKAT_ETTERNAVN = "Andersen";

	public static final String PERSON_FORNAVN = "Herr";
	public static final String PERSON_MELLOMNAVN = "Andersen";

	public static final String ADRESSENAVN_1 = "Finnesveien 45";

	public static final int FOEDSELSAAR = 1984;
	public static final LocalDate FOEDSELDATO = LocalDate.of(1984, Month.APRIL, 24);
	public static final LocalDate DOEDSDATO = LocalDate.now();
	public static final String KORT_NAVN = "GYNGEHEST ÅPENHJERTIG";
	public static final String FULLT_NAVN = "GYNGEHEST A. ÅPENHJERTIG";
	public static final String FULLT_NAVN2 = "AREMARK TESTFAMILIEN";
	public static final String FORNAVN = "GYNGEHEST";
	public static final String MELLOMNAVN = "A.";
	public static final String ETTERNAVN = "ÅPENHJERTIG";
	public static final Long MATRIKKEL_ID = 123456789L;
	public static final String HUSNUMMER = "45";
	public static final String HUSBOKSTAV = "B";
	public static final String BRUKSENETSNUMMER = "H0101";
	public static final String ADRESSELINJE1_POSTBOKS = "C/O Finnesveien 27";
	public static final String ADRESSELINJE2_POSTBOKS = "Postboks 7320";
	public static final String ADRESSENAVN = "Finnesveien";
	public static final String KOMMUNENUMMER = "5432";
	public static final String BYDERLSNUMMER = "030110";
	public static final String TILLEGGSNAVN = "Storgården";
	public static final String POSTNUMMER = "7320";
	public static final String FRITTFORMAT_ADRESSELINJE1 = "C/O Kari Hansen";
	public static final String FRITTFORMAT_ADRESSELINJE2 = "Kirkegata 12";
	public static final String FRITTFORMAT_POSTNUMMER = "7320";
	public static final String POSTSTED = "FANNREM";
	public static final String UTENLANDSK_POSTSTED = "Berlin";
	public static final String UTENLANDSK_POSTNUMMER = "12345";
	public static final String ALPHA3_LANDKODE_TYSKLAND = "DEU";
	public static final String POSTBOKSEIER = "Byggfirma A/S";
	public static final String STATE = "Yorkshire";
	public static final String ADRESSENAVN_NUMMER = "";
	public static final String POSTBOKSNUMMERNAVN = "P.O.Box 7320 Place";
	public static final String POSTKODE = "SE-12345";
	public static final String POSTKODE_AND_BYSTED = "SE-12345 Haworth";
	public static final String BYSTED = "Haworth";
	public static final String REGION_DISTRIKTOMRAADE = "Yorkshire";
	public static final String LANDKODE_UTENLANDSK = "SWE";
	public static final String ALPHA2_SWEDEN_LANDKODE = "SE";
	public static final String LAND_UTENLANDSK = "SVERIGE";
	public static final String SWEDEN_UTENLANDSK = "Sweden";
	public static final String GREECE_LANDKODE = "GR";
	public static final String GREECE = "Greece";
	public static final String LANDKODE_NORGE = "NO";
	public static final String LANDKODE_POLAND = "PL";
	public static final String LANDKODE_USA = "USA";
	public static final String LANDKODE_US = "US";
	public static final String POLAND = "Poland";
	public static final String PERSON_IDENT = "0102030405";
	public static final String D_NUMMER = "0102030405";
	public static final String IDENTIFIKASJONSNUMMER = "01038401226";
	public static final String IDENTTYPE_FNR = "FNR";
	public static final String STATUS = "I_BRUK";
	public static final String ORGANISASJONNUMMER = "1234567";
	public static final String ORGANISASJONNAVN = "Fred Advokat AS";
	public static final LocalDate ATTESTUTSTEDELSEDATO = LocalDate.now().plusMonths(6);
	public static final String COADRESSENAVN = "C/O Herr Andersen";
	public static final String V_ADRESSENAVN = "v/ Herr Andersen";
	public static final String CO_ORGINASJON_NAVN = "v/ Fred Advokat AS";

	public static final String TEMA = "DAG";
	public static final String POSTSTED_OSLO = "OSLO";
	public static final String ADRESSELINJE_POSTBOKS = "Postboks 15";
	public static final String ADRESSELINJE_BYSTED = "K1A 0B1 Ottawa";

	public static final LocalDateTime GYLDIG_TIL_MED_DATO = LocalDateTime.now().plusMonths(10L);
	public static final LocalDateTime GYLDIG_FRA_MED_DATO = LocalDateTime.now().minusDays(2L);
	public static final String BYGNING_ETASJE_LEILIGHET_BVH = "ESTLANDSHUSET";
	public static final String BYSTED_BVH = "BEVERLY HILLS";
	public static final String POSTKODE_BVH = "90210";

	public static PDLHentPersonResponse pdlHentPersonResponse(HentPerson.PersonNavn personNavn) {
		return PDLHentPersonResponse.builder()
				.data(PDLHentPersonResponse.PDLHentPerson.builder()
						.hentPerson(createPdlHentPerson(personNavn))
						.build())
				.build();
	}

	public static HentPerson createPdlHentPerson(HentPerson.PersonNavn personNavn) {
		return HentPerson.builder()
				.adressebeskyttelse(singletonList(HentPerson.Adressebeskyttelse.builder()
						.gradering(HentPerson.Gradering.FORTROLIG)
						.build()))
				.doedsfall(Collections.emptyList())
				.foedsel(singletonList(HentPerson.Foedsel.builder()
						.foedselsaar(FOEDSELSAAR)
						.foedselsdato(FOEDSELDATO)
						.build()))
				.navn(singletonList(personNavn))
				.kontaktadresse(singletonList(Kontaktadresse.builder()
						.gyldigFraOgMed(LocalDateTime.now().minusDays(2))
						.gyldigTilOgMed(LocalDateTime.now().plusYears(10))
						.type(PDLConstant.POSTADRESSE_INNLAND)
						.postadresseIFrittFormat(createPostadresseIFrittFormat())
						.metadata(Metadata.builder().master(FREG.name()).build())
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
						.forkortetNavn(KORT_NAVN)
						.fornavn(FORNAVN)
						.mellomnavn(MELLOMNAVN)
						.etternavn(ETTERNAVN)
						.build()))
				.kontaktadresse(singletonList(Kontaktadresse.builder()
						.gyldigFraOgMed(LocalDateTime.now().minusDays(2))
						.gyldigTilOgMed(LocalDateTime.now().plusYears(10))
						.type(PDLConstant.POSTADRESSE_INNLAND)
						.vegadresse(createVegadresse())
						.metadata(Metadata.builder().master(InformasjonKilde.PDL.name()).build())
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
						.forkortetNavn(KORT_NAVN)
						.fornavn(FORNAVN)
						.mellomnavn(MELLOMNAVN)
						.etternavn(ETTERNAVN)
						.build()))
				.oppholdsadresse(singletonList(Oppholdsadresse.builder()
						.gyldigFraOgMed(LocalDateTime.now().minusDays(2))
						.gyldigTilOgMed(LocalDateTime.now().plusYears(10))
						.vegadresse(createVegadresse())
						.metadata(Metadata.builder().master(PDL.name()).build())
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
						.forkortetNavn(KORT_NAVN)
						.fornavn(FORNAVN)
						.mellomnavn(MELLOMNAVN)
						.etternavn(ETTERNAVN)
						.build()))
				.bostedsadresse(singletonList(Bostedsadresse.builder()
						.gyldigFraOgMed(LocalDateTime.now().minusDays(2))
						.gyldigTilOgMed(LocalDateTime.now().plusYears(10))
						.vegadresse(createVegadresse())
						.metadata(Metadata.builder()
								.endringer(singletonList(Endring.builder()
										.registrert(LocalDateTime.now().minusDays(2))
										.type(OPPRETT)
										.build()))
								.build())
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

	public static HentPerson createPdlHentPersonWithBostedsadresseAndKontaktadresse(Bostedsadresse bostedsadresse, Kontaktadresse kontaktadresse) {
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
						.forkortetNavn(KORT_NAVN)
						.fornavn(FORNAVN)
						.mellomnavn(MELLOMNAVN)
						.etternavn(ETTERNAVN)
						.build()))
				.bostedsadresse(singletonList(bostedsadresse))
				.kontaktadresse(singletonList(kontaktadresse))
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

	public static HentPerson createPdlHentPersonWithBostedsadresseAndOppholdsAdresse(Bostedsadresse bostedsadresse, Oppholdsadresse oppholdsadresse) {
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
						.forkortetNavn(KORT_NAVN)
						.fornavn(FORNAVN)
						.mellomnavn(MELLOMNAVN)
						.etternavn(ETTERNAVN)
						.build()))
				.oppholdsadresse(singletonList(oppholdsadresse))
				.bostedsadresse(singletonList(bostedsadresse))
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

	public static Oppholdsadresse createOppholdsAdresseWithAntallDager(Integer antallDager, Integer oppholdsadresseSisteEndring){
		return Oppholdsadresse.builder()
				.gyldigFraOgMed(antallDager == null ? null : LocalDateTime.now().minusDays(antallDager))
				.gyldigTilOgMed(LocalDateTime.now().plusYears(10))
				.vegadresse(createVegadresse())
				.metadata(Metadata.builder()
						.master(PDL.name())
						.endringer(singletonList(Endring.builder()
								.registrert(LocalDateTime.now().minusDays(oppholdsadresseSisteEndring))
								.type(OPPRETT)
								.build()))
						.build())
				.build();
	}

	public static Bostedsadresse createBostedsAdresseWithAntallDager(Integer antallDager, Integer bostedsadresseSisteEndring){
		return Bostedsadresse.builder()
				.gyldigFraOgMed(antallDager == null ? null : LocalDateTime.now().minusDays(antallDager))
				.gyldigTilOgMed(LocalDateTime.now().plusYears(10))
				.vegadresse(createVegadresse())
				.metadata(Metadata.builder()
						.master(PDL.name())
						.endringer(singletonList(Endring.builder()
								.registrert(LocalDateTime.now().minusDays(bostedsadresseSisteEndring))
								.type(OPPRETT)
								.build()))
						.build())
				.build();
	}

	public static Bostedsadresse createUtenlandskBostedsAdresseWithAntallDager(Integer antallDager, Integer bostedsadresseSisteEndring){
		return Bostedsadresse.builder()
				.gyldigFraOgMed(antallDager == null ? null : LocalDateTime.now().minusDays(antallDager))
				.gyldigTilOgMed(LocalDateTime.now().plusYears(10))
				.utenlandskAdresse(createUtenlandskAdresse("FR"))
				.metadata(Metadata.builder()
						.master(PDL.name())
						.endringer(singletonList(Endring.builder()
								.registrert(LocalDateTime.now().minusDays(bostedsadresseSisteEndring))
								.type(OPPRETT)
								.build()))
						.build())
				.build();
	}


	public static Kontaktadresse createKontaktAdresseWithAntallDager(Integer antallDager, Integer kontaktadresseSisteEndring){
		return Kontaktadresse.builder()
				.gyldigFraOgMed(antallDager == null ? null : LocalDateTime.now().minusDays(antallDager))
				.gyldigTilOgMed(LocalDateTime.now().plusYears(10))
				.type(PDLConstant.POSTADRESSE_INNLAND)
				.vegadresse(createVegadresse())
				.metadata(Metadata.builder()
					.master(InformasjonKilde.PDL.name())
						.endringer(singletonList(Endring.builder()
								.registrert(LocalDateTime.now().minusDays(kontaktadresseSisteEndring))
								.type(OPPRETT)
								.build()))
						.build())
				.build();
	}

	public static HentPerson createBostedsadresseWithUkjentBosted() {
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
						.forkortetNavn(KORT_NAVN)
						.fornavn(FORNAVN)
						.mellomnavn(MELLOMNAVN)
						.etternavn(ETTERNAVN)
						.build()))
				.bostedsadresse(singletonList(Bostedsadresse.builder()
						.metadata(Metadata.builder()
								.endringer(singletonList(Endring.builder()
								.registrert(LocalDateTime.now().minusDays(2))
								.type(OPPRETT)
								.build()))
								.build())
						.gyldigFraOgMed(LocalDateTime.now().minusDays(2))
						.gyldigTilOgMed(LocalDateTime.now().plusYears(10))
						.ukjentBosted(UkjentBosted.builder().bostedskommune(POSTSTED_OSLO).build())
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
						.forkortetNavn(KORT_NAVN)
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

	public static HentPerson.PersonNavn createPersonnavn() {
		return HentPerson.PersonNavn.builder()
				.forkortetNavn(KORT_NAVN)
				.fornavn(FORNAVN)
				.mellomnavn(MELLOMNAVN)
				.etternavn(ETTERNAVN)
				.build();
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
						.forkortetNavn(KORT_NAVN)
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
						.forkortetNavn(KORT_NAVN)
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

	public static HentPerson createPdlHentPersonStatusUtflyttet() {
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
						.forkortetNavn(KORT_NAVN)
						.fornavn(FORNAVN)
						.mellomnavn(MELLOMNAVN)
						.etternavn(ETTERNAVN)
						.build()))
				.folkeregisteridentifikator(singletonList(HentPerson.Folkeregisteridentifikator.builder()
						.identifikasjonsnummer(IDENTIFIKASJONSNUMMER)
						.type(IDENTTYPE_FNR)
						.status(STATUS)
						.build()))
				.folkeregisterpersonstatus(singletonList(HentPerson.Folkeregisterpersonstatus.builder()
						.status(PERSONSTATUS_UTFLYTTET)
						.forenkletStatus("ikkeBosatt")
						.folkeregistermetadata(HentPerson.Folkeregistermetadata.builder()
								.kilde("KILDE_DSF")
								.build())
						.build()))
				.build();
	}

	public static HentPerson createPdlHentPersonUtenlandskAdresse() {
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
						.forkortetNavn(KORT_NAVN)
						.fornavn(FORNAVN)
						.mellomnavn(MELLOMNAVN)
						.etternavn(ETTERNAVN)
						.build()))
				.kontaktadresse(singletonList(Kontaktadresse.builder()
						.type(POSTADRESSE_UTLAND)
						.metadata(Metadata.builder().master(PDL.name()).build())
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
		return HentPerson.Doedsfall.builder().doedsdato(date).build();
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
				.adresse(createDoedsboKontaktAdresseForUtenland())
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

	private static KontaktinformasjonForDoedsbo.KontaktAdresse createDoedsboKontaktAdresseForUtenland() {
		KontaktinformasjonForDoedsbo.KontaktAdresse kontaktAdresse = new KontaktinformasjonForDoedsbo.KontaktAdresse();
		kontaktAdresse.setAdresselinje1(ADRESSENAVN_1);
		kontaktAdresse.setPostnummer(UTENLANDSK_POSTNUMMER);
		kontaktAdresse.setPoststedsnavn(UTENLANDSK_POSTSTED);
		kontaktAdresse.setLandkode(ALPHA3_LANDKODE_TYSKLAND);
		return kontaktAdresse;
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
				.landkode(CANADA_ALPHA3_LANDKODE)
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

	public static Kontaktadresse.Postboksadresse createPostboksadresse(String postboks) {
		return Kontaktadresse.Postboksadresse.builder()
				.postbokseier(POSTBOKSEIER)
				.postboks(postboks)
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

	public static Metadata createMetadata(String source) {
		return Metadata.builder()
				.master(source)
				.build();

	}

	public static void postPdlGraphql(int status, String filePath) {
		stubFor(post("/graphql").willReturn(aResponse()
				.withStatus(status)
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withHeader("Connection", "close")
				.withBodyFile(filePath)));
	}

	public static void postPdlDigdir(int status, String filePath) {
		stubFor(post("/digdir/rest/v1/personer?inkluderSikkerDigitalPost=false").willReturn(aResponse()
				.withStatus(status)
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withHeader("Connection", "close")
				.withBodyFile(filePath)));
	}

	public static void stubGetEnhetNavn(int status, String filePath) {
		stubFor(get("/norg2/enhet/0136").willReturn(aResponse()
				.withStatus(status)
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile(filePath)));
	}

	public static void stubGetEnhetKontaktInfo(int status, String filePath) {
		stubFor(get(urlMatching("/norg2/enhet/0136/kontaktinformasjon")).willReturn(aResponse()
				.withStatus(status)
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withHeader("Connection", "close")
				.withBodyFile(filePath)));
	}
	public static void postPdlGraphqlWithErrorResponse(int status) {
		stubFor(post("/graphql").willReturn(aResponse()
				.withStatus(status)
				.withHeader("Connection", "close")
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
	}

}
