package no.nav.regoppslag.pdl;


import no.nav.regoppslag.consumer.pdl.PdlGraphQLConsumer;
import no.nav.regoppslag.consumer.pdl.to.HentPerson;
import no.nav.regoppslag.consumer.pdl.to.KontaktinformasjonForDoedsbo;
import no.nav.regoppslag.consumer.pdl.to.PdlMottakerInfo;
import no.nav.regoppslag.consumer.pdl.to.PostadresseTo;
import no.nav.regoppslag.exceptions.UkjentAdressePersonErDoed;
import no.nav.regoppslag.service.PostnummerService;
import no.nav.regoppslag.util.PDLResponseUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode.KONTAKTINFORMASJONFORDØDSBO;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.PERSONSTATUS_DOED;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_INNLAND;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_UTLAND;
import static no.nav.regoppslag.util.PDLResponseUtil.ADRESSENAVN_1;
import static no.nav.regoppslag.util.PDLResponseUtil.CO_ORGINASJON_NAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.DOEDSDATO;
import static no.nav.regoppslag.util.PDLResponseUtil.FOEDSELDATO;
import static no.nav.regoppslag.util.PDLResponseUtil.FULLT_NAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.LANDKODE_NORGE;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTNUMMER;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTSTED;
import static no.nav.regoppslag.util.PDLResponseUtil.TEMA;
import static no.nav.regoppslag.util.PDLResponseUtil.UTENLANDSK_POSTNUMMER;
import static no.nav.regoppslag.util.PDLResponseUtil.UTENLANDSK_POSTSTED;
import static no.nav.regoppslag.util.PDLResponseUtil.V_ADRESSENAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.createDoedsfall;
import static no.nav.regoppslag.util.PDLResponseUtil.createFolkeregisterpersonstatus;
import static no.nav.regoppslag.util.PDLResponseUtil.createHentePersonBuilder;
import static no.nav.regoppslag.util.PDLResponseUtil.createKontaktinformasjonForDoedsbo;
import static no.nav.regoppslag.util.PDLResponseUtil.createKontaktinformasjonForDoedsboWithNoContact;
import static no.nav.regoppslag.util.PDLResponseUtil.createKontaktinformasjonForDoedsboWithOrginasjon;
import static no.nav.regoppslag.util.PDLResponseUtil.createNavnForOrginasjonSomKontakt;
import static no.nav.regoppslag.util.PDLResponseUtil.createPdlHentPersonWithPersonDoedOgAdvokatSomKontakt;
import static no.nav.regoppslag.util.PDLResponseUtil.createPersonKontaktAdresse;
import static no.nav.regoppslag.util.PDLResponseUtil.organisasjonSomKontakt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.GONE;

@ExtendWith(SpringExtension.class)
class DoedsboAdresseServiceTest {

	private static final String FEILMELDING_PERSON_DOED = "Person er død og har ingen registrerte kontaktsopplysninger for dødsbo";

	@InjectMocks
	private PostnummerService postnummerService;
	@Mock
	private PdlGraphQLConsumer pdlGraphQLConsumer;
	private DoedsboAdresseService doedsboAdresseService;

	@BeforeEach
	public void setup() {
		doedsboAdresseService = new DoedsboAdresseService(postnummerService, pdlGraphQLConsumer);
	}

