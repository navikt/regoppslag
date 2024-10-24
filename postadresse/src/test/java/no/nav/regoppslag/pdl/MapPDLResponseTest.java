package no.nav.regoppslag.pdl;

import lombok.SneakyThrows;
import no.nav.regoppslag.consumer.pdl.PdlGraphQLConsumer;
import no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode;
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
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static com.neovisionaries.i18n.CountryCode.XK;
import static java.util.Collections.singletonList;
import static no.nav.regoppslag.config.TimeConfig.OSLO_ZONE;
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
import static no.nav.regoppslag.domain.DomainConstants.KOSOVO_LANDKODE_NAV_REGISTRENE;
import static no.nav.regoppslag.domain.DomainConstants.UNKNOWN_LANDKODE;
import static no.nav.regoppslag.pdl.MapPDLResponse.UKJENT_ADRESSE_REASON_CODE;
import static no.nav.regoppslag.util.DomainConstants.SERVICE_CODE_TREG002;
import static no.nav.regoppslag.util.PDLResponseUtil.ADRESSENAVN_1;
import static no.nav.regoppslag.util.PDLResponseUtil.CANADA_ALPHA2_LANDKODE;
import static no.nav.regoppslag.util.PDLResponseUtil.CANADA_ALPHA3_LANDKODE;
import static no.nav.regoppslag.util.PDLResponseUtil.FULLT_NAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.GYLDIG_FRA_MED_DATO;
import static no.nav.regoppslag.util.PDLResponseUtil.GYLDIG_TIL_MED_DATO;
import static no.nav.regoppslag.util.PDLResponseUtil.KORT_NAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.LANDKODE_NORGE;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTNUMMER;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTSTED;
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
import static no.nav.regoppslag.util.PDLResponseUtil.createUtenlandskAdresse;
import static no.nav.regoppslag.util.PDLResponseUtil.createUtenlandskAdresseIFrittFormat;
import static no.nav.regoppslag.util.PDLResponseUtil.createUtenlandskBostedsAdresseWithAntallDager;
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

	private static final String POSTBOKS = "postboks 73";

	private PdlGraphQLConsumer pdlGraphQLConsumer;
	private MapPDLResponse mapPDLResponse;
	private DoedsboAdresseService doedsboAdresseService;

	@InjectMocks
	private PostnummerService postnummerService;

	@BeforeEach
	public void setUp() {
		pdlGraphQLConsumer = mock(PdlGraphQLConsumer.class);

		doedsboAdresseService = Mockito.spy(new DoedsboAdresseService(postnummerService, pdlGraphQLConsumer));
		mapPDLResponse = new MapPDLResponse(doedsboAdresseService, new NorskAdresseService(postnummerService), Clock.system(OSLO_ZONE));
	}

	@Test
	public void shouldDelegateToDoedsboserviceWhenPersonIsDead() {
		List<KontaktinformasjonForDoedsbo> kontaktinformasjon = singletonList(createKontaktinformasjonForDoedsbo());
		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(createPdlHentPersonWithPersonDoedOgAdvokatSomKontakt(kontaktinformasjon));
		mapPDLResponse.mapHentPerson(createPdlHentPersonWithPersonDoedOgAdvokatSomKontakt(kontaktinformasjon), SERVICE_CODE_TREG002);

		verify(doedsboAdresseService, times(1)).mapFoerDoedsbo(any());
	}

	@Test
	public void shouldMapMidlertidigKontaktWithVegadresse() {
		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(createPdlHentPersonWithVegadresse());
		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(createPdlHentPersonWithVegadresse(), SERVICE_CODE_TREG002);

		assertNull(mottakerInfo.getDoedsdato());
		assertEquals(FULLT_NAVN, mottakerInfo.getNavn());
		assertEquals(ADRESSENAVN_1, mottakerInfo.getPostadresse().getAdresselinje1());
		assertNull(mottakerInfo.getPostadresse().getAdresselinje2());
		assertEquals(POSTADRESSE_INNLAND, mottakerInfo.getPostadresse().getAdresseType());
		assertEquals(POSTNUMMER, mottakerInfo.getPostadresse().getPostnummer());
		assertEquals(POSTSTED, mottakerInfo.getPostadresse().getPoststed());
		assertEquals(LANDKODE_NORGE, mottakerInfo.getPostadresse().getLandkode());
		assertEquals(KONTAKTADRESSE, mottakerInfo.getPostadresse().getAdressekilde());
		verify(doedsboAdresseService, times(0)).mapFoerDoedsbo(any());
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
		assertEquals(OPPHOLDSADRESSE, mottakerInfo.getPostadresse().getAdressekilde());
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

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, SERVICE_CODE_TREG002);

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

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null);

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

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null);

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
		Kontaktadresse.Postboksadresse adresse = createPostboksadresse("73");
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
	public void shouldMapPostboksAdresseWhichStartsWithPostboks() throws RegoppslagIllegalArgumentException {
		Kontaktadresse.Postboksadresse adresse = createPostboksadresse(POSTBOKS);
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

		assertEquals("C/O Byggfirma A/S", response.getAdresselinje1());
		assertEquals(POSTBOKS, response.getAdresselinje2());
		assertNull(response.getAdresselinje3());

		assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		assertEquals(adresse.getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
		assertEquals(KONTAKTADRESSE, response.getAdressekilde());
	}

	@Test
	public void shouldMapInnlandKontaktadresseWithPostboksAdresseAndFREGAsSource() {
		Kontaktadresse.Postboksadresse adresse = createPostboksadresse("73");
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
	public void shouldMapKontaktadresseForUtlandWithUtlandsAddresse() {
		UtenlandskAdresse adresse = createUtenlandskAdresse(CANADA_ALPHA3_LANDKODE);
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
		assertEquals(adresse.getBySted() + " " + adresse.getPostkode(), response.getAdresselinje2());
		assertNull(response.getAdresselinje3());

		assertEquals(POSTADRESSE_UTLAND, response.getAdresseType());
		assertEquals(CANADA_ALPHA2_LANDKODE, response.getLandkode());
		assertNull(response.getPostnummer());
		assertNull(response.getPoststed());
		assertEquals(KONTAKTADRESSE, response.getAdressekilde());
	}

	@Test
	public void shouldMapKontaktadresseForUtlandWithKosovoAlpha3Landkode() {
		UtenlandskAdresse adresse = createUtenlandskAdresse(KOSOVO_LANDKODE_NAV_REGISTRENE);
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
		assertEquals(adresse.getPostkode() + " " + adresse.getBySted(), response.getAdresselinje2());
		assertNull(response.getAdresselinje3());

		assertEquals(POSTADRESSE_UTLAND, response.getAdresseType());
		assertEquals(XK.name(), response.getLandkode());
		assertNull(response.getPostnummer());
		assertNull(response.getPoststed());
	}

	@Test
	public void shouldThrowExceptionWhenLandKodeUtlandsAddresseIsNull() {
		UtenlandskAdresse adresse = createUtenlandskAdresse(null);
		Kontaktadresse kontaktadresse = Kontaktadresse.builder()
				.UtenlandskAdresse(adresse)
				.type(POSTADRESSE_UTLAND)
				.metadata(Metadata.builder().master(PDL.name()).build())
				.build();

		HentPerson hentPerson = createHentePersonBuilder()
				.folkeregisterpersonstatus(singletonList(createFolkeregisterpersonstatus(PERSONSTATUS_MIDLERTIDIG)))
				.kontaktadresse(singletonList(kontaktadresse))
				.build();

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null);

		assertNotNull(mottakerInfo);
		assertEquals(UNKNOWN_LANDKODE, mottakerInfo.getPostadresse().getLandkode());
	}

	@Test
	public void shouldMapKontaktadresseForUtlandWithUtenlandskAdresseIFrittFormat() {
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
		assertEquals(CANADA_ALPHA2_LANDKODE, response.getLandkode());
		assertNull(response.getPostnummer());
		assertNull(response.getPoststed());
		assertEquals(KONTAKTADRESSE, response.getAdressekilde());
	}

	@Test
	public void shouldThrowUkjentAdresseExceptionWhenNoAddess() {
		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(createPdlHentPersonWithVegadresse());

		assertThrows(UkjentAdresseException.class,
				() -> mapPDLResponse.mapHentPerson(PDLResponseUtil.createPdlHentPersonWithNoAdresse(), SERVICE_CODE_TREG002), "Fant ikke adresse for personen i PDL");
	}

	@Test
	public void shouldThrowFunctionalExceptionWhenPersonnavnIsNull() {
		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(createPdlHentPerson(null));
		RegoppslagIllegalArgumentException e = assertThrows(RegoppslagIllegalArgumentException.class, () ->
				mapPDLResponse.mapHentPerson(createPdlHentPerson(null), SERVICE_CODE_TREG002));
		assertEquals(BAD_REQUEST, e.getHttpStatusCode());
		assertEquals("Feltet Personnavn kan ikke være null eller tomt", e.getMessage());
	}

	@Test
	public void shouldThrowFunctionalExceptionWhenFornavnIsNull() {
		HentPerson.PersonNavn personNavn = createPersonnavn();
		personNavn.setFornavn(null);
		RegoppslagIllegalArgumentException e = assertThrows(RegoppslagIllegalArgumentException.class, () ->
				mapPDLResponse.mapHentPerson(createPdlHentPerson(personNavn), SERVICE_CODE_TREG002));
		assertEquals(BAD_REQUEST, e.getHttpStatusCode());
		assertEquals("Feltet Fornavn kan ikke være null eller tomt", e.getMessage());
	}

	@Test
	public void shouldThrowFunctionalExceptionWhenEtternavnIsNull() {
		HentPerson.PersonNavn personNavn = createPersonnavn();
		personNavn.setEtternavn(null);
		RegoppslagIllegalArgumentException e = assertThrows(RegoppslagIllegalArgumentException.class, () ->
				mapPDLResponse.mapHentPerson(createPdlHentPerson(personNavn), SERVICE_CODE_TREG002));
		assertEquals(BAD_REQUEST, e.getHttpStatusCode());
		assertEquals("Feltet Etternavn kan ikke være null eller tomt", e.getMessage());
	}

	@Test
	public void shouldThrowFunctionalExceptionForUkjentBostedMottaker() {
		HentPerson hentPerson = createBostedsadresseWithUkjentBosted();

		UkjentAdresseException e = assertThrows(UkjentAdresseException.class, () ->
				mapPDLResponse.mapHentPerson(hentPerson, SERVICE_CODE_TREG002));
		assertEquals(NOT_FOUND, e.getStatusCode());
		assertEquals("Fant ikke bostedsadresse for personen i PDL", e.getReason());
		assertEquals(UKJENT_ADRESSE_REASON_CODE, e.getReasonCode());
	}

	@Test
	public void shouldThrowFunctionalExceptionForUkjentBostedMedStatusUtflyttet() {
		HentPerson hentPerson = createPdlHentPersonStatusUtflyttet();

		UkjentAdresseException e = assertThrows(UkjentAdresseException.class, () ->
				mapPDLResponse.mapHentPerson(hentPerson, SERVICE_CODE_TREG002));
		assertEquals(NOT_FOUND, e.getStatusCode());
		assertEquals("Fant ikke adresse for personen i PDL, med status=utflyttet og kilde=KILDE_DSF", e.getReason());
		assertEquals(UKJENT_ADRESSE_REASON_CODE, e.getReasonCode());
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

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null);

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

	static Stream<Arguments> shouldMapBostedsadresseIfNewerThanKontaktadresseElseKontaktadresse() {
		return Stream.of(
				Arguments.of(createPdlHentPersonWithBostedsadresseAndKontaktadresse(
						createBostedsAdresseWithAntallDager(1, 1),
						createKontaktAdresseWithAntallDager(2, 2)
				), KONTAKTADRESSE),
				Arguments.of(createPdlHentPersonWithBostedsadresseAndKontaktadresse(
						createBostedsAdresseWithAntallDager(null, 1),
						createKontaktAdresseWithAntallDager(3, 3)
				), KONTAKTADRESSE),
				Arguments.of(createPdlHentPersonWithBostedsadresseAndKontaktadresse(
						createUtenlandskBostedsAdresseWithAntallDager(1, 1),
						createKontaktAdresseWithAntallDager(2, 2)
				), BOSTEDSADRESSE),
				Arguments.of(createPdlHentPersonWithBostedsadresseAndKontaktadresse(
						createUtenlandskBostedsAdresseWithAntallDager(null, 1),
						createKontaktAdresseWithAntallDager(3, 3)
				), BOSTEDSADRESSE),
				Arguments.of(createPdlHentPersonWithBostedsadresseAndKontaktadresse(
						createBostedsAdresseWithAntallDager(2, 1),
						createKontaktAdresseWithAntallDager(2, 2)
				), KONTAKTADRESSE),
				Arguments.of(createPdlHentPersonWithBostedsadresseAndKontaktadresse(
						createBostedsAdresseWithAntallDager(2, 2),
						createKontaktAdresseWithAntallDager(null, 1)
				), KONTAKTADRESSE),
				Arguments.of(createPdlHentPersonWithBostedsadresseAndKontaktadresse(
						createBostedsAdresseWithAntallDager(null, 3),
						createKontaktAdresseWithAntallDager(2, 2)
				), KONTAKTADRESSE),
				Arguments.of(createPdlHentPersonWithBostedsadresseAndKontaktadresse(
						createBostedsAdresseWithAntallDager(null, 3),
						createKontaktAdresseWithAntallDager(null, 2)
				), KONTAKTADRESSE)
		);
	}

	@ParameterizedTest
	@MethodSource
	public void shouldMapBostedsadresseIfNewerThanKontaktadresseElseKontaktadresse(HentPerson hentPerson, AdresseKildeCode adresseKilde) {
		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null);

		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals(adresseKilde, response.getAdressekilde());
	}

	static Stream<Arguments> shouldMapBostedsadresseIfNewerThanOppholdsadresseElseOppholdsadresse() {
		return Stream.of(
				Arguments.of(createPdlHentPersonWithBostedsadresseAndOppholdsAdresse(
						createBostedsAdresseWithAntallDager(1, 1),
						createOppholdsAdresseWithAntallDager(2, 2)
				), OPPHOLDSADRESSE),
				Arguments.of(createPdlHentPersonWithBostedsadresseAndOppholdsAdresse(
						createBostedsAdresseWithAntallDager(null, 1),
						createOppholdsAdresseWithAntallDager(2, 2)
				), OPPHOLDSADRESSE),
				Arguments.of(createPdlHentPersonWithBostedsadresseAndOppholdsAdresse(
						createUtenlandskBostedsAdresseWithAntallDager(1, 1),
						createOppholdsAdresseWithAntallDager(2, 2)
				), BOSTEDSADRESSE),
				Arguments.of(createPdlHentPersonWithBostedsadresseAndOppholdsAdresse(
						createUtenlandskBostedsAdresseWithAntallDager(null, 1),
						createOppholdsAdresseWithAntallDager(2, 2)
				), BOSTEDSADRESSE),
				Arguments.of(createPdlHentPersonWithBostedsadresseAndOppholdsAdresse(
						createBostedsAdresseWithAntallDager(2, 1),
						createOppholdsAdresseWithAntallDager(2, 2)
				), OPPHOLDSADRESSE),
				Arguments.of(createPdlHentPersonWithBostedsadresseAndOppholdsAdresse(
						createBostedsAdresseWithAntallDager(2, 2),
						createOppholdsAdresseWithAntallDager(null, 1)
				), OPPHOLDSADRESSE),
				Arguments.of(createPdlHentPersonWithBostedsadresseAndOppholdsAdresse(
						createBostedsAdresseWithAntallDager(null, 3),
						createOppholdsAdresseWithAntallDager(2, 2)
				), OPPHOLDSADRESSE),
				Arguments.of(createPdlHentPersonWithBostedsadresseAndOppholdsAdresse(
						createBostedsAdresseWithAntallDager(null, 3),
						createOppholdsAdresseWithAntallDager(null, 2)
				), OPPHOLDSADRESSE)
		);
	}

	@ParameterizedTest
	@MethodSource
	public void shouldMapBostedsadresseIfNewerThanOppholdsadresseElseOppholdsadresse(HentPerson hentPerson, AdresseKildeCode adresseKilde) {
		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null);

		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals(adresseKilde, response.getAdressekilde());
	}

}
