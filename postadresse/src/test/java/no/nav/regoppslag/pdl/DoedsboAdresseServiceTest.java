package no.nav.regoppslag.pdl;

import no.nav.regoppslag.consumer.pdl.PdlGraphQLConsumer;
import no.nav.regoppslag.consumer.pdl.to.HentPerson;
import no.nav.regoppslag.consumer.pdl.to.HentPerson.Folkeregisterpersonstatus;
import no.nav.regoppslag.consumer.pdl.to.KontaktinformasjonForDoedsbo;
import no.nav.regoppslag.consumer.pdl.to.PdlMottakerInfo;
import no.nav.regoppslag.consumer.pdl.to.PostadresseTo;
import no.nav.regoppslag.exceptions.UkjentAdressePersonErDoedException;
import no.nav.regoppslag.service.PostnummerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode.KONTAKTINFORMASJONFORDØDSBO;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.PERSONSTATUS_DOED;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_INNLAND;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_UTLAND;
import static no.nav.regoppslag.util.PDLResponseUtil.ADRESSENAVN_1;
import static no.nav.regoppslag.util.PDLResponseUtil.COADRESSENAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.CO_ORGANISASJON_NAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.DOEDSDATO;
import static no.nav.regoppslag.util.PDLResponseUtil.FULLT_NAVN;
import static no.nav.regoppslag.util.PDLResponseUtil.FULLT_NAVN_DOEDSBO;
import static no.nav.regoppslag.util.PDLResponseUtil.LANDKODE_NORGE;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTNUMMER;
import static no.nav.regoppslag.util.PDLResponseUtil.POSTSTED;
import static no.nav.regoppslag.util.PDLResponseUtil.UTENLANDSK_POSTNUMMER;
import static no.nav.regoppslag.util.PDLResponseUtil.UTENLANDSK_POSTSTED;
import static no.nav.regoppslag.util.PDLResponseUtil.createDoedsfall;
import static no.nav.regoppslag.util.PDLResponseUtil.createFolkeregisterpersonstatus;
import static no.nav.regoppslag.util.PDLResponseUtil.createHentePersonBuilder;
import static no.nav.regoppslag.util.PDLResponseUtil.createKontaktinformasjonForDoeds;
import static no.nav.regoppslag.util.PDLResponseUtil.createKontaktinformasjonForDoedsbo;
import static no.nav.regoppslag.util.PDLResponseUtil.createKontaktinformasjonForDoedsboWithNoContact;
import static no.nav.regoppslag.util.PDLResponseUtil.createKontaktinformasjonForDoedsboWithOrganisasjon;
import static no.nav.regoppslag.util.PDLResponseUtil.createKontaktinformasjonForDoedsboWithPerson;
import static no.nav.regoppslag.util.PDLResponseUtil.createNavnForOrganisasjonSomKontakt;
import static no.nav.regoppslag.util.PDLResponseUtil.createPdlHentPersonWithPersonDoedOgAdvokatSomKontakt;
import static no.nav.regoppslag.util.PDLResponseUtil.createPersonKontaktAdresse;
import static no.nav.regoppslag.util.PDLResponseUtil.createPersonKontaktAdresseUtenPoststed;
import static no.nav.regoppslag.util.PDLResponseUtil.organisasjonSomKontakt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.GONE;

@ExtendWith(MockitoExtension.class)
class DoedsboAdresseServiceTest {

	private static final String FEILMELDING_PERSON_DOED = "Person er død og har ingen registrerte kontaktsopplysninger for dødsbo";

	@Mock
	private PostnummerService postnummerService;
	@Mock
	private PdlGraphQLConsumer pdlGraphQLConsumer;
	@InjectMocks
	private DoedsboAdresseService doedsboAdresseService;