	@Test
	public void shouldMapMottakerInfoForDoedWithAdvokatSomKontakt() {
		List<KontaktinformasjonForDoedsbo> kontaktinformasjon = List.of(createKontaktinformasjonForDoedsbo());
		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(createPdlHentPersonWithPersonDoedOgAdvokatSomKontakt(kontaktinformasjon));
		PdlMottakerInfo mottakerInfo = doedsboAdresseService.mapFoerDoedsbo(createPdlHentPersonWithPersonDoedOgAdvokatSomKontakt(kontaktinformasjon), TEMA);

		assertEquals(FOEDSELDATO, mottakerInfo.getFoedselsdato());
		assertEquals(DOEDSDATO, mottakerInfo.getDoedsdato());
		assertEquals(FULLT_NAVN, mottakerInfo.getNavn());
		assertEquals(V_ADRESSENAVN, mottakerInfo.getPostadresse().getAdresselinje1());
		assertEquals(ADRESSENAVN_1, mottakerInfo.getPostadresse().getAdresselinje2());
		assertEquals(POSTADRESSE_INNLAND, mottakerInfo.getPostadresse().getAdresseType());
		assertEquals(POSTNUMMER, mottakerInfo.getPostadresse().getPostnummer());
		assertEquals(POSTSTED, mottakerInfo.getPostadresse().getPoststed());
		assertEquals(LANDKODE_NORGE, mottakerInfo.getPostadresse().getLandkode());
		assertEquals(KONTAKTINFORMASJONFORDØDSBO, mottakerInfo.getPostadresse().getAdressekilde());
	}

