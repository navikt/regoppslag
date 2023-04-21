package no.nav.regoppslag.pdl;

import lombok.SneakyThrows;
import no.nav.regoppslag.consumer.pdl.PdlGraphQLConsumer;
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
import no.nav.regoppslag.service.PostnummerService;
import no.nav.regoppslag.util.PDLResponseUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;

import static com.neovisionaries.i18n.CountryCode.XK;
import static java.util.Collections.singletonList;
import static no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode.BOSTEDSADRESSE;
import static no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode.KONTAKTADRESSE;
import static no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode.OPPHOLDSADRESSE;
import static no.nav.regoppslag.consumer.pdl.to.InformasjonKilde.FREG;
import static no.nav.regoppslag.consumer.pdl.to.InformasjonKilde.PDL;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.PERSONSTATUS_BOSATT;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.PERSONSTATUS_DOED;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.PERSONSTATUS_MIDLERTIDIG;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_INNLAND;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_UTLAND;
import static no.nav.regoppslag.metrics.MetricLabels.KOSOVO_LANDKODE_NAV_REGISTRENE;
import static no.nav.regoppslag.metrics.MetricLabels.SERVICE_CODE_TREG002;
import static no.nav.regoppslag.metrics.MetricLabels.UNKNOWN_LANDKODE;
import static no.nav.regoppslag.util.PDLResponseUtil.ADRESSENAVN_1;
import static no.nav.regoppslag.util.PDLResponseUtil.CANADA_ALPHA2_LANDKODE;
import static no.nav.regoppslag.util.PDLResponseUtil.CANADA_ALPHA3_LANDKODE;
import static no.nav.regoppslag.util.PDLResponseUtil.FOEDSELDATO;
import static no.nav.regoppslag.util.PDLResponseUtil.FULLT_NAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.GYLDIG_FRA_MED_DATO;
import static no.nav.regoppslag.util.PDLResponseUtil.GYLDIG_TIL_MED_DATO;
import static no.nav.regoppslag.util.PDLResponseUtil.KORT_NAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.LANDKODE_NORGE;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTNUMMER;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTSTED;
import static no.nav.regoppslag.util.PDLResponseUtil.TEMA;
import static no.nav.regoppslag.util.PDLResponseUtil.UTENLANDSK_BYSTED;
import static no.nav.regoppslag.util.PDLResponseUtil.UTENLANDSK_POSTBOKSNUMMERNAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.UTENLANDSK_POSTKODE;
import static no.nav.regoppslag.util.PDLResponseUtil.V_ADRESSENAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.createBostedsAdresseWithAntallDager;
import static no.nav.regoppslag.util.PDLResponseUtil.createBostedsadresseWithUkjentBosted;
import static no.nav.regoppslag.util.PDLResponseUtil.createFolkeregisterpersonstatus;
import static no.nav.regoppslag.util.PDLResponseUtil.createHentePersonBuilder;
import static no.nav.regoppslag.util.PDLResponseUtil.createKontaktAdresseWithAntallDager;
import static no.nav.regoppslag.util.PDLResponseUtil.createKontaktinformasjonForDoedsbo;
import static no.nav.regoppslag.util.PDLResponseUtil.createMetadata;
import static no.nav.regoppslag.util.PDLResponseUtil.createOppholdsAdresseWithAntallDager;
import static no.nav.regoppslag.util.PDLResponseUtil.createPdlHentPerson;
import static no.nav.regoppslag.util.PDLResponseUtil.createPdlHentPersonStatusUtflyttet;
import static no.nav.regoppslag.util.PDLResponseUtil.createPdlHentPersonWithBostedsadresse;
import static no.nav.regoppslag.util.PDLResponseUtil.createPdlHentPersonWithBostedsadresseAndKontaktadresse;
import static no.nav.regoppslag.util.PDLResponseUtil.createPdlHentPersonWithBostedsadresseAndOppholdsAdresse;
import static no.nav.regoppslag.util.PDLResponseUtil.createPdlHentPersonWithOppholdsadresse;
import static no.nav.regoppslag.util.PDLResponseUtil.createPdlHentPersonWithPersonDoedOgAdvokatSomKontakt;
import static no.nav.regoppslag.util.PDLResponseUtil.createPdlHentPersonWithVegadresse;
import static no.nav.regoppslag.util.PDLResponseUtil.createPersonnavn;
import static no.nav.regoppslag.util.PDLResponseUtil.createPostboksadresse;
import static no.nav.regoppslag.util.PDLResponseUtil.createUtenlandskAdresseIFrittFormat;
import static no.nav.regoppslag.util.PDLResponseUtil.createVegadresse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(SpringExtension.class)
public class MapPDLResponseTest {