	@Test
	public void shouldMapMottakerinfoForDoedsboWithAdvokatSomKontakt() {
		List<KontaktinformasjonForDoedsbo> kontaktinformasjon = singletonList(createKontaktinformasjonForDoedsbo());

		PdlMottakerInfo mottakerInfo = doedsboAdresseService.mapFoerDoedsbo(createPdlHentPersonWithPersonDoedOgAdvokatSomKontakt(kontaktinformasjon));

		assertEquals(DOEDSDATO, mottakerInfo.getDoedsdato());
		assertEquals(FULLT_NAVN_DOEDSBO, mottakerInfo.getNavn());
		assertEquals(COADRESSENAVN, mottakerInfo.getPostadresse().getAdresselinje1());
		assertEquals(ADRESSENAVN_1, mottakerInfo.getPostadresse().getAdresselinje2());
		assertEquals(POSTADRESSE_INNLAND, mottakerInfo.getPostadresse().getAdresseType());
		assertEquals(POSTNUMMER, mottakerInfo.getPostadresse().getPostnummer());
		assertEquals(POSTSTED, mottakerInfo.getPostadresse().getPoststed());
		assertEquals(LANDKODE_NORGE, mottakerInfo.getPostadresse().getLandkode());
		assertEquals(KONTAKTINFORMASJONFORDØDSBO, mottakerInfo.getPostadresse().getAdressekilde());
	}