	@Test
	public void shouldMapKontaktinformasjonForDoedsWithAdvokatAsContact() {
		KontaktinformasjonForDoedsbo kontaktinformasjon = createKontaktinformasjonForDoedsbo();

		HentPerson hentPerson = createHentePersonBuilder()
				.doedsfall(List.of(createDoedsfall(DOEDSDATO)))
				.folkeregisterpersonstatus(List.of(createFolkeregisterpersonstatus(PERSONSTATUS_DOED)))
				.kontaktinformasjonForDoedsbo(List.of(kontaktinformasjon))
				.build();

		PdlMottakerInfo mottakerInfo = doedsboAdresseService.mapFoerDoedsbo(hentPerson, TEMA);

		PostadresseTo response = mottakerInfo.getPostadresse();


		assertEquals(V_ADRESSENAVN, response.getAdresselinje1());
		assertEquals(kontaktinformasjon.getAdresse().getAdresselinje1(), response.getAdresselinje2());
		assertNull(response.getAdresselinje3());

		assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		assertEquals(kontaktinformasjon.getAdresse().getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
		assertEquals(KONTAKTINFORMASJONFORDØDSBO, response.getAdressekilde());
	}

	@Test
	public void shouldMapKontaktinformasjonForDoedsboWithPersonAsContact() {
		KontaktinformasjonForDoedsbo kontaktinformasjon = PDLResponseUtil.createKontaktinformasjonForDoedsboWithPerson();
		HentPerson hentPerson = createHentePersonBuilder()
				.doedsfall(List.of(createDoedsfall(DOEDSDATO)))
				.folkeregisterpersonstatus(List.of(createFolkeregisterpersonstatus(PERSONSTATUS_DOED)))
				.kontaktinformasjonForDoedsbo(List.of(kontaktinformasjon))
				.build();

		PdlMottakerInfo mottakerInfo = doedsboAdresseService.mapFoerDoedsbo(hentPerson, TEMA);

		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals(V_ADRESSENAVN, response.getAdresselinje1());
		assertEquals(kontaktinformasjon.getAdresse().getAdresselinje1(), response.getAdresselinje2());
		assertNull(response.getAdresselinje3());

		assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		assertEquals(kontaktinformasjon.getAdresse().getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
		assertEquals(KONTAKTINFORMASJONFORDØDSBO, response.getAdressekilde());
	}

	@Test
	public void shouldMapKontaktinformasjonForDoedsboSomHenteKontaktFraPDL() {
		KontaktinformasjonForDoedsbo kontaktinformasjon = PDLResponseUtil.createKontaktinformasjonForDoedsboWithPerson();
		kontaktinformasjon.getPersonSomKontakt().setPersonnavn(null);
		HentPerson hentPerson = createHentePersonBuilder()
				.doedsfall(List.of(createDoedsfall(DOEDSDATO)))
				.folkeregisterpersonstatus(List.of(createFolkeregisterpersonstatus(PERSONSTATUS_DOED)))
				.kontaktinformasjonForDoedsbo(List.of(kontaktinformasjon))
				.build();

		when(pdlGraphQLConsumer.hentDoedsBoKontaktPersonnavn(anyString(), anyString())).thenReturn(Optional.of(FULLT_NAVN));

		PdlMottakerInfo mottakerInfo = doedsboAdresseService.mapFoerDoedsbo(hentPerson, TEMA);

		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals("v/ " + FULLT_NAVN, response.getAdresselinje1());
		assertEquals(kontaktinformasjon.getAdresse().getAdresselinje1(), response.getAdresselinje2());
		assertNull(response.getAdresselinje3());

		assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		assertEquals(kontaktinformasjon.getAdresse().getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
		assertEquals(KONTAKTINFORMASJONFORDØDSBO, response.getAdressekilde());
	}

	@Test
	public void shouldMapKontaktinformasjonForDoedsboWithOrganisasjonAsContact() {
		KontaktinformasjonForDoedsbo kontaktinformasjon = createKontaktinformasjonForDoedsboWithOrginasjon(organisasjonSomKontakt(createNavnForOrginasjonSomKontakt()), createPersonKontaktAdresse());
		HentPerson hentPerson = createHentePersonBuilder()
				.doedsfall(List.of(createDoedsfall(DOEDSDATO)))
				.folkeregisterpersonstatus(List.of(createFolkeregisterpersonstatus(PERSONSTATUS_DOED)))
				.kontaktinformasjonForDoedsbo(List.of(kontaktinformasjon))
				.build();

		PdlMottakerInfo mottakerInfo = doedsboAdresseService.mapFoerDoedsbo(hentPerson, TEMA);
		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals(CO_ORGINASJON_NAVN, response.getAdresselinje1());
		assertEquals(kontaktinformasjon.getAdresse().getAdresselinje1(), response.getAdresselinje2());
		assertNull(response.getAdresselinje3());

		assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		assertEquals(kontaktinformasjon.getAdresse().getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
		assertEquals(KONTAKTINFORMASJONFORDØDSBO, response.getAdressekilde());
	}

	@Test
	public void shouldMapKontaktinformasjonForDoedsboWithUtenlandskAdresse() {
		KontaktinformasjonForDoedsbo kontaktinformasjon = PDLResponseUtil.createKontaktinformasjonForDoeds().build();
		HentPerson hentPerson = createHentePersonBuilder()
				.doedsfall(List.of(createDoedsfall(DOEDSDATO)))
				.folkeregisterpersonstatus(List.of(createFolkeregisterpersonstatus(PERSONSTATUS_DOED)))
				.kontaktinformasjonForDoedsbo(List.of(kontaktinformasjon))
				.build();

		PdlMottakerInfo mottakerInfo = doedsboAdresseService.mapFoerDoedsbo(hentPerson, TEMA);
		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals(V_ADRESSENAVN, response.getAdresselinje1());
		assertEquals(ADRESSENAVN_1, response.getAdresselinje2());
		assertEquals(UTENLANDSK_POSTNUMMER + " " + UTENLANDSK_POSTSTED, response.getAdresselinje3());

		assertEquals(POSTADRESSE_UTLAND, response.getAdresseType());
		assertEquals("DE", response.getLandkode());
		assertNull(response.getPostnummer());
		assertNull(response.getPoststed());
		assertEquals(KONTAKTINFORMASJONFORDØDSBO, response.getAdressekilde());
	}

	@Test
	public void shouldMapKontaktinformasjonForDoedsboWithKontaktPersonNull() {
		KontaktinformasjonForDoedsbo kontaktinformasjon = createKontaktinformasjonForDoedsboWithOrginasjon(organisasjonSomKontakt(createNavnForOrginasjonSomKontakt()), createPersonKontaktAdresse());
		HentPerson hentPerson = createHentePersonBuilder()
				.doedsfall(List.of(createDoedsfall(DOEDSDATO)))
				.folkeregisterpersonstatus(List.of(createFolkeregisterpersonstatus(PERSONSTATUS_DOED)))
				.kontaktinformasjonForDoedsbo(List.of(kontaktinformasjon))
				.build();

		kontaktinformasjon.getOrganisasjonSomKontakt().setKontaktperson(null);

		PdlMottakerInfo mottakerInfo = doedsboAdresseService.mapFoerDoedsbo(hentPerson, TEMA);
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
	public void shouldThrowFunctionalExceptionIfPersonErDoedOgHarIngenAdresse() {
		when(pdlGraphQLConsumer.hentPerson(anyString(), anyString())).thenReturn(createPdlHentPersonWithPersonDoedOgAdvokatSomKontakt(emptyList()));
		UkjentAdressePersonErDoed e = assertThrows(UkjentAdressePersonErDoed.class, () ->
				doedsboAdresseService.mapFoerDoedsbo(createPdlHentPersonWithPersonDoedOgAdvokatSomKontakt(emptyList()), TEMA));
		assertEquals(GONE, e.getHttpStatusCode());
		assertEquals(FEILMELDING_PERSON_DOED, e.getMessage());
	}

	@Test
	public void shouldThrowExceptionWhenKontaktAdresseForDoedsboIsNull() {
		KontaktinformasjonForDoedsbo kontaktinformasjon = createKontaktinformasjonForDoedsboWithOrginasjon(organisasjonSomKontakt(createNavnForOrginasjonSomKontakt()), null);

		HentPerson hentPerson = createHentePersonBuilder()
				.doedsfall(List.of(createDoedsfall(DOEDSDATO)))
				.folkeregisterpersonstatus(List.of(createFolkeregisterpersonstatus(PERSONSTATUS_DOED)))
				.kontaktinformasjonForDoedsbo(List.of(kontaktinformasjon))
				.build();

		UkjentAdressePersonErDoed e = assertThrows(UkjentAdressePersonErDoed.class, () ->
				doedsboAdresseService.mapFoerDoedsbo(hentPerson, TEMA));

		assertEquals(GONE, e.getHttpStatusCode());
		assertEquals(FEILMELDING_PERSON_DOED, e.getMessage());
	}

	@Test
	public void shouldThrowExceptionWhenKontakterAdresseForDoedsboWithOrginasjonIsNull() {
		KontaktinformasjonForDoedsbo kontaktinformasjon = createKontaktinformasjonForDoedsboWithOrginasjon(null, createPersonKontaktAdresse());

		HentPerson hentPerson = createHentePersonBuilder()
				.doedsfall(List.of(createDoedsfall(DOEDSDATO)))
				.folkeregisterpersonstatus(List.of(createFolkeregisterpersonstatus(PERSONSTATUS_DOED)))
				.kontaktinformasjonForDoedsbo(List.of(kontaktinformasjon))
				.build();

		UkjentAdressePersonErDoed e = assertThrows(UkjentAdressePersonErDoed.class, () -> doedsboAdresseService.mapFoerDoedsbo(hentPerson, TEMA));

		assertEquals(GONE, e.getHttpStatusCode());
		assertEquals(FEILMELDING_PERSON_DOED, e.getMessage());
	}

	@Test
	public void shouldThrowFunctionalGoneExceptionWhenDoedboWithNoKontakt() {

		KontaktinformasjonForDoedsbo kontaktinformasjon = createKontaktinformasjonForDoedsboWithNoContact(createPersonKontaktAdresse());
		HentPerson hentPerson = createHentePersonBuilder()
				.doedsfall(List.of(HentPerson.Doedsfall.builder().doedsdato(DOEDSDATO).build()))
				.kontaktinformasjonForDoedsbo(List.of(kontaktinformasjon))
				.folkeregisterpersonstatus(List.of(HentPerson.Folkeregisterpersonstatus.builder()
						.status(PERSONSTATUS_DOED)
						.forenkletStatus("bosattEtterFolkeregisterloven")
						.build()))
				.build();
		UkjentAdressePersonErDoed e = assertThrows(UkjentAdressePersonErDoed.class, () -> doedsboAdresseService.mapFoerDoedsbo(hentPerson, TEMA),
				"Mottaker er registrert som død og har ugyldig postadresse");
		assertEquals(GONE, e.getHttpStatusCode());
	}

}