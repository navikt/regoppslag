package no.nav.regoppslag.consumer.pdl;

import lombok.SneakyThrows;
import no.nav.regoppslag.consumer.pdl.map.MapPDLResponse;
import no.nav.regoppslag.consumer.pdl.to.HentPerson;
import no.nav.regoppslag.consumer.pdl.to.Kontaktadresse;
import no.nav.regoppslag.consumer.pdl.to.KontaktinformasjonForDoedsbo;
import no.nav.regoppslag.consumer.pdl.to.Metadata;
import no.nav.regoppslag.consumer.pdl.to.PDLConstant;
import no.nav.regoppslag.consumer.pdl.to.PdlMottakerInfo;
import no.nav.regoppslag.consumer.pdl.to.PostadresseTo;
import no.nav.regoppslag.consumer.pdl.to.UtenlandskAdresse;
import no.nav.regoppslag.consumer.pdl.to.Vegadresse;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;
import no.nav.regoppslag.exceptions.UkjentAdresseException;
import no.nav.regoppslag.exceptions.UkjentAdressePersonErDoed;
import no.nav.regoppslag.service.PostnummerService;
import no.nav.regoppslag.util.PDLResponseUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.nav.regoppslag.consumer.pdl.to.InformasjonKilde.FREG;
import static no.nav.regoppslag.consumer.pdl.to.InformasjonKilde.PDL;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.PERSONSTATUS_BOSATT;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.PERSONSTATUS_DOED;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.PERSONSTATUS_MIDLERTIDIG;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_INNLAND;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_UTLAND;
import static no.nav.regoppslag.metrics.MetricLabels.SERVICE_CODE_TREG002;
import static no.nav.regoppslag.util.PDLResponseUtil.ADRESSENAVN_1;
import static no.nav.regoppslag.util.PDLResponseUtil.COADRESSENAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.CO_ORGINASJON_NAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.DOEDSDATO;
import static no.nav.regoppslag.util.PDLResponseUtil.FOEDSELDATO;
import static no.nav.regoppslag.util.PDLResponseUtil.KORT_NAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.FULLT_NAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.GYLDIG_FRA_MED_DATO;
import static no.nav.regoppslag.util.PDLResponseUtil.GYLDIG_TIL_MED_DATO;
import static no.nav.regoppslag.util.PDLResponseUtil.LANDKODE_NORGE;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTNUMMER;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTSTED;
import static no.nav.regoppslag.util.PDLResponseUtil.UTENLANDSK_BYSTED;
import static no.nav.regoppslag.util.PDLResponseUtil.UTENLANDSK_LANDKODE;
import static no.nav.regoppslag.util.PDLResponseUtil.UTENLANDSK_POSTBOKSNUMMERNAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.UTENLANDSK_POSTKODE;
import static no.nav.regoppslag.util.PDLResponseUtil.createBostedsadresseWithUkjentBosted;
import static no.nav.regoppslag.util.PDLResponseUtil.createDoedsfall;
import static no.nav.regoppslag.util.PDLResponseUtil.createFolkeregisterpersonstatus;
import static no.nav.regoppslag.util.PDLResponseUtil.createHentePersonBuilder;
import static no.nav.regoppslag.util.PDLResponseUtil.createKontaktinformasjonForDoedsbo;
import static no.nav.regoppslag.util.PDLResponseUtil.createKontaktinformasjonForDoedsboWithNoContact;
import static no.nav.regoppslag.util.PDLResponseUtil.createKontaktinformasjonForDoedsboWithOrginasjon;
import static no.nav.regoppslag.util.PDLResponseUtil.createMetadata;
import static no.nav.regoppslag.util.PDLResponseUtil.createNavnForOrginasjonSomKontakt;
import static no.nav.regoppslag.util.PDLResponseUtil.createPdlHentPerson;
import static no.nav.regoppslag.util.PDLResponseUtil.createPdlHentPersonWithBostedsadresse;
import static no.nav.regoppslag.util.PDLResponseUtil.createPdlHentPersonWithOppholdsadresse;
import static no.nav.regoppslag.util.PDLResponseUtil.createPdlHentPersonWithPersonDoedOgAdvokatSomKontakt;
import static no.nav.regoppslag.util.PDLResponseUtil.createPdlHentPersonWithVegadresse;
import static no.nav.regoppslag.util.PDLResponseUtil.createPersonKontaktAdresse;
import static no.nav.regoppslag.util.PDLResponseUtil.createPersonnavn;
import static no.nav.regoppslag.util.PDLResponseUtil.createPostboksadresse;
import static no.nav.regoppslag.util.PDLResponseUtil.createUtenlandskAdresseIFrittFormat;
import static no.nav.regoppslag.util.PDLResponseUtil.organisasjonSomKontakt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.GONE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(SpringExtension.class)
public class MapPDLResponseTest {