	private static final String LANDKODE_FEILMELDING = "Feltet landkode kan ikke være null eller tomt for utenlandskAdresse";

	private PdlGraphQLConsumer pdlGraphQLConsumer;
	private MapPDLResponse mapPDLResponse;
	private DoedsboAdresseService doedsboAdresseService;

	@InjectMocks
	private PostnummerService postnummerService;

	@BeforeEach
	public void setUp() {
		pdlGraphQLConsumer = mock(PdlGraphQLConsumer.class);

		doedsboAdresseService = Mockito.spy(new DoedsboAdresseService(postnummerService, pdlGraphQLConsumer));
		mapPDLResponse = new MapPDLResponse(doedsboAdresseService, new NorskAdresseService(postnummerService));
	}

	@Test
	public void shouldDelegateToDoedsboserviceWhenPersonIsDead() {
		List<KontaktinformasjonForDoedsbo> kontaktinformasjon = singletonList(createKontaktinformasjonForDoedsbo());
		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(createPdlHentPersonWithPersonDoedOgAdvokatSomKontakt(kontaktinformasjon));
		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(createPdlHentPersonWithPersonDoedOgAdvokatSomKontakt(kontaktinformasjon), SERVICE_CODE_TREG002, TEMA);

		verify(doedsboAdresseService, times(1)).mapFoerDoedsbo(any(), anyString());
	}

	@Test
	public void shouldMapMidlertidigKontaktWithVegadresse() {
		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(createPdlHentPersonWithVegadresse());
		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(createPdlHentPersonWithVegadresse(), SERVICE_CODE_TREG002, TEMA);

		assertEquals(FOEDSELDATO, mottakerInfo.getFoedselsdato());
		assertNull(mottakerInfo.getDoedsdato());
		assertEquals(FULLT_NAVN, mottakerInfo.getNavn());
		assertEquals(ADRESSENAVN_1, mottakerInfo.getPostadresse().getAdresselinje1());
		assertNull(mottakerInfo.getPostadresse().getAdresselinje2());
		assertEquals(POSTADRESSE_INNLAND, mottakerInfo.getPostadresse().getAdresseType());
		assertEquals(POSTNUMMER, mottakerInfo.getPostadresse().getPostnummer());
		assertEquals(POSTSTED, mottakerInfo.getPostadresse().getPoststed());
		assertEquals(LANDKODE_NORGE, mottakerInfo.getPostadresse().getLandkode());
		assertEquals(KONTAKTADRESSE, mottakerInfo.getPostadresse().getAdressekilde());
		verify(doedsboAdresseService, times(0)).mapFoerDoedsbo(any(), anyString());
	}

