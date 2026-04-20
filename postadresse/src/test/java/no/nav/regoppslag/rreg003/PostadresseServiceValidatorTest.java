package no.nav.regoppslag.rreg003;

import no.nav.regoppslag.consumer.pdl.to.PdlMottakerInfo;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static no.nav.regoppslag.pdl.MapPDLResponse.FORTROLIG;
import static no.nav.regoppslag.pdl.MapPDLResponse.STRENGT_FORTROLIG;
import static no.nav.regoppslag.pdl.MapPDLResponse.STRENGT_FORTROLIG_UTLAND;
import static no.nav.regoppslag.rreg003.PostadresseServiceValidator.ADRESSEBESKYTTELSE_GYLDIGE_VERDIER;
import static no.nav.regoppslag.rreg003.PostadresseServiceValidator.personHarAdressebeskyttelse;
import static no.nav.regoppslag.rreg003.PostadresseServiceValidator.validerBehandlingsnummer;
import static no.nav.regoppslag.rreg003.PostadresseServiceValidator.validerRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostadresseServiceValidatorTest {

	private static final String IDENT_ORGNR = "123456789";
	private static final String IDENT_FNR = "12345678901";

	@ParameterizedTest
	@NullAndEmptySource
	void skalGodtaNullOgTomtBehandlingsnummer(String behandlingsnummer) {
		assertThatNoException().isThrownBy(() -> validerBehandlingsnummer(behandlingsnummer));
	}

	@ParameterizedTest
	@ValueSource(strings = {"B123", "Z001"})
	void skalGodtaGyldigBehandlingsnummer(String behandlingsnummer) {
		assertThatNoException().isThrownBy(() -> validerBehandlingsnummer(behandlingsnummer));
	}

	@ParameterizedTest
	@ValueSource(strings = {"B123,B456", "B123, B456", "B123,B456,C789"})
	void skalGodtaGyldigKommaseparertBehandlingsnummer(String behandlingsnummer) {
		assertThatNoException().isThrownBy(() -> validerBehandlingsnummer(behandlingsnummer));
	}

	@ParameterizedTest
	@ValueSource(strings = {"B1234", "B12", "b123", "BB13", "BB123", "123B"})
	void skalAvviseUgyldigBehandlingsnummer(String behandlingsnummer) {
		assertThatThrownBy(() -> validerBehandlingsnummer(behandlingsnummer))
				.isInstanceOf(RegoppslagIllegalArgumentException.class)
				.hasMessage("Ugyldig input med feilmelding=Hvert behandlingsnummer må bestå av én stor bokstav med tre etterfølgende siffer. F.eks. B123.");
	}

	@ParameterizedTest
	@ValueSource(strings = {"B123,a999", "a999,B123", "B123,,B456", ",B123", "B123,"})
	void skalAvviseKommaseparertListeMedUgyldigBehandlingsnummer(String behandlingsnummer) {
		assertThatThrownBy(() -> validerBehandlingsnummer(behandlingsnummer))
				.isInstanceOf(RegoppslagIllegalArgumentException.class)
				.hasMessage("Ugyldig input med feilmelding=Hvert behandlingsnummer må bestå av én stor bokstav med tre etterfølgende siffer. F.eks. B123.");
	}

	@ParameterizedTest
	@MethodSource
	@NullSource
	void skalGodtaRequestMedEllerUtenFiltreradressebeskyttelse(Set<String> filtrerAdressebeskyttelse) {
		var request = lagPostadresseRequest(IDENT_ORGNR);
		request.setFiltrerAdressebeskyttelse(filtrerAdressebeskyttelse);

		assertThatNoException().isThrownBy(() -> validerRequest(request));
	}

	private static Stream<Arguments> skalGodtaRequestMedEllerUtenFiltreradressebeskyttelse() {
		return Stream.of(
				Arguments.of(emptySet()),
				Arguments.of(Set.of(FORTROLIG, STRENGT_FORTROLIG))
		);
	}

	@Test
	void skalKasteExceptionHvisRequestErNull() {
		assertThatExceptionOfType(RegoppslagIllegalArgumentException.class)
				.isThrownBy(() -> validerRequest(null))
				.withMessage("Ugyldig input med feilmelding=Request body er tom.");
	}

	@Test
	void skalKasteExceptionHvisIdentErNull() {
		var request = lagPostadresseRequest(null);

		assertThatExceptionOfType(RegoppslagIllegalArgumentException.class)
				.isThrownBy(() -> validerRequest(request))
				.withMessage("Ugyldig input med feilmelding=Ident kan ikke være null.");
	}

	@ParameterizedTest
	@ValueSource(strings = {"a123", "123a", " 1234", "12-34", "1234567890!"})
	void skalKasteExceptionHvisIdentInneholderIkkeNumeriskeTegn(String ident) {
		var request = lagPostadresseRequest(ident);

		assertThatExceptionOfType(RegoppslagIllegalArgumentException.class)
				.isThrownBy(() -> validerRequest(request))
				.withMessage("Ugyldig input med feilmelding=Ident kan kun bestå av tall.");
	}

	@Test
	void skalKasteExceptionHvisIdentHarUgyldigLengde() {
		var request = lagPostadresseRequest("12345678");

		assertThatExceptionOfType(RegoppslagIllegalArgumentException.class)
				.isThrownBy(() -> validerRequest(request))
				.withMessage("Ugyldig input med feilmelding=Ident må ha lengde på 9, 11 eller 13 siffer.");
	}

	@Test
	void skalKasteExceptionHvisFiltrerAdressebeskyttelseHarMerEnn3Verdier() {
		var request = PostadresseRequest.builder()
				.ident(IDENT_ORGNR)
				.filtrerAdressebeskyttelse(Set.of(FORTROLIG, STRENGT_FORTROLIG, STRENGT_FORTROLIG_UTLAND, "EKSTRA_VERDI"))
				.build();

		assertThatExceptionOfType(RegoppslagIllegalArgumentException.class)
				.isThrownBy(() -> validerRequest(request))
				.withMessage("Ugyldig input med feilmelding=FiltrerAdressebeskyttelse må inneholde en eller flere av %s. Fikk ugyldig filtrerAdressebeskyttelse=[EKSTRA_VERDI]".formatted(ADRESSEBESKYTTELSE_GYLDIGE_VERDIER));
	}

	@Test
	void skalKasteExceptionHvisFiltrerAdressebeskyttelseHarUgyldigVerdi() {
		var request = PostadresseRequest.builder()
				.ident(IDENT_ORGNR)
				.filtrerAdressebeskyttelse(Set.of("STRENGT_FORTROLIG_INNLAND"))
				.build();

		assertThatExceptionOfType(RegoppslagIllegalArgumentException.class)
				.isThrownBy(() -> validerRequest(request))
				.withMessage("Ugyldig input med feilmelding=FiltrerAdressebeskyttelse må inneholde en eller flere av %s. Fikk ugyldig filtrerAdressebeskyttelse=[STRENGT_FORTROLIG_INNLAND]".formatted(ADRESSEBESKYTTELSE_GYLDIGE_VERDIER));
	}

	@ParameterizedTest
	@MethodSource
	void skalReturnereFalseHvisAdressebeskyttelseFraInputEllerPdlErTomEllerNull(Set<String> adressebeskyttelseFraInput, Set<String> adressebeskyttelseFraPdl) {
		var request = PostadresseRequest.builder()
				.ident(IDENT_FNR)
				.filtrerAdressebeskyttelse(adressebeskyttelseFraInput)
				.build();
		var pdlMottakerInfo = PdlMottakerInfo.builder()
				.adressebeskyttelseType(adressebeskyttelseFraPdl)
				.build();

		assertThat(personHarAdressebeskyttelse(request, pdlMottakerInfo)).isFalse();
	}

	private static Stream<Arguments> skalReturnereFalseHvisAdressebeskyttelseFraInputEllerPdlErTomEllerNull() {
		return Stream.of(
				Arguments.of(null, Set.of(STRENGT_FORTROLIG_UTLAND)),
				Arguments.of(emptySet(), Set.of(STRENGT_FORTROLIG_UTLAND)),
				Arguments.of(Set.of(STRENGT_FORTROLIG_UTLAND), null),
				Arguments.of(Set.of(STRENGT_FORTROLIG_UTLAND), emptySet())
		);
	}

	@Test
	void skalReturnereFalseNaarAdressebeskyttelseFraInputOgPdlErUlike() {
		var request = PostadresseRequest.builder()
				.ident(IDENT_FNR)
				.filtrerAdressebeskyttelse(Set.of(FORTROLIG))
				.build();
		var pdlMottakerInfo = PdlMottakerInfo.builder()
				.adressebeskyttelseType(Set.of(STRENGT_FORTROLIG))
				.build();

		assertThat(personHarAdressebeskyttelse(request, pdlMottakerInfo)).isFalse();
	}

	@ParameterizedTest
	@MethodSource
	void skalReturnereTrueNaarAdressebeskyttelseFraInputOgPdlMatcher(Set<String> adressebeskyttelseFraInput, Set<String> adressebeskyttelseFraPdl) {
		var request = PostadresseRequest.builder()
				.ident(IDENT_FNR)
				.filtrerAdressebeskyttelse(adressebeskyttelseFraInput)
				.build();
		var pdlMottakerInfo = PdlMottakerInfo.builder()
				.adressebeskyttelseType(adressebeskyttelseFraPdl)
				.build();

		assertThat(personHarAdressebeskyttelse(request, pdlMottakerInfo)).isTrue();
	}

	private static Stream<Arguments> skalReturnereTrueNaarAdressebeskyttelseFraInputOgPdlMatcher() {
		return Stream.of(
				Arguments.of(Set.of(STRENGT_FORTROLIG_UTLAND), Set.of(STRENGT_FORTROLIG_UTLAND)),
				Arguments.of(Set.of(FORTROLIG, STRENGT_FORTROLIG), Set.of(FORTROLIG)),
				Arguments.of(Set.of(FORTROLIG), Set.of(FORTROLIG, STRENGT_FORTROLIG))
		);
	}

	private PostadresseRequest lagPostadresseRequest(String ident) {
		return PostadresseRequest.builder()
				.ident(ident)
				.build();
	}

}