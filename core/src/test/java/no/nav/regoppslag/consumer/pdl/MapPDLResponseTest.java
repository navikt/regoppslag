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
import no.nav.regoppslag.service.LandkodeService;
import no.nav.regoppslag.service.PostnummerService;
import no.nav.regoppslag.util.PDLResponseUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.neovisionaries.i18n.CountryCode.XK;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.nav.regoppslag.consumer.pdl.to.InformasjonKilde.FREG;
import static no.nav.regoppslag.consumer.pdl.to.InformasjonKilde.PDL;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.PERSONSTATUS_BOSATT;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.PERSONSTATUS_DOED;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.PERSONSTATUS_MIDLERTIDIG;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_INNLAND;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_UTLAND;
import static no.nav.regoppslag.metrics.MetricLabels.KOSOVO;
import static no.nav.regoppslag.metrics.MetricLabels.KOSOVO_LANDKODE_NAV_REGISTRENE;
import static no.nav.regoppslag.metrics.MetricLabels.SERVICE_CODE_TREG002;
import static no.nav.regoppslag.metrics.MetricLabels.UNKNOWN_LANDKODE;
import static no.nav.regoppslag.util.PDLResponseUtil.ADRESSENAVN_1;
import static no.nav.regoppslag.util.PDLResponseUtil.CANADA;
import static no.nav.regoppslag.util.PDLResponseUtil.CANADA_ALPHA2_LANDKODE;
import static no.nav.regoppslag.util.PDLResponseUtil.CANADA_ALPHA3_LANDKODE;
import static no.nav.regoppslag.util.PDLResponseUtil.CO_ORGINASJON_NAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.DOEDSDATO;
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
import static no.nav.regoppslag.util.PDLResponseUtil.createPdlHentPersonStatusUtflyttet;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
	private LandkodeService landkodeService;

	@InjectMocks
	private PostnummerService postnummerService;


	@BeforeEach
	public void setUp() {
		landkodeService = new LandkodeService();
		pdlGraphQLConsumer = mock(PdlGraphQLConsumer.class);
		mapPDLResponse = new MapPDLResponse(postnummerService, landkodeService, pdlGraphQLConsumer);
	}

	@Test
	public void shouldMapMottakerInfoForDoedWithAdvokatSomKontakt() {
		List<KontaktinformasjonForDoedsbo> kontaktinformasjon = singletonList(createKontaktinformasjonForDoedsbo());
		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(createPdlHentPersonWithPersonDoedOgAdvokatSomKontakt(kontaktinformasjon));
		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(createPdlHentPersonWithPersonDoedOgAdvokatSomKontakt(kontaktinformasjon), SERVICE_CODE_TREG002, TEMA);

		assertEquals(FOEDSELDATO, mottakerInfo.getFoedselsdato());
		assertEquals(DOEDSDATO, mottakerInfo.getDoedsdato());
		assertEquals(FULLT_NAVN, mottakerInfo.getNavn());
		assertEquals(V_ADRESSENAVN, mottakerInfo.getPostadresse().getAdresselinje1());
		assertEquals(ADRESSENAVN_1, mottakerInfo.getPostadresse().getAdresselinje2());
		Assertions.assertEquals(POSTADRESSE_INNLAND, mottakerInfo.getPostadresse().getAdresseType());
		assertEquals(POSTNUMMER, mottakerInfo.getPostadresse().getPostnummer());
		assertEquals(POSTSTED, mottakerInfo.getPostadresse().getPoststed());
		assertEquals(LANDKODE_NORGE, mottakerInfo.getPostadresse().getLandkode());
	}

	@Test
	public void shouldMapMidlertidigKontaktWithVegadresse() {
		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(createPdlHentPersonWithVegadresse());
		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(createPdlHentPersonWithVegadresse(), SERVICE_CODE_TREG002, TEMA);

		assertEquals(FOEDSELDATO, mottakerInfo.getFoedselsdato());
		Assertions.assertNull(mottakerInfo.getDoedsdato());
		assertEquals(FULLT_NAVN, mottakerInfo.getNavn());
		assertEquals(ADRESSENAVN_1, mottakerInfo.getPostadresse().getAdresselinje1());
		Assertions.assertNull(mottakerInfo.getPostadresse().getAdresselinje2());
		Assertions.assertEquals(POSTADRESSE_INNLAND, mottakerInfo.getPostadresse().getAdresseType());
		assertEquals(POSTNUMMER, mottakerInfo.getPostadresse().getPostnummer());
		assertEquals(POSTSTED, mottakerInfo.getPostadresse().getPoststed());
		assertEquals(LANDKODE_NORGE, mottakerInfo.getPostadresse().getLandkode());
	}

	@SneakyThrows
	@Test
	public void shouldMapMidlertidigKontaktWithOppholdsadresse() {
		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(createPdlHentPersonWithVegadresse());
		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(createPdlHentPersonWithOppholdsadresse(), SERVICE_CODE_TREG002, TEMA);

		Assertions.assertNull(mottakerInfo.getDoedsdato());
		assertEquals(FULLT_NAVN, mottakerInfo.getNavn());
		assertEquals(KORT_NAVN, mottakerInfo.getKortNavn());
		assertEquals(ADRESSENAVN_1, mottakerInfo.getPostadresse().getAdresselinje1());
		Assertions.assertNull(mottakerInfo.getPostadresse().getAdresselinje2());
		Assertions.assertEquals(POSTADRESSE_INNLAND, mottakerInfo.getPostadresse().getAdresseType());
		assertEquals(POSTNUMMER, mottakerInfo.getPostadresse().getPostnummer());
		assertEquals(POSTSTED, mottakerInfo.getPostadresse().getPoststed());
		assertEquals(LANDKODE_NORGE, mottakerInfo.getPostadresse().getLandkode());
	}

	@SneakyThrows
	@Test
	public void shouldMapMidlertidigKontaktWithBostedsadresse() {
		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(createPdlHentPersonWithVegadresse());
		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(createPdlHentPersonWithBostedsadresse(), SERVICE_CODE_TREG002, TEMA);

		Assertions.assertNull(mottakerInfo.getDoedsdato());
		assertEquals(FULLT_NAVN, mottakerInfo.getNavn());
		assertEquals(KORT_NAVN, mottakerInfo.getKortNavn());
		assertEquals(ADRESSENAVN_1, mottakerInfo.getPostadresse().getAdresselinje1());
		Assertions.assertNull(mottakerInfo.getPostadresse().getAdresselinje2());
		Assertions.assertEquals(POSTADRESSE_INNLAND, mottakerInfo.getPostadresse().getAdresseType());
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

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, SERVICE_CODE_TREG002, TEMA);

		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals(ADRESSENAVN_1, response.getAdresselinje1());
		Assertions.assertNull(response.getAdresselinje2());
		Assertions.assertNull(response.getAdresselinje3());

		Assertions.assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		Assertions.assertEquals(adresse.getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
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
		Assertions.assertEquals(adresse.getAdresselinje1(), response.getAdresselinje2());
		Assertions.assertEquals(adresse.getAdresselinje2(), response.getAdresselinje3());

		Assertions.assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		Assertions.assertEquals(adresse.getPostnummer(), response.getPostnummer());
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

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null, TEMA);

		PostadresseTo response = mottakerInfo.getPostadresse();

		Assertions.assertEquals(adresse.getAdresselinje1(), response.getAdresselinje1());
		Assertions.assertEquals(adresse.getAdresselinje2(), response.getAdresselinje2());
		Assertions.assertEquals(adresse.getAdresselinje3(), response.getAdresselinje3());

		Assertions.assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		Assertions.assertEquals(adresse.getPostnummer(), response.getPostnummer());
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

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null, TEMA);

		PostadresseTo response = mottakerInfo.getPostadresse();

		Assertions.assertEquals("C/O Byggfirma A/S", response.getAdresselinje1());
		Assertions.assertEquals("Postboks " + adresse.getPostboks(), response.getAdresselinje2());
		Assertions.assertNull(response.getAdresselinje3());

		Assertions.assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		Assertions.assertEquals(adresse.getPostnummer(), response.getPostnummer());
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

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null, TEMA);

		PostadresseTo response = mottakerInfo.getPostadresse();

		Assertions.assertEquals("C/O Byggfirma A/S", response.getAdresselinje1());
		Assertions.assertEquals("Postboks " + adresse.getPostboks(), response.getAdresselinje2());
		Assertions.assertNull(response.getAdresselinje3());

		Assertions.assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		Assertions.assertEquals(adresse.getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
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

		Assertions.assertEquals(adresse.getPostboksNummerNavn(), response.getAdresselinje1());
		Assertions.assertEquals(adresse.getPostkode() + " " + adresse.getBySted(), response.getAdresselinje2());
		assertEquals(CANADA, response.getAdresselinje3());

		Assertions.assertEquals(POSTADRESSE_UTLAND, response.getAdresseType());
		assertEquals(CANADA_ALPHA2_LANDKODE, response.getLandkode());
		Assertions.assertNull(response.getPostnummer());
		Assertions.assertNull(response.getPoststed());
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

		Assertions.assertEquals(adresse.getPostboksNummerNavn(), response.getAdresselinje1());
		Assertions.assertEquals(adresse.getPostkode() + " " + adresse.getBySted(), response.getAdresselinje2());
		Assertions.assertEquals(KOSOVO, response.getAdresselinje3());

		Assertions.assertEquals(POSTADRESSE_UTLAND, response.getAdresseType());
		Assertions.assertEquals(CountryCode.XK.name(), response.getLandkode());
		Assertions.assertNull(response.getPostnummer());
		Assertions.assertNull(response.getPoststed());
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

		Assertions.assertNotNull(mottakerInfo);
		Assertions.assertEquals(UNKNOWN_LANDKODE, mottakerInfo.getPostadresse().getLandkode());
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

		Assertions.assertEquals(POSTADRESSE_UTLAND, response.getAdresseType());
		assertEquals(CANADA_ALPHA2_LANDKODE, response.getLandkode());
		Assertions.assertNull(response.getPostnummer());
		Assertions.assertNull(response.getPoststed());
	}

	@Test
	public void shlouldMapKontaktinformasjonForDoedsWithAdvokatAsContact() {
		KontaktinformasjonForDoedsbo kontaktinformasjon = createKontaktinformasjonForDoedsbo();

		HentPerson hentPerson = createHentePersonBuilder()
				.doedsfall(singletonList(createDoedsfall(DOEDSDATO)))
				.folkeregisterpersonstatus(singletonList(createFolkeregisterpersonstatus(PERSONSTATUS_DOED)))
				.kontaktinformasjonForDoedsbo(singletonList(kontaktinformasjon))
				.build();

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null, TEMA);

		PostadresseTo response = mottakerInfo.getPostadresse();


		assertEquals(V_ADRESSENAVN, response.getAdresselinje1());
		Assertions.assertEquals(kontaktinformasjon.getAdresse().getAdresselinje1(), response.getAdresselinje2());
		Assertions.assertNull(response.getAdresselinje3());

		Assertions.assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		Assertions.assertEquals(kontaktinformasjon.getAdresse().getPostnummer(), response.getPostnummer());
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

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null, TEMA);

		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals(V_ADRESSENAVN, response.getAdresselinje1());
		Assertions.assertEquals(kontaktinformasjon.getAdresse().getAdresselinje1(), response.getAdresselinje2());
		Assertions.assertNull(response.getAdresselinje3());

		Assertions.assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		Assertions.assertEquals(kontaktinformasjon.getAdresse().getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
	}

	@Test
	public void shouldMapKontaktinformasjonForDoedsboSomHenteKontaktFraPDL() {
		KontaktinformasjonForDoedsbo kontaktinformasjon = PDLResponseUtil.createKontaktinformasjonForDoedsboWithPerson();
		kontaktinformasjon.getPersonSomKontakt().setPersonnavn(null);
		HentPerson hentPerson = createHentePersonBuilder()
				.doedsfall(singletonList(createDoedsfall(DOEDSDATO)))
				.folkeregisterpersonstatus(singletonList(createFolkeregisterpersonstatus(PERSONSTATUS_DOED)))
				.kontaktinformasjonForDoedsbo(singletonList(kontaktinformasjon))
				.build();

		when(pdlGraphQLConsumer.hentDoedsBoKontaktPersonnavn(anyString(), anyString())).thenReturn(Optional.ofNullable(FULLT_NAVN));

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null, TEMA);

		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals("v/ " + FULLT_NAVN, response.getAdresselinje1());
		Assertions.assertEquals(kontaktinformasjon.getAdresse().getAdresselinje1(), response.getAdresselinje2());
		Assertions.assertNull(response.getAdresselinje3());

		Assertions.assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		Assertions.assertEquals(kontaktinformasjon.getAdresse().getPostnummer(), response.getPostnummer());
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

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null, TEMA);
		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals(CO_ORGINASJON_NAVN, response.getAdresselinje1());
		Assertions.assertEquals(kontaktinformasjon.getAdresse().getAdresselinje1(), response.getAdresselinje2());
		Assertions.assertNull(response.getAdresselinje3());

		Assertions.assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		Assertions.assertEquals(kontaktinformasjon.getAdresse().getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
	}

	@Test
	public void shouldMapKontaktinformasjonForDoedsboWithKontaktPersonNull() {
		KontaktinformasjonForDoedsbo kontaktinformasjon = createKontaktinformasjonForDoedsboWithOrginasjon(organisasjonSomKontakt(createNavnForOrginasjonSomKontakt()), createPersonKontaktAdresse());
		HentPerson hentPerson = createHentePersonBuilder()
				.doedsfall(singletonList(createDoedsfall(DOEDSDATO)))
				.folkeregisterpersonstatus(singletonList(createFolkeregisterpersonstatus(PERSONSTATUS_DOED)))
				.kontaktinformasjonForDoedsbo(singletonList(kontaktinformasjon))
				.build();

		kontaktinformasjon.getOrganisasjonSomKontakt().setKontaktperson(null);

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null, TEMA);
		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals(CO_ORGINASJON_NAVN, response.getAdresselinje1());
		Assertions.assertEquals(kontaktinformasjon.getAdresse().getAdresselinje1(), response.getAdresselinje2());
		Assertions.assertNull(response.getAdresselinje3());

		Assertions.assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		Assertions.assertEquals(kontaktinformasjon.getAdresse().getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
	}

	@Test
	public void shouldThrowUkjentAdresseExceptionWhenNoAddess() {
		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(createPdlHentPersonWithVegadresse());

		Assertions.assertThrows(UkjentAdresseException.class,
				() -> mapPDLResponse.mapHentPerson(PDLResponseUtil.createPdlHentPersonWithNoAdresse(), SERVICE_CODE_TREG002, TEMA), "Fant ikke adresse for personen i PDL");
	}

	@Test
	public void shouldThrowFunctionalExceptionIfPersonErDoedOgHarIngenAdresse() {
		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(createPdlHentPersonWithPersonDoedOgAdvokatSomKontakt(emptyList()));
		UkjentAdressePersonErDoed e = Assertions.assertThrows(UkjentAdressePersonErDoed.class, () ->
				mapPDLResponse.mapHentPerson(createPdlHentPersonWithPersonDoedOgAdvokatSomKontakt(emptyList()), SERVICE_CODE_TREG002, TEMA));
		assertEquals(HttpStatus.GONE, e.getHttpStatus());
		Assertions.assertEquals(FEILMELDING_PERSON_DOED, e.getMessage());
	}

	@Test
	public void shouldThrowFunctionalExceptionWhenPersonnavnIsNull() {
		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(createPdlHentPerson(null));
		RegoppslagIllegalArgumentException e = Assertions.assertThrows(RegoppslagIllegalArgumentException.class, () ->
				mapPDLResponse.mapHentPerson(createPdlHentPerson(null), SERVICE_CODE_TREG002, TEMA));
		assertEquals(HttpStatus.BAD_REQUEST, e.getHttpStatus());
		Assertions.assertEquals("Feltet Personnavn kan ikke være null eller tomt", e.getMessage());
	}

	@Test
	public void shouldThrowFunctionalExceptionWhenFornavnIsNull() {
		HentPerson.PersonNavn personNavn = createPersonnavn();
		personNavn.setFornavn(null);
		RegoppslagIllegalArgumentException e = Assertions.assertThrows(RegoppslagIllegalArgumentException.class, () ->
				mapPDLResponse.mapHentPerson(createPdlHentPerson(personNavn), SERVICE_CODE_TREG002, TEMA));
		assertEquals(HttpStatus.BAD_REQUEST, e.getHttpStatus());
		Assertions.assertEquals("Feltet Fornavn kan ikke være null eller tomt", e.getMessage());
	}

	@Test
	public void shouldThrowFunctionalExceptionWhenEtternavnIsNull() {
		HentPerson.PersonNavn personNavn = createPersonnavn();
		personNavn.setEtternavn(null);
		RegoppslagIllegalArgumentException e = Assertions.assertThrows(RegoppslagIllegalArgumentException.class, () ->
				mapPDLResponse.mapHentPerson(createPdlHentPerson(personNavn), SERVICE_CODE_TREG002, TEMA));
		assertEquals(HttpStatus.BAD_REQUEST, e.getHttpStatus());
		Assertions.assertEquals("Feltet Etternavn kan ikke være null eller tomt", e.getMessage());
	}

	@Test
	public void shouldThrowFunctionalExceptionForUkjentBostedMottaker() {
		HentPerson hentPerson = createBostedsadresseWithUkjentBosted();

		UkjentAdresseException e = Assertions.assertThrows(UkjentAdresseException.class, () ->
				mapPDLResponse.mapHentPerson(hentPerson, SERVICE_CODE_TREG002, TEMA));
		assertEquals(HttpStatus.NOT_FOUND, e.getHttpStatus());
		Assertions.assertEquals("TREG002: Kunne ikke mappe postadresse for UkjentBosted mottaker", e.getMessage());
	}

	@Test
	public void shouldThrowFunctionalExceptionForUkjentBostedMedStatusUtflyttet() {
		HentPerson hentPerson = createPdlHentPersonStatusUtflyttet();

		UkjentAdresseException e = Assertions.assertThrows(UkjentAdresseException.class, () ->
				mapPDLResponse.mapHentPerson(hentPerson, SERVICE_CODE_TREG002, TEMA));
		assertEquals(HttpStatus.NOT_FOUND, e.getHttpStatus());
		Assertions.assertEquals("Fant ikke adresse for personen i PDL, med status=utflyttet og kilde=KILDE_DSF", e.getMessage());
	}

	@Test
	public void shouldThrowExceptionWhenKontaktAdresseForDoedsboIsNull() {
		KontaktinformasjonForDoedsbo kontaktinformasjon = createKontaktinformasjonForDoedsboWithOrginasjon(organisasjonSomKontakt(createNavnForOrginasjonSomKontakt()), null);

		HentPerson hentPerson = createHentePersonBuilder()
				.doedsfall(singletonList(createDoedsfall(DOEDSDATO)))
				.folkeregisterpersonstatus(singletonList(createFolkeregisterpersonstatus(PERSONSTATUS_DOED)))
				.kontaktinformasjonForDoedsbo(singletonList(kontaktinformasjon))
				.build();

		UkjentAdressePersonErDoed e = Assertions.assertThrows(UkjentAdressePersonErDoed.class, () ->
				mapPDLResponse.mapHentPerson(hentPerson, null, TEMA));

		assertEquals(HttpStatus.GONE, e.getHttpStatus());
		Assertions.assertEquals(FEILMELDING_PERSON_DOED, e.getMessage());
	}

	@Test
	public void shouldThrowExceptionWhenKontakterAdresseForDoedsboWithOrginasjonIsNull() {
		KontaktinformasjonForDoedsbo kontaktinformasjon = createKontaktinformasjonForDoedsboWithOrginasjon(null, createPersonKontaktAdresse());

		HentPerson hentPerson = createHentePersonBuilder()
				.doedsfall(singletonList(createDoedsfall(DOEDSDATO)))
				.folkeregisterpersonstatus(singletonList(createFolkeregisterpersonstatus(PERSONSTATUS_DOED)))
				.kontaktinformasjonForDoedsbo(singletonList(kontaktinformasjon))
				.build();

		UkjentAdressePersonErDoed e = Assertions.assertThrows(UkjentAdressePersonErDoed.class, () -> mapPDLResponse.mapHentPerson(hentPerson, null, TEMA));

		assertEquals(HttpStatus.GONE, e.getHttpStatus());
		Assertions.assertEquals(FEILMELDING_PERSON_DOED, e.getMessage());
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
		UkjentAdressePersonErDoed e = Assertions.assertThrows(UkjentAdressePersonErDoed.class, () -> mapPDLResponse.mapHentPerson(hentPerson, "TREG002", TEMA),
				"Mottaker er registrert som død og har ugyldig postadresse");
		assertEquals(HttpStatus.GONE, e.getHttpStatus());
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
						.coAdressenavn(V_ADRESSENAVN)
						.vegadresse(vegadresse)
						.metadata(Metadata.builder().master(FREG.name()).build())
						.build()))
				.build();

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null, TEMA);

		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals(V_ADRESSENAVN, response.getAdresselinje1());
		assertEquals(ADRESSENAVN_1, response.getAdresselinje2());
		Assertions.assertNull(response.getAdresselinje3());

		Assertions.assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		Assertions.assertEquals(vegadresse.getPostnummer(), response.getPostnummer());
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

		PdlMottakerInfo mottakerInfo = mapPDLResponse.mapHentPerson(hentPerson, null, TEMA);

		PostadresseTo response = mottakerInfo.getPostadresse();

		Assertions.assertEquals(vegadresse.getAdressenavn() + " " + vegadresse.getHusnummer(), response.getAdresselinje1());
		Assertions.assertNull(response.getAdresselinje2());
		Assertions.assertNull(response.getAdresselinje3());

		Assertions.assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		Assertions.assertEquals(vegadresse.getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
	}
}