	@SneakyThrows
	@Test
	public void shouldMapMidlertidigKontaktWithOppholdsadresse() {
		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(createPdlHentPersonWithVegadresse());
		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(createPdlHentPersonWithOppholdsadresse(), SERVICE_CODE_TREG002, TEMA);

		assertNull(mottakerInfo.getDoedsdato());
		assertEquals(FULLT_NAVN, mottakerInfo.getNavn());
		assertEquals(KORT_NAVN, mottakerInfo.getKortNavn());
		assertEquals(ADRESSENAVN_1, mottakerInfo.getPostadresse().getAdresselinje1());
		assertNull(mottakerInfo.getPostadresse().getAdresselinje2());
		assertEquals(POSTADRESSE_INNLAND, mottakerInfo.getPostadresse().getAdresseType());
		assertEquals(POSTNUMMER, mottakerInfo.getPostadresse().getPostnummer());
		assertEquals(POSTSTED, mottakerInfo.getPostadresse().getPoststed());
		assertEquals(LANDKODE_NORGE, mottakerInfo.getPostadresse().getLandkode());
		assertEquals(OPPHOLDSADRESSE, mottakerInfo.getPostadresse().getAdressekilde());
	}

	@SneakyThrows
	@Test
	public void shouldMapMidlertidigKontaktWithBostedsadresse() {
		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(createPdlHentPersonWithVegadresse());
		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(createPdlHentPersonWithBostedsadresse(), SERVICE_CODE_TREG002, TEMA);

		assertNull(mottakerInfo.getDoedsdato());
		assertEquals(FULLT_NAVN, mottakerInfo.getNavn());
		assertEquals(KORT_NAVN, mottakerInfo.getKortNavn());
		assertEquals(ADRESSENAVN_1, mottakerInfo.getPostadresse().getAdresselinje1());
		assertNull(mottakerInfo.getPostadresse().getAdresselinje2());
		assertEquals(POSTADRESSE_INNLAND, mottakerInfo.getPostadresse().getAdresseType());
		assertEquals(POSTNUMMER, mottakerInfo.getPostadresse().getPostnummer());
		assertEquals(POSTSTED, mottakerInfo.getPostadresse().getPoststed());
		assertEquals(LANDKODE_NORGE, mottakerInfo.getPostadresse().getLandkode());
		assertEquals(BOSTEDSADRESSE, mottakerInfo.getPostadresse().getAdressekilde());
	}

	@Test
	public void ShouldMapAndValidateKontaktadresseForInnlandAddresseWithVegadresse() throws RegoppslagIllegalArgumentException {
		Vegadresse adresse = createVegadresse();
		Kontaktadresse kontaktadresse = Kontaktadresse.builder()
				.vegadresse(adresse)
				.type(POSTADRESSE_INNLAND)
				.build();
		kontaktadresse.setMetadata(Metadata.builder().master(PDL.name()).build());
		HentPerson hentPerson = createHentePersonBuilder()
				.folkeregisterpersonstatus(singletonList(createFolkeregisterpersonstatus(PERSONSTATUS_BOSATT)))
				.kontaktadresse(singletonList(kontaktadresse))
				.build();

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, SERVICE_CODE_TREG002, TEMA);

		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals(ADRESSENAVN_1, response.getAdresselinje1());
		assertNull(response.getAdresselinje2());
		assertNull(response.getAdresselinje3());

		assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		assertEquals(adresse.getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
		assertEquals(KONTAKTADRESSE, response.getAdressekilde());
	}


	@Test
	public void ShouldMapAndValidateKontaktadresseForInnlandAddresseWithPostadresseIFrittFormatAndCoAdressenavn() throws RegoppslagIllegalArgumentException {
		Kontaktadresse.PostadresseIFrittFormat adresse = PDLResponseUtil.createPostadresseIFrittFormat();
		Kontaktadresse kontaktadresse = Kontaktadresse.builder()
				.postadresseIFrittFormat(adresse)
				.type(POSTADRESSE_INNLAND)
				.coAdressenavn(V_ADRESSENAVN)
				.metadata(Metadata.builder().master(PDL.name()).build())
				.build();

		HentPerson hentPerson = createHentePersonBuilder()
				.folkeregisterpersonstatus(singletonList(createFolkeregisterpersonstatus(PERSONSTATUS_BOSATT)))
				.kontaktadresse(singletonList(kontaktadresse))
				.build();

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null, TEMA);

		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals(V_ADRESSENAVN, response.getAdresselinje1());
		assertEquals(adresse.getAdresselinje1(), response.getAdresselinje2());
		assertEquals(adresse.getAdresselinje2(), response.getAdresselinje3());

		assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		assertEquals(adresse.getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
		assertEquals(KONTAKTADRESSE, response.getAdressekilde());
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

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null, TEMA);

		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals(adresse.getAdresselinje1(), response.getAdresselinje1());
		assertEquals(adresse.getAdresselinje2(), response.getAdresselinje2());
		assertEquals(adresse.getAdresselinje3(), response.getAdresselinje3());

		assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		assertEquals(adresse.getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
		assertEquals(KONTAKTADRESSE, response.getAdressekilde());
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

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null, TEMA);

		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals("C/O Byggfirma A/S", response.getAdresselinje1());
		assertEquals("Postboks " + adresse.getPostboks(), response.getAdresselinje2());
		assertNull(response.getAdresselinje3());

		assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		assertEquals(adresse.getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
		assertEquals(KONTAKTADRESSE, response.getAdressekilde());
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

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null, TEMA);

		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals("C/O Byggfirma A/S", response.getAdresselinje1());
		assertEquals("Postboks " + adresse.getPostboks(), response.getAdresselinje2());
		assertNull(response.getAdresselinje3());

		assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		assertEquals(adresse.getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
		assertEquals(KONTAKTADRESSE, response.getAdressekilde());
	}

	@Test
	public void ShouldMapKontaktadresseForUtlandWithUtlandsAddresse() {
		UtenlandskAdresse adresse = PDLResponseUtil.createUtenlandskAdresse(CANADA_ALPHA3_LANDKODE);
		Kontaktadresse kontaktadresse = Kontaktadresse.builder()
				.UtenlandskAdresse(adresse)
				.type(POSTADRESSE_UTLAND)
				.build();
		kontaktadresse.setMetadata(Metadata.builder().master(PDL.name()).build());

		HentPerson hentPerson = createHentePersonBuilder()
				.folkeregisterpersonstatus(singletonList(createFolkeregisterpersonstatus(PERSONSTATUS_MIDLERTIDIG)))
				.kontaktadresse(singletonList(kontaktadresse))
				.build();

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null, TEMA);

		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals(adresse.getPostboksNummerNavn(), response.getAdresselinje1());
		assertEquals(adresse.getPostkode() + " " + adresse.getBySted(), response.getAdresselinje2());
		assertNull(response.getAdresselinje3());