	private static final String FEILMELDING_PERSON_DOED = "Person er død og har ingen registrerte kontaktsopplysninger for dødsbo";
	private static final String LANDKODE_FEILMELDING = "Feltet landkode kan ikke være null eller tomt for utenlandskAdresse";

	private PdlGraphQLConsumer pdlGraphQLConsumer;
	private MapPDLResponse mapPDLResponse;

	@InjectMocks
	private PostnummerService postnummerService;


	@BeforeEach
	public void setUp() {
		pdlGraphQLConsumer = mock(PdlGraphQLConsumer.class);
		mapPDLResponse = new MapPDLResponse(postnummerService);
	}

	@Test
	public void shouldMapMottakerInfoForDoedWithAdvokatSomKontakt() {
		List<KontaktinformasjonForDoedsbo> kontaktinformasjon = singletonList(createKontaktinformasjonForDoedsbo());
		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(createPdlHentPersonWithPersonDoedOgAdvokatSomKontakt(kontaktinformasjon));
		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(createPdlHentPersonWithPersonDoedOgAdvokatSomKontakt(kontaktinformasjon), SERVICE_CODE_TREG002);

		assertEquals(FOEDSELDATO, mottakerInfo.getFoedselsdato());
		assertEquals(DOEDSDATO, mottakerInfo.getDoedsdato());
		assertEquals(FULLT_NAVN, mottakerInfo.getNavn());
		assertEquals(COADRESSENAVN, mottakerInfo.getPostadresse().getAdresselinje1());
		assertEquals(ADRESSENAVN_1, mottakerInfo.getPostadresse().getAdresselinje2());
		assertEquals(POSTADRESSE_INNLAND, mottakerInfo.getPostadresse().getAdresseType());
		assertEquals(POSTNUMMER, mottakerInfo.getPostadresse().getPostnummer());
		assertEquals(POSTSTED, mottakerInfo.getPostadresse().getPoststed());
		assertEquals(LANDKODE_NORGE, mottakerInfo.getPostadresse().getLandkode());
	}

	@Test
	public void shouldMapMidlertidigKontaktWithVegadresse() {
		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(createPdlHentPersonWithVegadresse());
		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(createPdlHentPersonWithVegadresse(), SERVICE_CODE_TREG002);

		assertEquals(FOEDSELDATO, mottakerInfo.getFoedselsdato());
		assertNull(mottakerInfo.getDoedsdato());
		assertEquals(FULLT_NAVN, mottakerInfo.getNavn());
		assertEquals(ADRESSENAVN_1, mottakerInfo.getPostadresse().getAdresselinje1());
		assertNull(mottakerInfo.getPostadresse().getAdresselinje2());
		assertEquals(POSTADRESSE_INNLAND, mottakerInfo.getPostadresse().getAdresseType());
		assertEquals(POSTNUMMER, mottakerInfo.getPostadresse().getPostnummer());
		assertEquals(POSTSTED, mottakerInfo.getPostadresse().getPoststed());
		assertEquals(LANDKODE_NORGE, mottakerInfo.getPostadresse().getLandkode());
	}

