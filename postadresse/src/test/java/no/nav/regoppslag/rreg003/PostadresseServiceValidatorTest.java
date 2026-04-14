package no.nav.regoppslag.rreg003;

import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class PostadresseServiceValidatorTest {

	private static final String VALID_IDENT = "01020304051";

	@ParameterizedTest
	@ValueSource(strings = {"B123", "A000", "Z999"})
	void shouldAcceptValidSingleBehandlingsnummer(String behandlingsnummer) {
		PostadresseRequest request = PostadresseRequest.builder().ident(VALID_IDENT).build();
		assertDoesNotThrow(() -> PostadresseServiceValidator.validateInput(request, behandlingsnummer));
	}

	@Test
	void shouldAcceptNullBehandlingsnummer() {
		PostadresseRequest request = PostadresseRequest.builder().ident(VALID_IDENT).build();
		assertDoesNotThrow(() -> PostadresseServiceValidator.validateInput(request, null));
	}

	@ParameterizedTest
	@ValueSource(strings = {"B123,A456", "B123,A456,C789", "Z001,Z002"})
	void shouldAcceptValidCommaSeparatedBehandlingsnummerListe(String behandlingsnummer) {
		PostadresseRequest request = PostadresseRequest.builder().ident(VALID_IDENT).build();
		assertDoesNotThrow(() -> PostadresseServiceValidator.validateInput(request, behandlingsnummer));
	}

	@ParameterizedTest
	@ValueSource(strings = {"B1234", "B12", "b123", "BB13", "BB123", "B123,invalid", "B123,b456"})
	void shouldRejectInvalidBehandlingsnummer(String behandlingsnummer) {
		PostadresseRequest request = PostadresseRequest.builder().ident(VALID_IDENT).build();
		assertThatThrownBy(() -> PostadresseServiceValidator.validateInput(request, behandlingsnummer))
				.isInstanceOf(RegoppslagIllegalArgumentException.class)
				.hasMessageContaining("Behandlingsnummer må bestå av en stor bokstav og tre etterfølgende siffer. Eks B123");
	}

	@Test
	void shouldRejectNullIdent() {
		PostadresseRequest request = PostadresseRequest.builder().ident(null).build();
		assertThatThrownBy(() -> PostadresseServiceValidator.validateInput(request, null))
				.isInstanceOf(RegoppslagIllegalArgumentException.class)
				.hasMessageContaining("Ident kan ikke være null");
	}

	@Test
	void shouldRejectNullRequest() {
		assertThatThrownBy(() -> PostadresseServiceValidator.validateInput(null, null))
				.isInstanceOf(RegoppslagIllegalArgumentException.class)
				.hasMessageContaining("Request body er tom");
	}

	@Test
	void shouldRejectTooShortIdent() {
		PostadresseRequest request = PostadresseRequest.builder().ident("123").build();
		assertThatThrownBy(() -> PostadresseServiceValidator.validateInput(request, null))
				.isInstanceOf(RegoppslagIllegalArgumentException.class)
				.hasMessageContaining("Ident må ha lengde på 9, 11 eller 13 siffer");
	}

	@Test
	void shouldRejectNonNumericIdent() {
		PostadresseRequest request = PostadresseRequest.builder().ident("123456abc").build();
		assertThatThrownBy(() -> PostadresseServiceValidator.validateInput(request, null))
				.isInstanceOf(RegoppslagIllegalArgumentException.class)
				.hasMessageContaining("Ident kan kun bestå av tall");
	}

	@Test
	void isValidBehandlingsnummerListeShouldReturnTrueForSingle() {
		assertThat(PostadresseServiceValidator.isValidBehandlingsnummerListe("B123")).isTrue();
	}

	@Test
	void isValidBehandlingsnummerListeShouldReturnTrueForCommaSeparated() {
		assertThat(PostadresseServiceValidator.isValidBehandlingsnummerListe("B123,A456")).isTrue();
	}

	@Test
	void isValidBehandlingsnummerListeShouldReturnFalseForInvalidElement() {
		assertThat(PostadresseServiceValidator.isValidBehandlingsnummerListe("B123,invalid")).isFalse();
	}

	@Test
	void shouldRejectInvalidAdressebeskyttelseFiltreringInput() {
		PostadresseRequest request = PostadresseRequest.builder()
				.ident(VALID_IDENT)
				.filtrerAdressebeskyttelse(Set.of("UGYLDIG"))
				.build();
		assertThatThrownBy(() -> PostadresseServiceValidator.validateInput(request, null))
				.isInstanceOf(RegoppslagIllegalArgumentException.class)
				.hasMessageContaining("Fikk ugyldig filtrerAdressebeskyttelse");
	}
}