		assertEquals(POSTADRESSE_UTLAND, response.getAdresseType());
		assertEquals(CANADA_ALPHA2_LANDKODE, response.getLandkode());
		assertNull(response.getPostnummer());
		assertNull(response.getPoststed());
		assertEquals(KONTAKTADRESSE, response.getAdressekilde());
	}

	@Test
	public void ShouldMapKontaktadresseForUtlandWithKosovoAlpha3Landkode() {
		UtenlandskAdresse adresse = PDLResponseUtil.createUtenlandskAdresse(KOSOVO_LANDKODE_NAV_REGISTRENE);
		Kontaktadresse kontaktadresse = Kontaktadresse.builder()
				.UtenlandskAdresse(adresse)
				.type(POSTADRESSE_UTLAND)
				.build();
		kontaktadresse.setMetadata(Metadata.builder().master(PDL.name()).build());

		HentPerson hentPerson = createHentePersonBuilder()
				.folkeregisterpersonstatus(singletonList(createFolkeregisterpersonstatus(PERSONSTATUS_MIDLERTIDIG)))
				.kontaktadresse(singletonList(kontaktadresse))
				.build();

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null, TEMA);

		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals(adresse.getPostboksNummerNavn(), response.getAdresselinje1());
		assertEquals(adresse.getPostkode() + " " + adresse.getBySted(), response.getAdresselinje2());
		assertNull(response.getAdresselinje3());

		assertEquals(POSTADRESSE_UTLAND, response.getAdresseType());
		assertEquals(XK.name(), response.getLandkode());
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

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null, TEMA);

		assertNotNull(mottakerInfo);
		assertEquals(UNKNOWN_LANDKODE, mottakerInfo.getPostadresse().getLandkode());
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

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null, TEMA);

		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals(UTENLANDSK_POSTBOKSNUMMERNAVN, response.getAdresselinje1());
		assertEquals(UTENLANDSK_POSTKODE, response.getAdresselinje2());
		assertEquals(UTENLANDSK_BYSTED, response.getAdresselinje3());

		assertEquals(POSTADRESSE_UTLAND, response.getAdresseType());
		assertEquals(CANADA_ALPHA2_LANDKODE, response.getLandkode());
		assertNull(response.getPostnummer());
		assertNull(response.getPoststed());
		assertEquals(KONTAKTADRESSE, response.getAdressekilde());
	}

	@Test
	public void shouldThrowUkjentAdresseExceptionWhenNoAddess() {
		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(createPdlHentPersonWithVegadresse());

		assertThrows(UkjentAdresseException.class,
				() -> mapPDLResponse.mapHentPerson(PDLResponseUtil.createPdlHentPersonWithNoAdresse(), SERVICE_CODE_TREG002, TEMA), "Fant ikke adresse for personen i PDL");
	}

	@Test
	public void shouldThrowFunctionalExceptionWhenPersonnavnIsNull() {
		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(createPdlHentPerson(null));
		RegoppslagIllegalArgumentException e = assertThrows(RegoppslagIllegalArgumentException.class, () ->
				mapPDLResponse.mapHentPerson(createPdlHentPerson(null), SERVICE_CODE_TREG002, TEMA));
		assertEquals(BAD_REQUEST, e.getHttpStatus());
		assertEquals("Feltet Personnavn kan ikke være null eller tomt", e.getMessage());
	}

	@Test
	public void shouldThrowFunctionalExceptionWhenFornavnIsNull() {
		HentPerson.PersonNavn personNavn = createPersonnavn();
		personNavn.setFornavn(null);
		RegoppslagIllegalArgumentException e = assertThrows(RegoppslagIllegalArgumentException.class, () ->
				mapPDLResponse.mapHentPerson(createPdlHentPerson(personNavn), SERVICE_CODE_TREG002, TEMA));
		assertEquals(BAD_REQUEST, e.getHttpStatus());
		assertEquals("Feltet Fornavn kan ikke være null eller tomt", e.getMessage());
	}

	@Test
	public void shouldThrowFunctionalExceptionWhenEtternavnIsNull() {
		HentPerson.PersonNavn personNavn = createPersonnavn();
		personNavn.setEtternavn(null);
		RegoppslagIllegalArgumentException e = assertThrows(RegoppslagIllegalArgumentException.class, () ->
				mapPDLResponse.mapHentPerson(createPdlHentPerson(personNavn), SERVICE_CODE_TREG002, TEMA));
		assertEquals(BAD_REQUEST, e.getHttpStatus());
		assertEquals("Feltet Etternavn kan ikke være null eller tomt", e.getMessage());
	}

	@Test
	public void shouldThrowFunctionalExceptionForUkjentBostedMottaker() {
		HentPerson hentPerson = createBostedsadresseWithUkjentBosted();

		UkjentAdresseException e = assertThrows(UkjentAdresseException.class, () ->
				mapPDLResponse.mapHentPerson(hentPerson, SERVICE_CODE_TREG002, TEMA));
		assertEquals(NOT_FOUND, e.getHttpStatus());
		assertEquals("TREG002: Kunne ikke mappe postadresse for UkjentBosted mottaker", e.getMessage());
	}

	@Test
	public void shouldThrowFunctionalExceptionForUkjentBostedMedStatusUtflyttet() {
		HentPerson hentPerson = createPdlHentPersonStatusUtflyttet();

		UkjentAdresseException e = assertThrows(UkjentAdresseException.class, () ->
				mapPDLResponse.mapHentPerson(hentPerson, SERVICE_CODE_TREG002, TEMA));
		assertEquals(NOT_FOUND, e.getHttpStatus());
		assertEquals("Fant ikke adresse for personen i PDL, med status=utflyttet og kilde=KILDE_DSF", e.getMessage());
	}

	@Test
	public void shouldmapVegadresseWhenCoAdressenavnIsSett() {
		Vegadresse vegadresse = createVegadresse();

		HentPerson hentPerson = createHentePersonBuilder()
				.folkeregisterpersonstatus(List.of(createFolkeregisterpersonstatus(PERSONSTATUS_DOED)))
				.kontaktadresse(List.of(Kontaktadresse.builder()
						.gyldigFraOgMed(LocalDateTime.now().minusDays(2))
						.gyldigTilOgMed(LocalDateTime.now().plusYears(10))
						.type(POSTADRESSE_INNLAND)
						.coAdressenavn(V_ADRESSENAVN)
						.vegadresse(vegadresse)
						.metadata(Metadata.builder().master(FREG.name()).build())
						.build()))
				.build();

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null, TEMA);

		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals(V_ADRESSENAVN, response.getAdresselinje1());
		assertEquals(ADRESSENAVN_1, response.getAdresselinje2());
		assertNull(response.getAdresselinje3());

		assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		assertEquals(vegadresse.getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
	}

	@Test
	public void shouldMapVegadresseWhenCoAdressenavnIsNull() {
		Vegadresse vegadresse = createVegadresse();
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

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null, TEMA);

		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals(vegadresse.getAdressenavn() + " " + vegadresse.getHusnummer(), response.getAdresselinje1());
		assertNull(response.getAdresselinje2());
		assertNull(response.getAdresselinje3());

		assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		assertEquals(vegadresse.getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
	}

	@ParameterizedTest
	@CsvSource(value = {
			"1, 2, BOSTEDSADRESSE",
			"2, 2, KONTAKTADRESSE",
			"1, null, KONTAKTADRESSE",
			"null, 2, KONTAKTADRESSE",
			"null, null, KONTAKTADRESSE"
	}, nullValues={"null"})
	public void shouldMapBostedsadresseIfNewerThanKontaktadresseElseKontaktadresse(String bostedsadresseDager, String kontaktadresseDager, String adresseKilde) {
		HentPerson hentPerson = createPdlHentPersonWithBostedsadresseAndKontaktadresse(
				createBostedsAdresseWithAntallDager(bostedsadresseDager),
				createKontaktAdresseWithAntallDager(kontaktadresseDager)
		);

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null, TEMA);

		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals(adresseKilde, response.getAdressekilde().name());
	}


	@ParameterizedTest
	@CsvSource(value = {
			"1, 2, BOSTEDSADRESSE",
			"2, 2, OPPHOLDSADRESSE",
			"2, null, OPPHOLDSADRESSE",
			"null, 2, OPPHOLDSADRESSE",
			"null, null, OPPHOLDSADRESSE"
	}, nullValues={"null"})
	public void shouldMapBostedsadresseIfNewerThanOppholdsadresseElseOppholdsadresse(String bostedsadresseDager, String oppholdsadresseDager, String adresseKilde) {
		HentPerson hentPerson = createPdlHentPersonWithBostedsadresseAndOppholdsAdresse(
				createBostedsAdresseWithAntallDager(bostedsadresseDager),
				createOppholdsAdresseWithAntallDager(oppholdsadresseDager));

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null, TEMA);

		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals(adresseKilde, response.getAdressekilde().name());
	}

}