	@SneakyThrows
	@Test
	public void shouldMapMidlertidigKontaktWithOppholdsadresse() {
		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(createPdlHentPersonWithVegadresse());
		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(createPdlHentPersonWithOppholdsadresse(), SERVICE_CODE_TREG002);

		assertNull(mottakerInfo.getDoedsdato());
		assertEquals(FULLT_NAVN, mottakerInfo.getNavn());
		assertEquals(KORT_NAVN, mottakerInfo.getKortNavn());
		assertEquals(ADRESSENAVN_1, mottakerInfo.getPostadresse().getAdresselinje1());
		assertNull(mottakerInfo.getPostadresse().getAdresselinje2());
		assertEquals(POSTADRESSE_INNLAND, mottakerInfo.getPostadresse().getAdresseType());
		assertEquals(POSTNUMMER, mottakerInfo.getPostadresse().getPostnummer());
		assertEquals(POSTSTED, mottakerInfo.getPostadresse().getPoststed());
		assertEquals(LANDKODE_NORGE, mottakerInfo.getPostadresse().getLandkode());
	}

	@SneakyThrows
	@Test
	public void shouldMapMidlertidigKontaktWithBostedsadresse() {
		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(createPdlHentPersonWithVegadresse());
		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(createPdlHentPersonWithBostedsadresse(), SERVICE_CODE_TREG002);

		assertNull(mottakerInfo.getDoedsdato());
		assertEquals(FULLT_NAVN, mottakerInfo.getNavn());
		assertEquals(KORT_NAVN, mottakerInfo.getKortNavn());
		assertEquals(ADRESSENAVN_1, mottakerInfo.getPostadresse().getAdresselinje1());
		assertNull(mottakerInfo.getPostadresse().getAdresselinje2());
		assertEquals(POSTADRESSE_INNLAND, mottakerInfo.getPostadresse().getAdresseType());
		assertEquals(POSTNUMMER, mottakerInfo.getPostadresse().getPostnummer());
		assertEquals(POSTSTED, mottakerInfo.getPostadresse().getPoststed());
		assertEquals(LANDKODE_NORGE, mottakerInfo.getPostadresse().getLandkode());
	}

	@Test
	public void ShouldMapAndValidateKontaktadresseForInnlandAddresseWithVegadresse() throws RegoppslagIllegalArgumentException {
		Vegadresse adresse = PDLResponseUtil.createVegadresse();
		Kontaktadresse kontaktadresse = Kontaktadresse.builder()
				.vegadresse(adresse)
				.type(POSTADRESSE_INNLAND)
				.build();
		kontaktadresse.setMetadata(Metadata.builder().master(PDL.name()).build());
		HentPerson hentPerson = createHentePersonBuilder()
				.folkeregisterpersonstatus(singletonList(createFolkeregisterpersonstatus(PERSONSTATUS_BOSATT)))
				.kontaktadresse(singletonList(kontaktadresse))
				.build();

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, SERVICE_CODE_TREG002);

		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals(ADRESSENAVN_1, response.getAdresselinje1());
		assertNull(response.getAdresselinje2());
		assertNull(response.getAdresselinje3());

		assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		assertEquals(adresse.getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
	}