	@Test
	public void shouldMapKontaktinformasjonForDoedsboWithAdvokatSomKontakt() {
		KontaktinformasjonForDoedsbo kontaktinformasjon = createKontaktinformasjonForDoedsbo();
		HentPerson hentPerson = createHentePersonBuilder()
				.doedsfall(singletonList(createDoedsfall(DOEDSDATO)))
				.folkeregisterpersonstatus(singletonList(createFolkeregisterpersonstatus(PERSONSTATUS_DOED)))
				.kontaktinformasjonForDoedsbo(singletonList(kontaktinformasjon))
				.build();

		PdlMottakerInfo mottakerInfo = doedsboAdresseService.mapFoerDoedsbo(hentPerson);
		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals(FULLT_NAVN_DOEDSBO, mottakerInfo.getNavn());
		assertEquals(COADRESSENAVN, response.getAdresselinje1());
		assertEquals(kontaktinformasjon.getAdresse().getAdresselinje1(), response.getAdresselinje2());
		assertNull(response.getAdresselinje3());
		assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		assertEquals(kontaktinformasjon.getAdresse().getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
		assertEquals(KONTAKTINFORMASJONFORDØDSBO, response.getAdressekilde());
	}

	@Test
	public void shouldMapKontaktinformasjonForDoedsboWithPersonSomKontakt() {
		KontaktinformasjonForDoedsbo kontaktinformasjon = createKontaktinformasjonForDoedsboWithPerson();
		HentPerson hentPerson = createHentePersonBuilder()
				.doedsfall(List.of(createDoedsfall(DOEDSDATO)))
				.folkeregisterpersonstatus(List.of(createFolkeregisterpersonstatus(PERSONSTATUS_DOED)))
				.kontaktinformasjonForDoedsbo(List.of(kontaktinformasjon))
				.build();

		PdlMottakerInfo mottakerInfo = doedsboAdresseService.mapFoerDoedsbo(hentPerson);
		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals(COADRESSENAVN, response.getAdresselinje1());
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
		KontaktinformasjonForDoedsbo kontaktinformasjon = createKontaktinformasjonForDoedsboWithPerson();
		kontaktinformasjon.getPersonSomKontakt().setPersonnavn(null);
		HentPerson hentPerson = createHentePersonBuilder()
				.doedsfall(singletonList(createDoedsfall(DOEDSDATO)))
				.folkeregisterpersonstatus(singletonList(createFolkeregisterpersonstatus(PERSONSTATUS_DOED)))
				.kontaktinformasjonForDoedsbo(singletonList(kontaktinformasjon))
				.build();

		when(pdlGraphQLConsumer.hentDoedsBoKontaktPersonnavn(anyString())).thenReturn(Optional.of(FULLT_NAVN));

		PdlMottakerInfo mottakerInfo = doedsboAdresseService.mapFoerDoedsbo(hentPerson);
		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals("C/O " + FULLT_NAVN, response.getAdresselinje1());
		assertEquals(kontaktinformasjon.getAdresse().getAdresselinje1(), response.getAdresselinje2());
		assertNull(response.getAdresselinje3());
		assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		assertEquals(kontaktinformasjon.getAdresse().getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
		assertEquals(KONTAKTINFORMASJONFORDØDSBO, response.getAdressekilde());
	}

	@Test
	public void shouldMapKontaktinformasjonForDoedsboWithOrganisasjonSomKontakt() {
		KontaktinformasjonForDoedsbo kontaktinformasjon = createKontaktinformasjonForDoedsboWithOrganisasjon(organisasjonSomKontakt(createNavnForOrganisasjonSomKontakt()), createPersonKontaktAdresse());
		HentPerson hentPerson = createHentePersonBuilder()
				.doedsfall(singletonList(createDoedsfall(DOEDSDATO)))
				.folkeregisterpersonstatus(singletonList(createFolkeregisterpersonstatus(PERSONSTATUS_DOED)))
				.kontaktinformasjonForDoedsbo(singletonList(kontaktinformasjon))
				.build();

		PdlMottakerInfo mottakerInfo = doedsboAdresseService.mapFoerDoedsbo(hentPerson);
		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals(CO_ORGANISASJON_NAVN, response.getAdresselinje1());
		assertEquals(kontaktinformasjon.getAdresse().getAdresselinje1(), response.getAdresselinje2());
		assertNull(response.getAdresselinje3());
		assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		assertEquals(kontaktinformasjon.getAdresse().getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
		assertEquals(KONTAKTINFORMASJONFORDØDSBO, response.getAdressekilde());
	}

	@Test
	public void shouldMapKontaktinformasjonUtenPoststedForDoedsboWithOrganisasjonSomKontakt() {
		KontaktinformasjonForDoedsbo kontaktinformasjon = createKontaktinformasjonForDoedsboWithOrganisasjon(organisasjonSomKontakt(createNavnForOrganisasjonSomKontakt()), createPersonKontaktAdresseUtenPoststed());
		HentPerson hentPerson = createHentePersonBuilder()
				.doedsfall(singletonList(createDoedsfall(DOEDSDATO)))
				.folkeregisterpersonstatus(singletonList(createFolkeregisterpersonstatus(PERSONSTATUS_DOED)))
				.kontaktinformasjonForDoedsbo(singletonList(kontaktinformasjon))
				.build();
		when(postnummerService.finnPoststed(POSTNUMMER)).thenReturn(POSTSTED);

		PdlMottakerInfo mottakerInfo = doedsboAdresseService.mapFoerDoedsbo(hentPerson);
		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals(CO_ORGANISASJON_NAVN, response.getAdresselinje1());
		assertEquals(kontaktinformasjon.getAdresse().getAdresselinje1(), response.getAdresselinje2());
		assertNull(response.getAdresselinje3());
		assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		assertEquals(kontaktinformasjon.getAdresse().getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
		assertEquals(KONTAKTINFORMASJONFORDØDSBO, response.getAdressekilde());
		verify(postnummerService, times(1)).finnPoststed(POSTNUMMER);
	}

	@Test
	public void shouldMapKontaktinformasjonForDoedsboWithUtenlandskAdresse() {
		KontaktinformasjonForDoedsbo kontaktinformasjon = createKontaktinformasjonForDoeds().build();
		HentPerson hentPerson = createHentePersonBuilder()
				.doedsfall(singletonList(createDoedsfall(DOEDSDATO)))
				.folkeregisterpersonstatus(singletonList(createFolkeregisterpersonstatus(PERSONSTATUS_DOED)))
				.kontaktinformasjonForDoedsbo(singletonList(kontaktinformasjon))
				.build();

		PdlMottakerInfo mottakerInfo = doedsboAdresseService.mapFoerDoedsbo(hentPerson);
		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals(COADRESSENAVN, response.getAdresselinje1());
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
		KontaktinformasjonForDoedsbo kontaktinformasjon = createKontaktinformasjonForDoedsboWithOrganisasjon(organisasjonSomKontakt(createNavnForOrganisasjonSomKontakt()), createPersonKontaktAdresse());

		HentPerson hentPerson = createHentePersonBuilder()
				.doedsfall(singletonList(createDoedsfall(DOEDSDATO)))
				.folkeregisterpersonstatus(singletonList(createFolkeregisterpersonstatus(PERSONSTATUS_DOED)))
				.kontaktinformasjonForDoedsbo(singletonList(kontaktinformasjon))
				.build();
		kontaktinformasjon.getOrganisasjonSomKontakt().setKontaktperson(null);

		PdlMottakerInfo mottakerInfo = doedsboAdresseService.mapFoerDoedsbo(hentPerson);
		PostadresseTo response = mottakerInfo.getPostadresse();

		assertEquals(CO_ORGANISASJON_NAVN, response.getAdresselinje1());
		assertEquals(kontaktinformasjon.getAdresse().getAdresselinje1(), response.getAdresselinje2());
		assertNull(response.getAdresselinje3());
		assertEquals(POSTADRESSE_INNLAND, response.getAdresseType());
		assertEquals(LANDKODE_NORGE, response.getLandkode());
		assertEquals(kontaktinformasjon.getAdresse().getPostnummer(), response.getPostnummer());
		assertEquals(POSTSTED, response.getPoststed());
	}

	@Test
	public void shouldThrowUkjentAdressePersonErDoedExceptionIfPersonErDoedOgHarIngenAdresse() {
		UkjentAdressePersonErDoedException e = assertThrows(UkjentAdressePersonErDoedException.class, () ->
				doedsboAdresseService.mapFoerDoedsbo(createPdlHentPersonWithPersonDoedOgAdvokatSomKontakt(emptyList())));

		assertEquals(GONE, e.getHttpStatusCode());
		assertEquals(FEILMELDING_PERSON_DOED, e.getMessage());
	}

	@Test
	public void shouldThrowUkjentAdressePersonErDoedExceptionWhenKontaktAdresseForDoedsboIsNull() {
		KontaktinformasjonForDoedsbo kontaktinformasjon = createKontaktinformasjonForDoedsboWithOrganisasjon(organisasjonSomKontakt(createNavnForOrganisasjonSomKontakt()), null);

		HentPerson hentPerson = createHentePersonBuilder()
				.doedsfall(singletonList(createDoedsfall(DOEDSDATO)))
				.folkeregisterpersonstatus(singletonList(createFolkeregisterpersonstatus(PERSONSTATUS_DOED)))
				.kontaktinformasjonForDoedsbo(singletonList(kontaktinformasjon))
				.build();

		UkjentAdressePersonErDoedException e = assertThrows(UkjentAdressePersonErDoedException.class, () -> doedsboAdresseService.mapFoerDoedsbo(hentPerson));

		assertEquals(GONE, e.getHttpStatusCode());
		assertEquals(FEILMELDING_PERSON_DOED, e.getMessage());
	}

	@Test
	public void shouldThrowUkjentAdressePersonErDoedExceptionWhenKontakterAdresseForDoedsboWithOrganisasjonIsNull() {
		KontaktinformasjonForDoedsbo kontaktinformasjon = createKontaktinformasjonForDoedsboWithOrganisasjon(null, createPersonKontaktAdresse());

		HentPerson hentPerson = createHentePersonBuilder()
				.doedsfall(singletonList(createDoedsfall(DOEDSDATO)))
				.folkeregisterpersonstatus(singletonList(createFolkeregisterpersonstatus(PERSONSTATUS_DOED)))
				.kontaktinformasjonForDoedsbo(singletonList(kontaktinformasjon))
				.build();

		UkjentAdressePersonErDoedException e = assertThrows(UkjentAdressePersonErDoedException.class, () -> doedsboAdresseService.mapFoerDoedsbo(hentPerson));

		assertEquals(GONE, e.getHttpStatusCode());
		assertEquals(FEILMELDING_PERSON_DOED, e.getMessage());
	}

	@Test
	public void shouldThrowUkjentAdressePersonErDoedExceptionWhenDoedsboWithNoKontakt() {
		KontaktinformasjonForDoedsbo kontaktinformasjon = createKontaktinformasjonForDoedsboWithNoContact(createPersonKontaktAdresse());

		HentPerson hentPerson = createHentePersonBuilder()
				.doedsfall(singletonList(createDoedsfall(DOEDSDATO)))
				.kontaktinformasjonForDoedsbo(singletonList(kontaktinformasjon))
				.folkeregisterpersonstatus(singletonList(Folkeregisterpersonstatus.builder()
						.status(PERSONSTATUS_DOED)
						.forenkletStatus("bosattEtterFolkeregisterloven")
						.build()))
				.build();

		UkjentAdressePersonErDoedException e = assertThrows(UkjentAdressePersonErDoedException.class, () -> doedsboAdresseService.mapFoerDoedsbo(hentPerson));

		assertEquals(GONE, e.getHttpStatusCode());
		assertEquals(FEILMELDING_PERSON_DOED, e.getMessage());
	}

}