	@Test
	public void ShouldMapAndValidateKontaktadresseForInnlandAddresseWithPostadresseIFrittFormatAndCoAdressenavn() throws RegoppslagIllegalArgumentException {
		Kontaktadresse.PostadresseIFrittFormat adresse = PDLResponseUtil.createPostadresseIFrittFormat();
		Kontaktadresse kontaktadresse = Kontaktadresse.builder()
				.postadresseIFrittFormat(adresse)
				.type(POSTADRESSE_INNLAND)
				.coAdressenavn(COADRESSENAVN)
				.metadata(Metadata.builder().master(PDL.name()).build())
				.build();

		HentPerson hentPerson = createHentePersonBuilder()
				.folkeregisterpersonstatus(singletonList(createFolkeregisterpersonstatus(PERSONSTATUS_BOSATT)))
				.kontaktadresse(singletonList(kontaktadresse))
				.build();

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null);

		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals(COADRESSENAVN, response.getAdresselinje1());
		assertEquals(adresse.getAdresselinje1(), response.getAdresselinje2());
		assertEquals(adresse.getAdresselinje2(), response.getAdresselinje3());

		assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		assertEquals(adresse.getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
	}

	@Test
	public void ShouldMapAndValidateKontaktadresseForInnlandAddresseWithPostadresseIFrittFormat() throws RegoppslagIllegalArgumentException {
		Kontaktadresse.PostadresseIFrittFormat adresse = PDLResponseUtil.createPostadresseIFrittFormat();
		Kontaktadresse kontaktadresse = Kontaktadresse.builder()
				.postadresseIFrittFormat(adresse)
				.type(POSTADRESSE_INNLAND)
				.build();
		kontaktadresse.setMetadata(Metadata.builder().master(PDL.name()).build());

		HentPerson hentPerson = createHentePersonBuilder()
				.folkeregisterpersonstatus(singletonList(createFolkeregisterpersonstatus(PERSONSTATUS_BOSATT)))
				.kontaktadresse(singletonList(kontaktadresse))
				.build();

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null);

		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals(adresse.getAdresselinje1(), response.getAdresselinje1());
		assertEquals(adresse.getAdresselinje2(), response.getAdresselinje2());
		assertEquals(adresse.getAdresselinje3(), response.getAdresselinje3());

		assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		assertEquals(adresse.getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
	}

	@Test
	public void shouldMapInnlandKontaktadresseWithPostboksAdresseAndPDLAsSource() throws RegoppslagIllegalArgumentException {
		Kontaktadresse.Postboksadresse adresse = createPostboksadresse();
		Kontaktadresse kontaktadresse = Kontaktadresse.builder()
				.postboksadresse(adresse)
				.type(POSTADRESSE_INNLAND)
				.build();
		kontaktadresse.setMetadata(createMetadata(PDL.name()));

		HentPerson hentPerson = createHentePersonBuilder()
				.folkeregisterpersonstatus(singletonList(createFolkeregisterpersonstatus(PERSONSTATUS_BOSATT)))
				.kontaktadresse(singletonList(kontaktadresse))
				.build();

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null);

		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals("Postboks " + adresse.getPostboks(), response.getAdresselinje1());
		assertNull(response.getAdresselinje2());
		assertNull(response.getAdresselinje3());

		assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertNull(response.getLandkode());
		assertEquals(adresse.getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
	}

	@Test
	public void shouldMapInnlandKontaktadresseWithPostboksAdresseAndFREGAsSource() {
		Kontaktadresse.Postboksadresse adresse = createPostboksadresse();
		Kontaktadresse kontaktadresse = Kontaktadresse.builder()
				.gyldigFraOgMed(GYLDIG_FRA_MED_DATO)
				.gyldigTilOgMed(GYLDIG_TIL_MED_DATO)
				.postboksadresse(adresse)
				.type(POSTADRESSE_INNLAND)
				.build();
		kontaktadresse.setMetadata(createMetadata(FREG.name()));

		HentPerson hentPerson = createHentePersonBuilder()
				.folkeregisterpersonstatus(singletonList(createFolkeregisterpersonstatus(PERSONSTATUS_BOSATT)))
				.kontaktadresse(singletonList(kontaktadresse))
				.build();

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null);

		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals("Postboks " + adresse.getPostboks(), response.getAdresselinje1());
		assertNull(response.getAdresselinje2());
		assertNull(response.getAdresselinje3());

		assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertNull(response.getLandkode());
		assertEquals(adresse.getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
	}

	@Test
	public void ShouldMapKontaktadresseForUtlandWithUtlandsAddresse() {
		UtenlandskAdresse adresse = PDLResponseUtil.createUtenlandskAdresse(UTENLANDSK_LANDKODE);
		Kontaktadresse kontaktadresse = Kontaktadresse.builder()
				.UtenlandskAdresse(adresse)
				.type(POSTADRESSE_UTLAND)
				.build();
		kontaktadresse.setMetadata(Metadata.builder().master(PDL.name()).build());

		HentPerson hentPerson = createHentePersonBuilder()
				.folkeregisterpersonstatus(singletonList(createFolkeregisterpersonstatus(PERSONSTATUS_MIDLERTIDIG)))
				.kontaktadresse(singletonList(kontaktadresse))
				.build();

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null);

		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals(adresse.getPostboksNummerNavn(), response.getAdresselinje1());
		assertEquals(adresse.getPostkode(), response.getAdresselinje2());
		assertEquals(adresse.getBySted(), response.getAdresselinje3());

		assertEquals(POSTADRESSE_UTLAND, response.getAdresseType());
		assertEquals(UTENLANDSK_LANDKODE, response.getLandkode());
		assertNull(response.getPostnummer());
		assertNull(response.getPoststed());
	}

	@Test
	public void ShouldThrowExceptionWhenLandKodeUtlandsAddresseIsNull() {
		UtenlandskAdresse adresse = PDLResponseUtil.createUtenlandskAdresse(null);
		Kontaktadresse kontaktadresse = Kontaktadresse.builder()
				.UtenlandskAdresse(adresse)
				.type(POSTADRESSE_UTLAND)
				.metadata(Metadata.builder().master(PDL.name()).build())
				.build();

		HentPerson hentPerson = createHentePersonBuilder()
				.folkeregisterpersonstatus(singletonList(createFolkeregisterpersonstatus(PERSONSTATUS_MIDLERTIDIG)))
				.kontaktadresse(singletonList(kontaktadresse))
				.build();

		RegoppslagIllegalArgumentException e = assertThrows(RegoppslagIllegalArgumentException.class, () -> mapPDLResponse.mapHentPerson(hentPerson, null));

		assertEquals(BAD_REQUEST, e.getHttpStatus());
		assertEquals(LANDKODE_FEILMELDING, e.getMessage());
	}

	@Test
	public void ShouldMapKontaktadresseForUtlandWithUtenlandskAdresseIFrittFormat() {
		Kontaktadresse.UtenlandskAdresseIFrittFormat adresse = createUtenlandskAdresseIFrittFormat();
		Kontaktadresse kontaktadresse = Kontaktadresse.builder()
				.gyldigFraOgMed(GYLDIG_FRA_MED_DATO)
				.gyldigTilOgMed(GYLDIG_TIL_MED_DATO)
				.utenlandskAdresseIFrittFormat(adresse)
				.type(POSTADRESSE_UTLAND)
				.metadata(Metadata.builder().master(PDL.name()).build())
				.build();

		HentPerson hentPerson = createHentePersonBuilder()
				.folkeregisterpersonstatus(singletonList(createFolkeregisterpersonstatus(PERSONSTATUS_MIDLERTIDIG)))
				.kontaktadresse(singletonList(kontaktadresse))
				.build();

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null);

		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals(UTENLANDSK_POSTBOKSNUMMERNAVN, response.getAdresselinje1());
		assertEquals(UTENLANDSK_POSTKODE, response.getAdresselinje2());
		assertEquals(UTENLANDSK_BYSTED, response.getAdresselinje3());

		assertEquals(POSTADRESSE_UTLAND, response.getAdresseType());
		assertEquals(UTENLANDSK_LANDKODE, response.getLandkode());
		assertNull(response.getPostnummer());
		assertNull(response.getPoststed());
	}

	@Test
	public void shlouldMapKontaktinformasjonForDoedsWithAdvokatAsContact() {
		KontaktinformasjonForDoedsbo kontaktinformasjon = createKontaktinformasjonForDoedsbo();

		HentPerson hentPerson = createHentePersonBuilder()
				.doedsfall(singletonList(createDoedsfall(DOEDSDATO)))
				.folkeregisterpersonstatus(singletonList(createFolkeregisterpersonstatus(PERSONSTATUS_DOED)))
				.kontaktinformasjonForDoedsbo(singletonList(kontaktinformasjon))
				.build();

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null);

		PostadresseTo response = mottakerInfo.getPostadresse();


		assertEquals(COADRESSENAVN, response.getAdresselinje1());
		assertEquals(kontaktinformasjon.getAdresse().getAdresselinje1(), response.getAdresselinje2());
		assertNull(response.getAdresselinje3());

		assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		assertEquals(kontaktinformasjon.getAdresse().getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
	}

	@Test
	public void shouldMapKontaktinformasjonForDoedsboWithPersonAsContact() {
		KontaktinformasjonForDoedsbo kontaktinformasjon = PDLResponseUtil.createKontaktinformasjonForDoedsboWithPerson();
		HentPerson hentPerson = createHentePersonBuilder()
				.doedsfall(singletonList(createDoedsfall(DOEDSDATO)))
				.folkeregisterpersonstatus(singletonList(createFolkeregisterpersonstatus(PERSONSTATUS_DOED)))
				.kontaktinformasjonForDoedsbo(singletonList(kontaktinformasjon))
				.build();

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null);

		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals(COADRESSENAVN, response.getAdresselinje1());
		assertEquals(kontaktinformasjon.getAdresse().getAdresselinje1(), response.getAdresselinje2());
		assertNull(response.getAdresselinje3());

		assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		assertEquals(kontaktinformasjon.getAdresse().getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
	}

	@Test
	public void shouldMapKontaktinformasjonForDoedsboWithOrganisasjonAsContact() {
		KontaktinformasjonForDoedsbo kontaktinformasjon = createKontaktinformasjonForDoedsboWithOrginasjon(organisasjonSomKontakt(createNavnForOrginasjonSomKontakt()), createPersonKontaktAdresse());
		HentPerson hentPerson = createHentePersonBuilder()
				.doedsfall(singletonList(createDoedsfall(DOEDSDATO)))
				.folkeregisterpersonstatus(singletonList(createFolkeregisterpersonstatus(PERSONSTATUS_DOED)))
				.kontaktinformasjonForDoedsbo(singletonList(kontaktinformasjon))
				.build();

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null);
		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals(CO_ORGINASJON_NAVN, response.getAdresselinje1());
		assertEquals(kontaktinformasjon.getAdresse().getAdresselinje1(), response.getAdresselinje2());
		assertNull(response.getAdresselinje3());

		assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		assertEquals(kontaktinformasjon.getAdresse().getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
	}

	@Test
	public void shouldThrowUkjentAdresseExceptionWhenNoAddess() {
		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(createPdlHentPersonWithVegadresse());

		assertThrows(UkjentAdresseException.class,
				() -> mapPDLResponse.mapHentPerson(PDLResponseUtil.createPdlHentPersonWithNoAdresse(), SERVICE_CODE_TREG002), "Fant ikke adresse for personen i PDL");
	}

	@Test
	public void shouldThrowFunctionalExceptionIfPersonErDoedOgHarIngenAdresse() {
		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(createPdlHentPersonWithPersonDoedOgAdvokatSomKontakt(emptyList()));
		UkjentAdressePersonErDoed e = assertThrows(UkjentAdressePersonErDoed.class, () ->
				mapPDLResponse.mapHentPerson(createPdlHentPersonWithPersonDoedOgAdvokatSomKontakt(emptyList()), SERVICE_CODE_TREG002));
		assertEquals(GONE, e.getHttpStatus());
		assertEquals(FEILMELDING_PERSON_DOED, e.getMessage());
	}

	@Test
	public void shouldThrowFunctionalExceptionWhenPersonnavnIsNull() {
		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(createPdlHentPerson(null));
		RegoppslagIllegalArgumentException e = assertThrows(RegoppslagIllegalArgumentException.class, () ->
				mapPDLResponse.mapHentPerson(createPdlHentPerson(null), SERVICE_CODE_TREG002));
		assertEquals(BAD_REQUEST, e.getHttpStatus());
		assertEquals("Feltet Personnavn kan ikke være null eller tomt", e.getMessage());
	}

	@Test
	public void shouldThrowFunctionalExceptionWhenFornavnIsNull() {
		HentPerson.PersonNavn personNavn = createPersonnavn();
		personNavn.setFornavn(null);
		RegoppslagIllegalArgumentException e = assertThrows(RegoppslagIllegalArgumentException.class, () ->
				mapPDLResponse.mapHentPerson(createPdlHentPerson(personNavn), SERVICE_CODE_TREG002));
		assertEquals(BAD_REQUEST, e.getHttpStatus());
		assertEquals("Feltet Fornavn kan ikke være null eller tomt", e.getMessage());
	}

	@Test
	public void shouldThrowFunctionalExceptionWhenEtternavnIsNull() {
		HentPerson.PersonNavn personNavn = createPersonnavn();
		personNavn.setEtternavn(null);
		RegoppslagIllegalArgumentException e = assertThrows(RegoppslagIllegalArgumentException.class, () ->
				mapPDLResponse.mapHentPerson(createPdlHentPerson(personNavn), SERVICE_CODE_TREG002));
		assertEquals(BAD_REQUEST, e.getHttpStatus());
		assertEquals("Feltet Etternavn kan ikke være null eller tomt", e.getMessage());
	}

	@Test
	public void shouldThrowFunctionalExceptionForUkjentBostedMottaker() {
		HentPerson hentPerson = createBostedsadresseWithUkjentBosted();

		UkjentAdresseException e = assertThrows(UkjentAdresseException.class, () ->
				mapPDLResponse.mapHentPerson(hentPerson, SERVICE_CODE_TREG002));
		assertEquals(NOT_FOUND, e.getHttpStatus());
		assertEquals("TREG002: Kunne ikke mappe postadresse for UkjentBosted mottaker", e.getMessage());
	}


	@Test
	public void shouldThrowExceptionWhenKontaktAdresseForDoedsboIsNull() {
		KontaktinformasjonForDoedsbo kontaktinformasjon = createKontaktinformasjonForDoedsboWithOrginasjon(organisasjonSomKontakt(createNavnForOrginasjonSomKontakt()), null);

		HentPerson hentPerson = createHentePersonBuilder()
				.doedsfall(singletonList(createDoedsfall(DOEDSDATO)))
				.folkeregisterpersonstatus(singletonList(createFolkeregisterpersonstatus(PERSONSTATUS_DOED)))
				.kontaktinformasjonForDoedsbo(singletonList(kontaktinformasjon))
				.build();

		UkjentAdressePersonErDoed e = assertThrows(UkjentAdressePersonErDoed.class, () ->
				mapPDLResponse.mapHentPerson(hentPerson, null));

		assertEquals(GONE, e.getHttpStatus());
		assertEquals(FEILMELDING_PERSON_DOED, e.getMessage());
	}

	@Test
	public void shouldThrowExceptionWhenKontakterAdresseForDoedsboWithOrginasjonIsNull() {
		KontaktinformasjonForDoedsbo kontaktinformasjon = createKontaktinformasjonForDoedsboWithOrginasjon(null, createPersonKontaktAdresse());

		HentPerson hentPerson = createHentePersonBuilder()
				.doedsfall(singletonList(createDoedsfall(DOEDSDATO)))
				.folkeregisterpersonstatus(singletonList(createFolkeregisterpersonstatus(PERSONSTATUS_DOED)))
				.kontaktinformasjonForDoedsbo(singletonList(kontaktinformasjon))
				.build();

		UkjentAdressePersonErDoed e = assertThrows(UkjentAdressePersonErDoed.class, () -> mapPDLResponse.mapHentPerson(hentPerson, null));

		assertEquals(GONE, e.getHttpStatus());
		assertEquals(FEILMELDING_PERSON_DOED, e.getMessage());
	}

	@Test
	public void shouldThrowFunctionalGoneExceptionWhenDoedboWithNoKontakt() {

		KontaktinformasjonForDoedsbo kontaktinformasjon = createKontaktinformasjonForDoedsboWithNoContact(createPersonKontaktAdresse());
		HentPerson hentPerson = createHentePersonBuilder()
				.doedsfall(singletonList(HentPerson.Doedsfall.builder().doedsdato(DOEDSDATO).build()))
				.kontaktinformasjonForDoedsbo(singletonList(kontaktinformasjon))
				.folkeregisterpersonstatus(singletonList(HentPerson.Folkeregisterpersonstatus.builder()
						.status(PERSONSTATUS_DOED)
						.forenkletStatus("bosattEtterFolkeregisterloven")
						.build()))
				.build();
		UkjentAdressePersonErDoed e = assertThrows(UkjentAdressePersonErDoed.class, () -> mapPDLResponse.mapHentPerson(hentPerson, "TREG002"),
				"Mottaker er registrert som død og har ugyldig postadresse");
		assertEquals(GONE, e.getHttpStatus());
	}

	@Test
	public void shouldmapVegadresseWhenCoAdressenavnIsSett() {
		Vegadresse vegadresse = PDLResponseUtil.createVegadresse();

		HentPerson hentPerson = createHentePersonBuilder()
				.folkeregisterpersonstatus(singletonList(createFolkeregisterpersonstatus(PERSONSTATUS_DOED)))
				.kontaktadresse(singletonList(Kontaktadresse.builder()
						.gyldigFraOgMed(LocalDateTime.now().minusDays(2))
						.gyldigTilOgMed(LocalDateTime.now().plusYears(10))
						.type(POSTADRESSE_INNLAND)
						.coAdressenavn(COADRESSENAVN)
						.vegadresse(vegadresse)
						.metadata(Metadata.builder().master(FREG.name()).build())
						.build()))
				.build();

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null);

		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals(COADRESSENAVN, response.getAdresselinje1());
		assertEquals(ADRESSENAVN_1, response.getAdresselinje2());
		assertNull(response.getAdresselinje3());

		assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		assertEquals(vegadresse.getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
	}

	@Test
	public void shouldMapVegadresseWhenCoAdressenavnIsNull() {
		Vegadresse vegadresse = PDLResponseUtil.createVegadresse();
		HentPerson hentPerson = createHentePersonBuilder()
				.folkeregisterpersonstatus(singletonList(createFolkeregisterpersonstatus(PERSONSTATUS_DOED)))
				.kontaktadresse(singletonList(Kontaktadresse.builder()
						.gyldigFraOgMed(LocalDateTime.now().minusDays(2))
						.gyldigTilOgMed(LocalDateTime.now().plusYears(10))
						.type(PDLConstant.POSTADRESSE_INNLAND)
						.vegadresse(vegadresse)
						.metadata(createMetadata(PDL.name()))
						.build()))
				.build();

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null);

		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals(vegadresse.getAdressenavn() + " " + vegadresse.getHusnummer(), response.getAdresselinje1());
		assertNull(response.getAdresselinje2());
		assertNull(response.getAdresselinje3());

		assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		assertEquals(vegadresse.getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
	}
}
